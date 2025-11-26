# Utility function to map agent framework events to chatkit events

from concurrent.futures import thread
from typing import AsyncGenerator, AsyncIterable
from app.common.chatkit.widgets import build_approval_request
import uuid
from datetime import datetime

from agent_framework import AgentRunResponseUpdate, AgentRunUpdateEvent, ExecutorCompletedEvent, FunctionApprovalRequestContent, FunctionCallContent, FunctionResultContent, RequestInfoEvent, TextContent, WorkflowEvent, WorkflowStatusEvent, ExecutorInvokedEvent, SuperStepStartedEvent, SuperStepCompletedEvent
from chatkit.types import (
    AssistantMessageContent,
    AssistantMessageContentPartTextDelta,
    AssistantMessageItem,
    ThreadItemAddedEvent,
    ThreadItemDoneEvent,
    ThreadItemUpdated,
    ThreadItemUpdate,
    ThreadStreamEvent,
    ProgressUpdateEvent,
    CustomTask,
    TaskItem,
    WidgetItem
)

#dictionary for tool call name <-> description mapper
event_description_map = {
    "WorkflowStartedEvent" : "Processing your request ...",
    "getAccountsByUserName": {
        "start": "Looking up your account for your user name...",
        "end": "Retrieved your accounts"
    },
    "getAccountDetails": {
        "start": "Fetching your account details...",
        "end": "Fetched your account details"
    },
    "getPaymentMethodDetails": {
        "start": "Fetching your payment method details...",
        "end": "Fetched your payment method details"
    },
    "getTransactionsByRecipientName": {
        "start": "Searching transactions for the recipient...",
        "end": "Found transactions for the recipient"
    },
    "scan_invoice": {
        "start": "Extracting data from the uploaded image...",
        "end": "Data extracted from the uploaded image"
    },
    "processPayment": {
        "start": "Processing your payment...",
        "end": "Payment processed"
    },
    "getCreditCards": {
        "start": "Retrieving your credit cards...",
        "end": "Retrieved your credit cards"
    },
    "getCardDetails": {
        "start": "Fetching your credit card details...",
        "end": "Fetched your credit card details"
    },
    "getCardTransactions": {
        "start": "Looking up transactions for your credit card...",
        "end": "Retrieved transactions for your credit card"
    }


}



class ChatKitEventsHandler:
     
    def __init__(self) -> None:
            # Track if we've started the message
        self.message_started = False
        self.accumulated_text = ""
        self.content_index = 0
        self.tool_name_id_map = {}

    def _handle_text_content(self, thread_id: str, message_id:str ,text:str) -> ThreadStreamEvent:
       
            # Start the assistant message if not already started
        if not self.message_started:
                assistant_message = AssistantMessageItem(
                    id=message_id,
                    thread_id=thread_id,
                    type="assistant_message",
                    content=[AssistantMessageContent(text= text)],
                    created_at=datetime.now(),
                )
                self.message_started = True
                self.accumulated_text = text
                return ThreadItemAddedEvent(type="thread.item.added", item=assistant_message)
                

        
        self.accumulated_text += text
        self.content_index += 1

        item_id = f"itm_{uuid.uuid4().hex[:8]}"
        return ThreadItemUpdated(
            type="thread.item.updated",
            item_id=item_id,
            update=AssistantMessageContentPartTextDelta(
                content_index=self.content_index,
                delta=text,
            ),
        )
        
        


    async def handle_events(self, thread_id: str, events: AsyncIterable[WorkflowEvent]) -> AsyncGenerator[ThreadStreamEvent,None]:
    
        message_id = f"msg_{uuid.uuid4().hex[:8]}"
        async for event in events:
                
            # Skip non-text events
            if(isinstance(event, WorkflowStatusEvent) \
            or isinstance(event, ExecutorInvokedEvent) ) \
            or isinstance(event, ExecutorCompletedEvent) \
            or isinstance(event, SuperStepStartedEvent) \
            or isinstance(event, SuperStepCompletedEvent) \
            or isinstance(event, RequestInfoEvent):
                continue    
            
            #TODO: need to handle WorkflowFailedEvent

            if isinstance(event, AgentRunUpdateEvent):
                if isinstance(event.data, AgentRunResponseUpdate) \
                and event.executor_id != "triage_agent" \
                and event.data is not None and isinstance(event.data, AgentRunResponseUpdate) \
                and event.data.contents \
                and isinstance(event.data.contents, list) \
                and all(isinstance(item, TextContent) for item in event.data.contents): 
                #In our case we just return text
                    
                    text_update = event.data.contents[0].text #type: ignore
                    yield self._handle_text_content(thread_id=thread_id,message_id=message_id,text=text_update)

                if isinstance(event.data, AgentRunResponseUpdate) \
                and event.executor_id != "triage_agent" \
                and event.data.contents \
                and isinstance(event.data.contents, list) \
                and all(isinstance(item, FunctionCallContent) for item in event.data.contents):
                    #force this to be FunctionCallContent
                    function_call_content: FunctionCallContent = event.data.contents[0] #type: ignore
                    

                    if function_call_content.name:
                         call_id = function_call_content.call_id
                         self.tool_name_id_map[call_id] = function_call_content.name
                         descriptive_title = event_description_map[function_call_content.name]["start"] if function_call_content.name in event_description_map else function_call_content.name
                         function_call_task = CustomTask(title=descriptive_title, icon="search")
                         taskUpdate =  TaskItem(thread_id=thread_id,id=call_id, task=function_call_task, created_at=datetime.now())
                         yield ThreadItemAddedEvent(item=taskUpdate)

                if isinstance(event.data, AgentRunResponseUpdate) \
                and event.executor_id != "triage_agent" \
                and event.data.contents \
                and isinstance(event.data.contents, list) \
                and all(isinstance(item, FunctionResultContent) for item in event.data.contents):
                    function_result_content: FunctionResultContent = event.data.contents[0] #type: ignore
                    if function_result_content.call_id :
                       tool_name = self.tool_name_id_map.get(function_result_content.call_id, function_result_content.call_id)     
                       descriptive_title = event_description_map[tool_name]["end"] if tool_name in event_description_map else tool_name
                       function_result_task = CustomTask(title=descriptive_title, icon="check-circle-filled")
                       taskResultUpdate =  TaskItem(thread_id=thread_id,id=function_result_content.call_id, task=function_result_task, created_at=datetime.now())
                       yield ThreadItemAddedEvent(item=taskResultUpdate)
                
                if isinstance(event.data, AgentRunResponseUpdate) \
                and event.executor_id != "triage_agent" \
                and event.data.contents \
                and isinstance(event.data.contents, list) \
                and all(isinstance(item, FunctionApprovalRequestContent) for item in event.data.contents):
                    function_approval_content: FunctionApprovalRequestContent = event.data.contents[0] #type: ignore
                    tool_name =  function_approval_content.function_call.name
                    parsed_args = function_approval_content.function_call.parse_arguments()
                    function_approval_content.function_call.call_id
                    approval_request_widget = build_approval_request(tool_name=tool_name, tool_args=parsed_args, call_id=function_approval_content.function_call.call_id, request_id=function_approval_content.id)
                    #function_result_task = CustomTask(title="ToolName[] " , icon="check-circle-filled", content=f"```py\n{parsed_args}\n```")
                    #taskResultUpdate =  TaskItem(thread_id=thread_id,id="11111", task=function_result_task, created_at=datetime.now())
                    widget_item = WidgetItem(
                    id= f"wdg_{uuid.uuid4().hex[:8]}",
                    thread_id=thread_id,
                    created_at=datetime.now(),
                    widget=approval_request_widget)
                    yield ThreadItemDoneEvent(type="thread.item.done", item=widget_item)

            else:
                event_description = event_description_map.get(event.__class__.__name__, event.__class__.__name__)
                progressUpdate = ProgressUpdateEvent(text=event_description, icon="atom")
                yield progressUpdate

        # Finalize the message
        if self.message_started:
            final_message = AssistantMessageItem(
                id=message_id,
                thread_id=thread_id,
                type="assistant_message",
                content=[AssistantMessageContent(text= self.accumulated_text)]
                if self.accumulated_text
                else [],
                created_at=datetime.now(),
            )

            yield ThreadItemDoneEvent(type="thread.item.done", item=final_message)