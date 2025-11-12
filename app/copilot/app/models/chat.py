from pydantic import BaseModel
from typing import List, Optional, Any

class ChatMessage(BaseModel):
    role: str
    content: str
    attachments: Optional[List[Any]] = None

class ChatAppRequest(BaseModel):
    stream: Optional[bool] = False
    messages: List[ChatMessage]
    attachements: Optional[List[str]] = None
    threadId: Optional[str] = None

class ChatResponseMessage(BaseModel):
    content: str
    role: str
    attachments: List[Any] = []

class ChatContext(BaseModel):
    thoughts: str = ""
    data_points: List[Any] = []

class ChatDelta(BaseModel):
    content: str
    role: str
    attachments: List[Any] = []

class ChatChoice(BaseModel):
    index: int
    message: ChatResponseMessage
    context: ChatContext
    delta: ChatDelta

class ChatResponse(BaseModel):
    choices: List[ChatChoice]
    threadId: str
