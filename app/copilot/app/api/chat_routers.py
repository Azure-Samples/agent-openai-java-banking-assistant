from fastapi import APIRouter, HTTPException, Depends
from typing import List, Any

# Foundry Agent based dependencies
from app.agents.foundry.supervisor_agent_foundry import SupervisorAgent
from app.config.container_foundry import Container

# Azure Chat based agents dependencies
#from app.agents.azure_chat.supervisor_agent import SupervisorAgent
#from app.config.container_azure_chat import Container

from app.models.chat import ChatAppRequest, ChatResponse, ChatResponseMessage, ChatChoice, ChatContext, ChatDelta
from app.models.chat import ChatMessage as AppChatMessage
from agent_framework._threads import ChatMessageList
from agent_framework import ChatMessage, Role
from dependency_injector.wiring import Provide, inject


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

# Helper function to convert ChatAppRequest to ChatMessageList
def _chat_app_request_to_chat_message_list(chat_request: ChatAppRequest) -> ChatMessageList:
    """Convert a ChatAppRequest to a ChatMessageList for agent-framework threading, mapping roles appropriately."""
    messages: list[ChatMessage] = []
    for msg in chat_request.messages[:-1]:
        # Map roles from ChatAppRequest to agent-framework ChatMessage roles
        if msg.role == "user":
            af_role = Role.USER
        elif msg.role == "assistant":
            af_role = Role.ASSISTANT
        else:
            # raise exception that role is not recognized
            raise ValueError(f"Unrecognized role from ChatAppRequest: {msg.role}. The message content is: {msg.content} ")

        # Create a new ChatMessage instance
        af_message = ChatMessage(
            role=af_role,
            text=msg.content
        )
        messages.append(af_message)
    return ChatMessageList(messages)


@router.post("/chat", response_model=ChatResponse)
@inject
async def chat(chat_request: ChatAppRequest, supervisor_agent : SupervisorAgent = Depends(Provide[Container.supervisor_agent])):
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
    response_content, thread_id = await supervisor_agent.processMessage(last_message.content, 
                                                                        chat_request.threadId,
                                                                        _chat_app_request_to_chat_message_list(chat_request))
    
    # Convert string response to structured ChatResponse
    return _convert_string_to_chat_response(response_content,thread_id)
