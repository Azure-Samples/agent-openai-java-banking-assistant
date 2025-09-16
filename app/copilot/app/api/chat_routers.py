from fastapi import APIRouter, HTTPException, Depends
from typing import List, Any
from app.models.chat import ChatMessage, ChatAppRequest, ChatResponse, ChatResponseMessage, ChatChoice, ChatContext, ChatDelta
from app.api.agent_astool_orchestration import SupervisorOrchestrationService
from semantic_kernel.agents import ChatHistoryAgentThread
from semantic_kernel.contents import ChatHistory
from semantic_kernel.contents import ChatMessageContent
from semantic_kernel.contents.utils.author_role import AuthorRole
from dependency_injector.wiring import Provide, inject
from app.config.container import Container
from app.config.observability import enable_trace

router = APIRouter()



# Helper function to convert messages

def _convert_string_to_chat_response(content: str) -> ChatResponse:
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
    
    return ChatResponse(choices=[choice])

def convertToSKChatHistory(request: ChatAppRequest) -> List[ChatMessageContent]:
    """Convert ChatAppRequest to List[ChatMessageContent]."""
    
    #else create a list with the given messages
    chat_messages = []
    for message in request.messages:  # Exclude the last message
        if message.role == "user":
            chat_messages.append(ChatMessageContent(role=AuthorRole.USER, content=message.content))
        elif message.role == "assistant":
            chat_messages.append(ChatMessageContent(role=AuthorRole.ASSISTANT, content=message.content))
    return chat_messages

def _convertToSKThread(request: ChatAppRequest) -> ChatHistoryAgentThread :
    """Convert ChatAppRequest to ChatHistoryAgentThread."""
    
   # if request.threadId is None:
   #     return None 
    #else create a list with the given messages
    chat_messages = []
    for message in request.messages:  # Exclude the last message
        if message.role == "user":
            # Check if there are attachments and append to the content string
            content = message.content
            if message.attachments:
                content += " " + ",".join(message.attachments)
            chat_messages.append(ChatMessageContent(role=AuthorRole.USER, content=content))
        elif message.role == "assistant":
            chat_messages.append(ChatMessageContent(role=AuthorRole.ASSISTANT, content=message.content))
    return ChatHistoryAgentThread(ChatHistory(messages=chat_messages),thread_id=request.threadId)


@router.post("/chat", response_model=ChatResponse)
@inject
async def chat(chat_request: ChatAppRequest, orchestration_service : SupervisorOrchestrationService = Depends(Provide[Container.supervisor_orchestration_service])):
    if chat_request.stream:
        raise HTTPException(status_code=400, detail="Requested application/json but also requested streaming. Use application/ndjson.")
    if not chat_request.messages:
        raise HTTPException(status_code=400, detail="history cannot be null in Chat request")

    thread : ChatHistoryAgentThread  = _convertToSKThread(chat_request)
    
        
    # Use the agent orchestration service with dependency injection
    response_content = await orchestration_service.processMessage(thread)
    
    # Convert string response to structured ChatResponse
    return _convert_string_to_chat_response(response_content)
