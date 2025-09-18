from fastapi import APIRouter, HTTPException, Depends
from typing import List, Any
from app.models.chat import ChatMessage, ChatAppRequest, ChatResponse, ChatResponseMessage, ChatChoice, ChatContext, ChatDelta
from app.api.agent_astool_orchestration import SupervisorOrchestrationService
from dependency_injector.wiring import Provide, inject
from app.config.container import Container
from app.config.observability import enable_trace

router = APIRouter()



# Helper function to convert messages

def _convert_string_to_chat_response(content: str, thread_id: str | None) -> ChatResponse:
    """Convert a string response to ChatResponse format."""
    chat_message = ChatResponseMessage(
        content=content,
        role="assistant",
        attachments=[]
    )
    
    context = ChatContext(
        thoughts="",
        data_points=[]
    )
    
    delta = ChatDelta(
        content=content,
        role="assistant",
        attachments=[]
    )
    
    choice = ChatChoice(
        index=0,
        message=chat_message,
        context=context,
        delta=delta
    )

    return ChatResponse(choices=[choice], threadId=thread_id if thread_id else "")



@router.post("/chat", response_model=ChatResponse)
@inject
async def chat(chat_request: ChatAppRequest, orchestration_service : SupervisorOrchestrationService = Depends(Provide[Container.supervisor_orchestration_service])):
    if chat_request.stream:
        raise HTTPException(status_code=400, detail="Requested application/json but also requested streaming. Use application/ndjson.")
    if not chat_request.messages:
        raise HTTPException(status_code=400, detail="history cannot be null in Chat request")
    
    # Check the request for attachements reference.If any they will be appended to user message

    last_message = chat_request.messages[-1]
    if last_message.attachments:
        # Append attachment references to the user message
        last_message.content += " " + ",".join(last_message.attachments)

    # Use the agent orchestration service with dependency injection
    response_content, thread_id = await orchestration_service.processMessage(last_message.content, chat_request.threadId)
    
    # Convert string response to structured ChatResponse
    return _convert_string_to_chat_response(response_content,thread_id)
