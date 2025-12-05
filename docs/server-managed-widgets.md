# Server Managed Widgets

## Server-Managed Widgets (DSL-Based)

Server-managed widgets use a declarative DSL to define the UI structure. The server sends a complete widget definition that the client renders dynamically.

**Structure:**
```python
{
    "type": "widget",
    "widget": {  # Root widget component (Card, ListView, etc.)
        "type": "Card",
        "padding": 4,
        "children": [
            {
                "type": "Text",
                "value": "Hello World"
            },
            {
                "type": "Button",
                "label": "Click Me",
                "onClickAction": {
                    "type": "my-action",
                    "payload": {"data": "value"}
                }
            }
        ]
    }
}
```

**Key characteristics:**
- Item type is `"widget"`
- Uses `widget` field containing the root component definition
- Widget structure defined entirely by the server
- Rendered dynamically by the client using WidgetRenderer
- Supports all DSL components (Card, ListView, Text, Button, Box, etc.)

Widgets are constructed with a single container (WidgetRoot), which contains many components (WidgetNode).

## Containers (WidgetRoot)
Containers have specific characteristics, like display status indicator text and primary actions.

### Card
A bounded container for widgets. Supports status, confirm and cancel fields for presenting status indicators and action buttons below the widget.

- children: list[WidgetNode]
- size: "sm" | "md" | "lg" | "full" (default: "md")
- padding: float | str | dict[str, float | str] | None (keys: top, right, bottom, left, x, y)
- background: str | { dark: str, light: str } | None
- status: { text: str, favicon?: str } | { text: str, icon?: str } | None
- collapsed: bool | None
- asForm: bool | None
- confirm: { label: str, action: ActionConfig } | None
- cancel: { label: str, action: ActionConfig } | None
- theme: "light" | "dark" | None
- key: str | None

### ListView – Displays a vertical list of items, each as a ListViewItem.

- children: list[ListViewItem]
- limit: int | "auto" | None
- status: { text: str, favicon?: str } | { text: str, icon?: str } | None
- theme: "light" | "dark" | None
- key: str | None

## Components (WidgetNode)
The following widget types are supported. You can also browse components and use an interactive editor in the components section of the Widget Builder.

### Badge
A small label for status or metadata.

- label: str
- color: "secondary" | "success" | "danger" | "warning" | "info" | "discovery" | None
- variant: "solid" | "soft" | "outline" | None
- pill: bool | None
- size: "sm" | "md" | "lg" | None
- key: str | None

### Box 
A flexible container for layout, supports direction, spacing, and styling.

- children: list[WidgetNode] | None
- direction: "row" | "column" | None
- align: "start" | "center" | "end" | "baseline" | "stretch" | None
- justify: "start" | "center" | "end" | "stretch" | "between" | "around" | "evenly" | None
- wrap: "nowrap" | "wrap" | "wrap-reverse" | None
- flex: int | str | None
- height: float | str | None
- width: float | str | None
- minHeight: int | str | None
- minWidth: int | str | None
- maxHeight: int | str | None
- maxWidth: int | str | None
- size: float | str | None
- minSize: int | str | None
- maxSize: int | str | None
- gap: int | str | None
- padding: float | str | dict[str, float | str] | None (keys: top, right, bottom, left, x, y)
- margin: float | str | dict[str, float | str] | None (keys: top, right, bottom, left, x, y)
- border: int | dict[str, Any] | None (single border: { size: int, color?: str | { dark: str, light: str }, style?: "solid" | "dashed" | "dotted" | "double" | "groove" | "ridge" | "inset" | "outset" }per-side: { top?: int|dict, right?: int|dict, bottom?: int|dict, left?: int|dict, x?: int|dict, y?: int|dict })
- radius: "2xs" | "xs" | "sm" | "md" | "lg" | "xl" | "2xl" | "3xl" | "4xl" | "full" | "100%" | "none" | None
- background: str | { dark: str, light: str } | None
- aspectRatio: float | str | None
- key: str | None

### Row
Arranges children horizontally.

- children: list[WidgetNode] | None
- gap: int | str | None
- padding: float | str | dict[str, float | str] | None (keys: top, right, - bottom, left, x, y)
- align: "start" | "center" | "end" | "baseline" | "stretch" | None
- justify: "start" | "center" | "end" | "stretch" | "between" | "around" | "evenly" | None
- flex: int | str | None
- height: float | str | None
- width: float | str | None
- minHeight: int | str | None
- minWidth: int | str | None
- maxHeight: int | str | None
- maxWidth: int | str | None
- size: float | str | None
- minSize: int | str | None
- maxSize: int | str | None
- margin: float | str | dict[str, float | str] | None (keys: top, right, bottom, left, x, y)
- border: int | dict[str, Any] | None (single border: { size: int, color?: str | { dark: str, light: str }, style?: "solid" | "dashed" | "dotted" | "double" | "groove" | "ridge" | "inset" | "outset" } per-side: { top?: int|dict, right?: int|dict, bottom?: int|dict, left?: int|dict, x?: int|dict, y?: int|dict })
- radius: "2xs" | "xs" | "sm" | "md" | "lg" | "xl" | "2xl" | "3xl" | "4xl" | "full" | "100%" | "none" | None
- background: str | { dark: str, light: str } | None
aspectRatio: float | str | None
key: str | None

### Col
Arranges children vertically.

- children: list[WidgetNode] | None
- gap: int | str | None
- padding: float | str | dict[str, float | str] | None (keys: top, right, bottom, left, x, y)
- align: "start" | "center" | "end" | "baseline" | "stretch" | None
- justify: "start" | "center" | "end" | "stretch" | "between" | "around" | "evenly" | None
- wrap: "nowrap" | "wrap" | "wrap-reverse" | None
- flex: int | str | None
- height: float | str | None
- width: float | str | None
- minHeight: int | str | None
- minWidth: int | str | None
- maxHeight: int | str | None
- maxWidth: int | str | None
- size: float | str | None
- minSize: int | str | None
- maxSize: int | str | None
- margin: float | str | dict[str, float | str] | None (keys: top, right, bottom, left, x, y)
- border: int | dict[str, Any] | None (single border: { size: int, color?: str | { dark: str, light: str }, style?: "solid" | "dashed" | "dotted" | "double" | "groove" | "ridge" | "inset" | "outset" } per-side: { top?: int|dict, right?: int|dict, bottom?: int|dict, left?: int|dict, x?: int|dict, y?: int|dict })
- radius: "2xs" | "xs" | "sm" | "md" | "lg" | "xl" | "2xl" | "3xl" | "4xl" | "full" | "100%" | "none" | None
- background: str | { dark: str, light: str }| None
aspectRatio: float | str | None
- key: str | None

### Button
A flexible action button.

- submit: bool | None
- style: "primary" | "secondary" | None
- label: str
- onClickAction: ActionConfig
- iconStart: str | None
- iconEnd: str | None
- color: "primary" | "secondary" | "info" | "discovery" | "success" | "caution" | "warning" | "danger" | None
- variant: "solid" | "soft" | "outline" | "ghost" | None
- size: "3xs" | "2xs" | "xs" | "sm" | "md" | "lg" | "xl" | "2xl" | "3xl" | None
- pill: bool | None
- block: bool | None
- uniform: bool | None
- iconSize: "sm" | "md" | "lg" | "xl" | "2xl" | None
- key: str | None

### Caption
Smaller, supporting text.

- value: str
- size: "sm" | "md" | "lg" | None
- weight: "normal" | "medium" | "semibold" | "bold" | None
- textAlign: "start" | "center" | "end" | None
- color: str | { dark: str, light: str } | None
- truncate: bool | None
- maxLines: int | None
- key: str | None

### DatePicker
A date input with a dropdown calendar.

- onChangeAction: ActionConfig | None
- name: str
- min: datetime | None
- max: datetime | None
- side: "top" | "bottom" | "left" | "right" | None
- align: "start" | "center" | "end" | None
- placeholder: str | None
- defaultValue: datetime | None
- variant: "solid" | "soft" | "outline" | "ghost" | None
- size: "3xs" | "2xs" | "xs" | "sm" | "md" | "lg" | "xl" | "2xl" | "3xl" | None
- pill: bool | None
- block: bool | None
- clearable: bool | None
- disabled: bool | None
- key: str | None

### Divider
A horizontal or vertical separator.

- spacing: int | str | None
- color: str | { dark: str, light: str } | None
- size: int | str | None
- flush: bool | None
- key: str | None

### Icon

- name: str
- color: str | { dark: str, light: str } | None
- size: "xs" | "sm" | "md" | "lg" | "xl" | None
- key: str | None

### Image
Displays an image with optional styling, fit, and position.

- size: int | str | None
- height: int | str | None
- width: int | str | None
- minHeight: int | str | None
- minWidth: int | str | None
- maxHeight: int | str | None
- maxWidth: int | str | None
- minSize: int | str | None
- maxSize: int | str | None
- radius: "2xs" | "xs" | "sm" | "md" | "lg" | "xl" | "2xl" | "3xl" | "4xl" | "full" | "100%" | "none" | None
- background: str | { dark: str, light: str } | None
- margin: int | str | dict[str, int | str] | None (keys: top, right, bottom, left, x, y)
- aspectRatio: float | str | None
- flex: int | str | None
- src: str
- alt: str | None
- fit: "none" | "cover" | "contain" | "fill" | "scale-down" | None
- position: "center" | "top" | "bottom" | "left" | "right" | "top left" | "top right" | "bottom left" | "bottom right" | None
- frame: bool | None
- flush: bool | None
- key: str | None

### ListView
Displays a vertical list of items.

-children: list[ListViewItem] | None
-limit: int | "auto" | None
-status: dict[str, Any] | None (shape: { text: str, favicon?: str })
-theme: "light" | "dark" | None
-key: str | None

### ListViewItem
An item in a ListView with optional action.

- children: list[WidgetNode] | None
- onClickAction: ActionConfig | None
- gap: int | str | None
- align: "start" | "center" | "end" | "baseline" | "stretch" | None
- key: str | None

### Markdown
Renders markdown-formatted text, supports streaming updates.

- value: str
- streaming: bool | None
- key: str | None

### Select
A dropdown single-select input.

- options: list[dict[str, str]] (each option: { label: str, value: str })
- onChangeAction: ActionConfig | None
- name: str
- placeholder: str | None
- defaultValue: str | None
- variant: "solid" | "soft" | "outline" | "ghost" | None
- size: "3xs" | "2xs" | "xs" | "sm" | "md" | "lg" | "xl" | "2xl" | "3xl" | None
- pill: bool | None
- block: bool | None
- clearable: bool | None
- disabled: bool | None
- key: str | None

### Spacer
Flexible empty space used in layouts.

- minSize: int | str | None
- key: str | None

### Text
Displays plain text (use Markdown for markdown rendering). Supports streaming updates.

- value: str
- color: str | { dark: str, light: str } | None
- width: float | str | None
- size: "xs" | "sm" | "md" | "lg" | "xl" | None
- weight: "normal" | "medium" | "semibold" | "bold" | None
- textAlign: "start" | "center" | "end" | None
- italic: bool | None
- lineThrough: bool | None
- truncate: bool | None
- minLines: int | None
- maxLines: int | None
- streaming: bool | None
- editable: bool | dict[str, Any] | None (when dict: { name: str, autoComplete?: str, autoFocus?: bool, autoSelect?: bool, allowAutofillExtensions?: bool, required?: bool, placeholder?: str, pattern?: str })
- key: str | None

### Title
Prominent heading text.

- value: str
- size: "xs" | "sm" | "md" | "lg" | "xl" | "2xl" | "3xl" | "4xl" | "5xl" | None
- weight: "normal" | "medium" | "semibold" | "bold" | None
- textAlign: "start" | "center" | "end" | None
- color: str | { dark: str, light: str } | None
- truncate: bool | None
- maxLines: int | None
- key: str | None

### Form
A layout container that can submit an action.

- onSubmitAction: ActionConfig
- children: list[WidgetNode] | None
- align: "start" | "center" | "end" | "baseline" | "stretch" | None
- justify: "start" | "center" | "end" | "stretch" | "between" | "around" | "evenly" | None
- flex: int | str | None
- gap: int | str | None
- height: float | str | None
- width: float | str | None
- minHeight: int | str | None
- minWidth: int | str | None
- maxHeight: int | str | None
- maxWidth: int | str | None
- size: float | str | None
- minSize: int | str | None
- maxSize: int | str | None
- padding: float | str | dict[str, float | str] | None (keys: top, right, bottom, left, x, y)
- margin: float | str | dict[str, float | str] | None (keys: top, right, bottom, left, x, y)
- border: int | dict[str, Any] | None (single border: { size: int, color?: str | { dark: str, light: str }, style?: "solid" | "dashed" | "dotted" | "double" | "groove" | "ridge" | "inset" | "outset" } per-side: { top?: int|dict, right?: int|dict, bottom?: int|dict, left?: int|dict, x?: int|dict, y?: int|dict })
- radius: "2xs" | "xs" | "sm" | "md" | "lg" | "xl" | "2xl" | "3xl" | "4xl" | "full" | "100%" | "none" | None
- background: str | { dark: str, light: str } | None
- key: str | None
- Transition – Wraps content that may animate.
- children: WidgetNode | None
- key: str | None


## ActionConfig

Trigger actions on the backend from user interactions in your chat.
Actions are a way for the ChatKit SDK frontend to trigger a streaming response without the user submitting a message. They can also be used to trigger side-effects outside ChatKit SDK.

### Triggering actions
In response to user interaction with widgets
Actions can be triggered by attaching an ActionConfig to any widget node that supports it. For example, you can respond to click events on Buttons. When a user clicks on this button, the action will be sent to your server where you can update the widget, run inference, stream new thread items, etc.

```python
Button(
    label="Example",
    onClickAction=ActionConfig(
        type="example",
        payload={"id": 123},
    )
)
```

Actions can also be sent imperatively by your frontend with sendAction(). This is probably most useful when you need ChatKit to respond to interaction happening outside ChatKit, but it can also be used to chain actions when you need to respond on both the client and the server (more on that below).


```python
await chatKit.sendAction({
  type: "example",
  payload: { id: 123 },
});
```
### Handling actions

On the server
By default, actions are sent to your server. You can handle actions on your server by implementing the action method on ChatKitServer.

```python
class MyChatKitServer(ChatKitServer[RequestContext])
    async def action(
        self,
        thread: ThreadMetadata,
        action: Action[str, Any],
        sender: WidgetItem | None,
        context: RequestContext,
    ) -> AsyncIterator[Event]:
        if action.type == "example":
          await do_thing(action.payload['id'])

          # often you'll want to add a HiddenContextItem so the model
          # can see that the user did something
          await self.store.add_thread_item(
              thread.id,
              HiddenContextItem(
                  id="item_123",
                  created_at=datetime.now(),
                  content=(
                      "<USER_ACTION>The user did a thing</USER_ACTION>"
                  ),
              ),
              context,
          )

          # then you might want to run inference to stream a response
          # back to the user.
          async for e in self.generate(context, thread):
              yield e
```

### Client
Sometimes you’ll want to handle actions in your client integration. To do that you need to specify that the action should be sent to your client-side action handler by adding handler="client to the ActionConfig.

```python
Button(
    label="Example",
    onClickAction=ActionConfig(
      type="example",
      payload={"id": 123},
      handler="client"
    )
)
```

Then, when the action is triggered, it will then be passed to a callback that you provide when instantiating ChatKit.

```python
async function handleWidgetAction(action: {type: string, Record<string, unknown>}) {
  if (action.type === "example") {
    const res = await doSomething(action)

    // You can fire off actions to your server from here as well.
    // e.g. if you want to stream new thread items or update a widget.
    await chatKit.sendAction({
      type: "example_complete",
      payload: res
    })
  }
}

chatKit.setOptions({
  // other options...
  widgets: { onAction: handleWidgetAction }
})
```