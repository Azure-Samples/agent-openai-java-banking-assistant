from typing import Any, AsyncIterator
from fastapi import APIRouter, Request,Depends, UploadFile, File
from dependency_injector.wiring import Provide, inject
from fastapi.responses import Response, StreamingResponse, FileResponse
from starlette.responses import JSONResponse
from agent_framework_chatkit import ThreadItemConverter, stream_agent_response
from chatkit.actions import Action
from chatkit.server import ChatKitServer
from chatkit.store import StoreItemType, default_generate_id
from chatkit.types import (
    ThreadItemDoneEvent,
    ThreadMetadata,
    ThreadStreamEvent,
    UserMessageItem,
    WidgetItem,
    ErrorEvent
)

from app.agents.azure_chat.handoff.chatkit.handoff_orchestrator_chatkit import HandoffOrchestrator
from app.agents.azure_chat.handoff.chatkit._chatkit_events_handler import ChatKitEventsHandler
from app.config.container_azure_chat import Container
from .attachement_store import AttachmentMetadataStore

from .memory_store import MemoryStore
from .sqllite_store import SQLiteStore
import logging

router = APIRouter()
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

           

        except Exception as e:
            logger.error(f"Error processing message for thread {thread.id}: {e}", exc_info=True)
            yield ErrorEvent(message = f"An error occurred while processing your message for thread {thread.id}")

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


@router.post("/chatkit")
@inject
async def chatkit_endpoint(request: Request, handoff_orchestrator: HandoffOrchestrator = Depends(Provide[Container.handoff_orchestrator_chatkit])):
    """Main ChatKit endpoint that handles all ChatKit requests.

    This endpoint follows the ChatKit server protocol and handles both
    streaming and non-streaming responses.
    """
    logger.debug(f"Received ChatKit request from {request.client}")
    origin = request.headers.get("origin")
    logger.debug(f"Request origin: {origin}")

    request_body = await request.body()

    # Create context following the working examples pattern
    context = {"request": request}
    
    chatkit_server = BankingAssistantChatKitServer(handoff_orchestrator=handoff_orchestrator,origin=origin)
    try:
        # Process the request using ChatKit server
        result = await chatkit_server.process(request_body, context)

        # Return appropriate response type
        if hasattr(result, "__aiter__"):  # StreamingResult
            logger.debug("Returning streaming response")
            return StreamingResponse(result, media_type="text/event-stream")  # type: ignore[arg-type]
        # NonStreamingResult
        logger.debug("Returning non-streaming response")
        return Response(content=result.json, media_type="application/json")  # type: ignore[union-attr]
    except Exception as e:
        logger.error(f"Error processing ChatKit request: {e}", exc_info=True)
        raise
