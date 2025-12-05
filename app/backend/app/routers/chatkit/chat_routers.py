from typing import Any, AsyncIterator
from fastapi import APIRouter, Request,Depends
from dependency_injector.wiring import Provide, inject
from fastapi.responses import Response, StreamingResponse

from chatkit.types import (
    ErrorEvent

)

from app.agents.azure_chat.handoff.chatkit.handoff_orchestrator_chatkit import HandoffOrchestrator
from app.routers.chatkit.chatkit_server import BankingAssistantChatKitServer
from app.config.container_azure_chat import Container

import logging

router = APIRouter()
logger = logging.getLogger(__name__)

DEFAULT_USER_ID = "demo_user"



async def wrap_stream_with_error_handling(streaming_result):
    """Wrap the SSE stream to catch errors and emit them as error events.
    
    This ensures that any errors during streaming are sent to the client
    as error events within the SSE stream, rather than causing the HTTP
    connection to fail.
    
    Args:
        streaming_result: A StreamingResult instance (AsyncIterable[bytes])
    
    Yields:
        bytes: SSE-formatted event data
    """
    try:
        async for chunk in streaming_result:
            yield chunk
    except Exception as e:
        logger.error(f"Error during SSE streaming: {e}", exc_info=True)
        # Emit error event to the client within the SSE stream
        error_event = ErrorEvent(
            message=f"An error occurred during streaming: {str(e)}",
            allow_retry=True
        )
        import json
        error_data = error_event.model_dump(mode="json")
        error_line = f"data: {json.dumps(error_data)}\n\n"
        yield error_line.encode("utf-8")


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
            # Wrap the stream with error handling to catch errors during streaming
            # This only applies to errors that occur AFTER the SSE connection is established
            wrapped_stream = wrap_stream_with_error_handling(result)
            return StreamingResponse(wrapped_stream, media_type="text/event-stream")  # type: ignore[arg-type]
        # NonStreamingResult
        logger.debug("Returning non-streaming response")
        return Response(content=result.json, media_type="application/json")  # type: ignore[union-attr]
    except Exception as e:
        logger.error(f"Error processing ChatKit request: {e}", exc_info=True)
        raise
