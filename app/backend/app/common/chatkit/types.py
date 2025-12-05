from typing import Any, Annotated, Generic, Literal
from pydantic import AnyUrl, BaseModel, Field
from chatkit.types import (
   ThreadItemBase
)

from chatkit.types import AssistantMessageItem, ClientToolCallItem, EndOfTurnItem, HiddenContextItem, TaskItem, UserMessageItem, WidgetItem, WorkflowItem


class ClientWidgetItem(ThreadItemBase):
    """Thread item containing widget content."""
    type: Literal["client_widget"] = "client_widget"
    name: Annotated[str, Field(description="The name of pre-built the widget to render on the client side.")]
    args: dict[str, Any] | None

ThreadItem = Annotated[
    UserMessageItem
    | AssistantMessageItem
    | ClientToolCallItem
    | WidgetItem
    | WorkflowItem
    | TaskItem
    | HiddenContextItem
    | EndOfTurnItem
    | ClientWidgetItem,
    Field(discriminator="type")
]
"""Union of all thread item variants provided by chatkit + the custom ClientWidgetItem."""


class CustomThreadItemDoneEvent(BaseModel):
    """Event emitted when a thread item is marked complete."""

    type: Literal["thread.item.done"] = "thread.item.done"
    item: ThreadItem

