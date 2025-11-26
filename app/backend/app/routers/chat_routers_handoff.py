from fastapi import APIRouter, HTTPException, Depends
from typing import List, Any
import logging

# Foundry Agent based dependencies
#from app.agents.foundry.supervisor_agent_foundry import SupervisorAgent
#from app.config.container_foundry import Container

# Azure Chat based agents dependencies
from app.agents.azure_chat.handoff.handoff_orchestrator import HandoffOrchestrator
from app.config.container_azure_chat import Container

from app.models.chat import ChatAppRequest, ChatResponse, ChatResponseMessage, ChatChoice, ChatContext, ChatDelta
from app.models.chat import ChatMessage as AppChatMessage
# from agent_framework import ChatMessage, Role
from dependency_injector.wiring import Provide, inject
from fastapi.responses import StreamingResponse
import json
from typing import AsyncGenerator


router = APIRouter()
logger = logging.getLogger(__name__)



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
# def _chat_app_request_to_chat_message_list(chat_request: ChatAppRequest) -> ChatMessageList:
#     """Convert a ChatAppRequest to a ChatMessageList for agent-framework threading, mapping roles appropriately."""
#     messages: list[ChatMessage] = []
#     for msg in chat_request.messages[:-1]:
#         # Map roles from ChatAppRequest to agent-framework ChatMessage roles
#         if msg.role == "user":
#             af_role = Role.USER
#         elif msg.role == "assistant":
#             af_role = Role.ASSISTANT
#         else:
#             # raise exception that role is not recognized
#             raise ValueError(f"Unrecognized role from ChatAppRequest: {msg.role}. The message content is: {msg.content} ")

#         # Create a new ChatMessage instance
#         af_message = ChatMessage(
#             role=af_role,
#             text=msg.content
#         )
#         messages.append(af_message)
#     return ChatMessageList(messages)

def _format_stream_chunk(content: str, is_final: bool = False, thread_id: str | None = None) -> str:
    """Format a chunk for streaming response in NDJSON format."""
    if is_final:
        # Final chunk with context and full message
        response = {
            "choices": [{
                "index": 0,
                "delta": {
                    "content": content,  # ← CHANGED: Include content in delta too
                    "role": "assistant",
                    "attachments": []
                },
                "message": {
                    "content": content,
                    "role": "assistant",
                    "attachments": []
                },
                "context": {
                    "thoughts": "",
                    "data_points": []
                }
            }]
        }
        if thread_id:
            response["threadId"] = thread_id #type: ignore
    else:
        # Streaming chunk with delta only
        response = {
            "choices": [{
                "index": 0,
                "delta": {
                    "content": content,
                    "role": "assistant",
                    "attachments": []
                }
            }]
        }
        response["threadId"] = thread_id #type: ignore
    
    return json.dumps(response) + "\n"


async def _stream_response(
    handoff_orchestrator: HandoffOrchestrator,
    user_message: str,
    thread_id: str | None
) -> AsyncGenerator[str, None]:
    """Stream the response from the handoff orchestrator."""
    full_content = ""
    final_thread_id = None
    
    try:
        async for content, is_final, tid in handoff_orchestrator.processMessageStream(user_message, thread_id):
           
          yield _format_stream_chunk(content=content, is_final=is_final, thread_id=tid)
          
    except Exception as e:
        logger.error(f"Error during streaming: {str(e)}", exc_info=True)
        # Send error as a final chunk
        error_message = f"An error occurred: {str(e)}"
        error_response = {
            "choices": [{
                "index": 0,
                "delta": {
                    "content": error_message,
                    "role": "assistant",
                    "attachments": []
                },
                "message": {
                    "content": error_message,
                    "role": "assistant",
                    "attachments": []
                },
                "context": {
                    "thoughts": "",
                    "data_points": []
                }
            }],
            "error": str(e)
        }
        if thread_id:
            error_response["threadId"] = thread_id
        yield json.dumps(error_response) + "\n"


@router.post("/chat")
@inject
async def chat(chat_request: ChatAppRequest, handoff_orchestrator: HandoffOrchestrator = Depends(Provide[Container.handoff_orchestrator])):
    if not chat_request.messages:
        raise HTTPException(status_code=400, detail="history cannot be null in Chat request")
    
    # Check the request for attachments reference. If any they will be appended to user message
    last_message = chat_request.messages[-1]
    if last_message.attachments:
        # Append attachment references to the user message
        last_message.content += " " + ",".join(last_message.attachments)

    # Handle streaming vs non-streaming
    if chat_request.stream:
        try:
            # Return streaming response with NDJSON
            return StreamingResponse(
                _stream_response(handoff_orchestrator, last_message.content, chat_request.threadId),
                media_type="application/x-ndjson"
            )
        except Exception as e:
            logger.error(f"Error initiating stream: {str(e)}", exc_info=True)
            raise HTTPException(status_code=500, detail=f"Error processing request: {str(e)}")
    else:
        raise HTTPException(status_code=500, detail=f"Synch mode not supported: ")
            