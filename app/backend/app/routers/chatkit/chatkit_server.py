import asyncio
from datetime import datetime
from typing import Any, AsyncIterator, Callable, AsyncGenerator
from typing_extensions import TypeVar
from agent_framework_chatkit import ThreadItemConverter
from chatkit.actions import Action
from chatkit.server import ChatKitServer,agents_sdk_user_agent_override
from chatkit.errors import CustomStreamError, StreamError, ErrorCode


from chatkit.types import (
    ThreadItemDoneEvent,
    ThreadMetadata,
    ThreadStreamEvent,
    UserMessageItem,
    WidgetItem,
    ErrorEvent,
    ThreadItem,
    ThreadItemRemovedEvent,
    ThreadItemReplacedEvent,
    ThreadUpdatedEvent,
    HiddenContextItem,
    StreamingReq,
    ThreadsCreateReq,
    ThreadsAddUserMessageReq,
    ThreadsAddClientToolOutputReq,
    ThreadsRetryAfterItemReq,
    ThreadsCustomActionReq,
    Page,
    ThreadCreatedEvent,
    ClientToolCallItem,
    Thread

)

from chatkit.widgets import Card

from app.agents.azure_chat.handoff.chatkit.handoff_orchestrator_chatkit import HandoffOrchestrator
from app.agents.azure_chat.handoff.chatkit._chatkit_events_handler import ChatKitEventsHandler
from app.common.chatkit.types import ClientWidgetItem, CustomThreadItemDoneEvent
from app.config.container_azure_chat import Container

from .attachement_store import AttachmentMetadataStore

from .memory_store import MemoryStore
from .sqllite_store import SQLiteStore
import logging

logger = logging.getLogger(__name__)

DEFAULT_USER_ID = "demo_user"



class BankingAssistantChatKitServer(ChatKitServer[dict[str, Any]]):
    """ChatKit server implementation using Agent Framework.

    This server integrates Agent Framework agents with ChatKit's server protocol,
    handling message conversion, agent execution, and response streaming.
    """
    
    # Use in-memory store for simplicity; replace with persistent store as needed
    store = MemoryStore()

    metadata_store = SQLiteStore()
 
   

    def __init__(self, handoff_orchestrator: HandoffOrchestrator, origin: str  | None = None):
        
        #need to use origin to set base url for attachment store
        if origin is None:
            origin = "http://localhost"
            logger.warning("Origin header is missing; defaulting base_url for attachment to http://localhost")
       
        attachment_metadata_store = AttachmentMetadataStore(
        base_url=origin,
        metadata_store=BankingAssistantChatKitServer.metadata_store,
         )
       
        super().__init__(BankingAssistantChatKitServer.metadata_store, attachment_metadata_store)
    
        # Create ThreadItemConverter with attachment data fetcher
        self.converter = ThreadItemConverter()
        self.handoff_orchestrator = handoff_orchestrator

    async def _update_thread_title(
        self, thread: ThreadMetadata, user_message_content: UserMessageItem, context: dict[str, Any]
    ) -> None:
        """Update thread title simply using first message.

        Args:
            thread: The thread metadata to update.
            thread_items: All items in the thread.
            context: The context dictionary.
        """
        logger.info(f"Attempting to update thread title for thread: {thread.id}")
 

        # Pick the firs user message
        first_user_message: str = "Untitled thread"
       
        for content_part in user_message_content.content:
            if hasattr(content_part, "text") and isinstance(content_part.text, str):
                first_user_message = content_part.text
       
        if not first_user_message:
            logger.debug("No user messages found for title generation. Defaulting to 'Untitled thread'")
            
        thread.title = first_user_message[:50].strip()
        await self.store.save_thread(thread, context)
        logger.info(f"Updated thread {thread.id} title to: {thread.title}")

    
    # This is called by the ChatKit server when a new user message is received
    async def respond(
        self,
        thread: ThreadMetadata,
        input_user_message: UserMessageItem | None,
        context: dict[str, Any],
    ) -> AsyncIterator[ThreadStreamEvent]:
        """Handle incoming user messages and generate responses.

        This method converts ChatKit messages to Agent Framework format using ThreadItemConverter,
        runs the agent, converts the response back to ChatKit events using stream_agent_response,
        and creates interactive weather widgets when weather data is queried.
        """
        from agent_framework import FunctionResultContent

        if input_user_message is None:
            logger.debug("Received None user message, skipping")
            return

        logger.info(f"Processing message for thread: {thread.id}")

        try:
            
            #Extracting the attachments id. Only one is supported right now
            attachment_ids = []
            if input_user_message.attachments :
                attachment_ids = [attachment.id for attachment in input_user_message.attachments]
                logger.info(f"User message has attachments: {attachment_ids}")

            # Convert ChatKit user message to Agent Framework ChatMessage using ThreadItemConverter
            agent_messages = await self.converter.to_agent_input(input_user_message)

            if not agent_messages:
                logger.warning("No messages after conversion")
                return

            logger.info(f"Running agent with {len(agent_messages)} message(s)")


            #get last message
            last_message = agent_messages[-1]

            expanded_text_with_attachements = last_message.text
            

            if attachment_ids:
                expanded_text_with_attachements += (f" [attachment_id: {attachment_ids[0]}]")
            
            af_events = self.handoff_orchestrator.processMessageStream(expanded_text_with_attachements, thread.id)

            chatkit_event_handler = ChatKitEventsHandler()

            async for event in chatkit_event_handler.handle_events(thread.id, af_events):
                yield event

           # Update thread title based on first user message if not already set
            if not thread.title or thread.title == "New thread":
                await self._update_thread_title(thread, input_user_message, context)

        except Exception as e:
            logger.error(f"Error processing message for thread {thread.id}: {e}", exc_info=True)
            yield ErrorEvent(message = f"An error occurred while processing your message for thread {thread.id}")

    #this is called by chatkit server when a custom action is received from the client like the ones defined in widgets.
    async def action(
        self,
        thread: ThreadMetadata,
        action: Action[str, Any],
        sender: WidgetItem | None,
        context: dict[str, Any],
    ) -> AsyncIterator[ThreadStreamEvent]:
        """Handle widget actions from the frontend.

        This method processes actions triggered by interactive widgets,
        such as city selection from the city selector widget.
        """

        logger.info(f"Received action: {action.type} for thread: {thread.id}")

        try:
            if action.type == "approval":
                # Extract city information from the action payload
                approved = action.payload.get("approved", False)
                call_id = action.payload.get("call_id", None)
                request_id = action.payload.get("request_id", None)
                tool_name = action.payload.get("tool_name", None)

            # Manage last user message. what about thread ? 
                af_events = self.handoff_orchestrator.processToolApprovalResponse(thread.id,approved,call_id=call_id, request_id=request_id, tool_name=tool_name)

                chatkit_event_handler = ChatKitEventsHandler()

                async for event in chatkit_event_handler.handle_events(thread.id, af_events):
                    yield event

           
        except Exception as e:
            logger.error(f"Error processing message for thread {thread.id}: {e}", exc_info=True)
            yield ErrorEvent(message = f"An error occurred while processing your message for thread {thread.id}")


########################## ChatKit Server Customization ##############################

    #Need to customize this to support client widget rendering
    async def _process_events(
            self,
            thread: ThreadMetadata,
            context: dict[str, Any],
            stream: Callable[[], AsyncIterator[ThreadStreamEvent]],
        ) -> AsyncIterator[ThreadStreamEvent]:
            await asyncio.sleep(0)  # allow the response to start streaming

            last_thread = thread.model_copy(deep=True)

            try:
                with agents_sdk_user_agent_override():
                    async for event in stream():
                        match event:
                            case ThreadItemDoneEvent() | CustomThreadItemDoneEvent():
                                await self.store.add_thread_item(
                                    thread.id, event.item, context=context # type: ignore
                                )
                            case ThreadItemRemovedEvent():
                                await self.store.delete_thread_item(
                                    thread.id, event.item_id, context=context # type: ignore
                                )
                            case ThreadItemReplacedEvent():
                                await self.store.save_item(
                                    thread.id, event.item, context=context # type: ignore
                                )

                        # special case - don't send hidden context items back to the client
                        should_swallow_event = isinstance(
                            event, ThreadItemDoneEvent
                        ) and isinstance(event.item, HiddenContextItem)

                        if not should_swallow_event:
                            yield event

                        # in case user updated the thread while streaming
                        if thread != last_thread:
                            last_thread = thread.model_copy(deep=True)
                            await self.store.save_thread(thread, context=context) # type: ignore
                            yield ThreadUpdatedEvent(
                                thread=self._to_thread_response(thread)
                            )
                    # in case user updated the thread while streaming
                    if thread != last_thread:
                        last_thread = thread.model_copy(deep=True)
                        await self.store.save_thread(thread, context=context) # type: ignore
                        yield ThreadUpdatedEvent(thread=self._to_thread_response(thread))
            except CustomStreamError as e:
                yield ErrorEvent(
                    code="custom",
                    message=e.message,
                    allow_retry=e.allow_retry,
                )
            except StreamError as e:
                yield ErrorEvent(
                    code=e.code,
                    allow_retry=e.allow_retry,
                )
            except Exception as e:
                yield ErrorEvent(
                    code=ErrorCode.STREAM_ERROR,
                    allow_retry=True,
                )
                logger.exception(e)

            if thread != last_thread:
                # in case user updated the thread at the end of the stream
                await self.store.save_thread(thread, context=context) # type: ignore
                yield ThreadUpdatedEvent(thread=self._to_thread_response(thread))



    async def _process_streaming_impl(
        self, request: StreamingReq, context: dict[str, Any]
    ) -> AsyncGenerator[ThreadStreamEvent, None]:
        match request:
            case ThreadsCreateReq():
                thread = Thread(
                    id=self.store.generate_thread_id(context), # type: ignore
                    created_at=datetime.now(),
                    items=Page(), # type: ignore
                )
                await self.store.save_thread(
                    ThreadMetadata(**thread.model_dump()),
                    context=context,
                )
                yield ThreadCreatedEvent(thread=self._to_thread_response(thread))
                user_message = await self._build_user_message_item(
                    request.params.input, thread, context
                )
                async for event in self._process_new_thread_item_respond(
                    thread,
                    user_message,
                    context,
                ):
                    yield event

            case ThreadsAddUserMessageReq():
                thread = await self.store.load_thread(
                    request.params.thread_id, context=context
                )
                user_message = await self._build_user_message_item(
                    request.params.input, thread, context
                )
                async for event in self._process_new_thread_item_respond(
                    thread,
                    user_message,
                    context,
                ):
                    yield event

            case ThreadsAddClientToolOutputReq():
                thread = await self.store.load_thread(
                    request.params.thread_id, context=context
                )
                items = await self.store.load_thread_items(
                    thread.id, None, 1, "desc", context
                )
                tool_call = next(
                    (
                        item
                        for item in items.data
                        if isinstance(item, ClientToolCallItem)
                        and item.status == "pending"
                    ),
                    None,
                )
                if not tool_call:
                    raise ValueError(
                        f"Last thread item in {thread.id} was not a ClientToolCallItem"
                    )

                tool_call.output = request.params.result
                tool_call.status = "completed"

                await self.store.save_item(thread.id, tool_call, context=context)

                # Safety against dangling pending tool calls if there are
                # multiple in a row, which should be impossible, and
                # integrations should ultimately filter out pending tool calls
                # when creating input response messages.
                await self._cleanup_pending_client_tool_call(thread, context)

                async for event in self._process_events(
                    thread,
                    context,
                    lambda: self.respond(thread, None, context),
                ):
                    yield event

            case ThreadsRetryAfterItemReq():
                thread_metadata = await self.store.load_thread(
                    request.params.thread_id, context=context
                )

                # Collect items to remove (all items after the user message)
                items_to_remove: list[ThreadItem] = []
                user_message_item = None

                async for item in self._paginate_thread_items_reverse(
                    request.params.thread_id, context
                ):
                    if item.id == request.params.item_id:
                        if not isinstance(item, UserMessageItem):
                            raise ValueError(
                                f"Item {request.params.item_id} is not a user message"
                            )
                        user_message_item = item
                        break
                    items_to_remove.append(item)

                if user_message_item:
                    for item in items_to_remove:
                        await self.store.delete_thread_item(
                            request.params.thread_id, item.id, context=context
                        )
                    async for event in self._process_events(
                        thread_metadata,
                        context,
                        lambda: self.respond(
                            thread_metadata,
                            user_message_item,
                            context,
                        ),
                    ):
                        yield event
            case ThreadsCustomActionReq():
                thread_metadata = await self.store.load_thread(
                    request.params.thread_id, context=context
                )

                item = {}
                if request.params.item_id:
                    item = await self.store.load_item(
                        request.params.thread_id,
                        request.params.item_id,
                        context=context,
                    )

                if item and not isinstance(item, WidgetItem) and not isinstance(item, ClientWidgetItem):
                    # shouldn't happen if the caller is using the API correctly.
                    yield ErrorEvent(
                        code=ErrorCode.STREAM_ERROR,
                        message=f"Item {request.params.item_id} is not neither a widget item nor a client widget item",
                        allow_retry=False,
                    )
                    return

                #To bypass type checking we create a fake WidgetItem from ClientWidgetItem.
                fake_widget_root = Card( children=[])
                fake_widget_item: WidgetItem | None = None
               
                if isinstance(item, ClientWidgetItem):
                    fake_widget_item = WidgetItem(
                        id=item.id,
                        thread_id=item.thread_id,
                        created_at=item.created_at,
                        widget = fake_widget_root
                    )
                elif isinstance(item, WidgetItem):
                    fake_widget_item = item

                async for event in self._process_events(
                    thread_metadata,
                    context,
                    lambda: self.action(
                        thread_metadata,
                        request.params.action,
                        fake_widget_item,
                        context,
                    ),
                ):
                    yield event

            case _:
                assert_never(request)

