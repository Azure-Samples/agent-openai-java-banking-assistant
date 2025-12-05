# Widget Rendering Implementation

This document describes the implementation of the widget rendering functionality for the banking assistant chat application.

## Overview

The widget rendering system allows the backend to stream rich, interactive UI components (widgets) as part of the chat conversation. Widgets are defined using a declarative DSL and are rendered as React components with full interactivity.

## Architecture

### 1. Widget Types (`/components/widgets/types.ts`)

Defines TypeScript types for all widget components:

- **Root Containers**: `CardWidget`, `ListViewWidget`
- **Layout Components**: `BoxWidget`, `RowWidget`, `ColWidget`, `FormWidget`
- **Text Components**: `TextWidget`, `TitleWidget`, `CaptionWidget`, `MarkdownWidget`
- **Interactive Components**: `ButtonWidget`, `SelectWidget`, `CheckboxWidget`, `InputWidget`, `RadioGroupWidget`, `TextareaWidget`
- **Visual Components**: `BadgeWidget`, `IconWidget`, `ImageWidget`, `DividerWidget`, `SpacerWidget`
- **Action Configuration**: `ActionConfig` for handling user interactions

### 2. Widget Utilities (`/components/widgets/utils.ts`)

Provides helper functions for converting widget properties to CSS/Tailwind classes:

- `spacingToClasses()` - Converts spacing objects to Tailwind padding/margin classes
- `borderToStyle()` - Converts border definitions to CSS styles
- `radiusToClass()` - Maps radius tokens to Tailwind border-radius classes
- `colorToStyle()` - Handles color values (tokens, CSS strings, theme-aware colors)
- `backgroundToStyle()` - Handles background colors
- `getSizeClass()` - Maps size tokens for different component types
- `getVariantClass()` - Maps variant tokens for controls
- `buildLayoutClasses()` - Builds complete layout class/style objects for Box/Row/Col
- `createActionHandler()` - Creates action handlers for interactive components

### 3. Widget Renderer (`/components/widgets/WidgetRenderer.tsx`)

The main rendering engine that:

1. **Provides Context**: Uses React Context to pass `onAction` handler and `itemId` to all child components
2. **Maps Widget Types to React Components**: Each widget type has a corresponding component renderer
3. **Handles Interactivity**: Processes user interactions (clicks, changes) and triggers actions
4. **Supports Nesting**: Recursively renders child widgets for layout components

#### Key Components:

- `TextComponent` - Renders plain text with typography controls
- `TitleComponent` - Renders headings with size/weight options
- `CaptionComponent` - Renders supporting text
- `MarkdownComponent` - Renders Markdown content
- `BoxComponent`, `RowComponent`, `ColComponent` - Flex layout containers
- `FormComponent` - Form container with submit handling
- `ButtonComponent` - Interactive buttons with action handlers
- `SelectComponent`, `CheckboxComponent`, `InputComponent`, etc. - Form controls
- `BadgeComponent`, `IconComponent`, `ImageComponent` - Visual elements
- `CardComponent` - Root card container with optional actions
- `ListViewComponent` - Root list container with pagination

### 4. Integration with Chat System

#### StreamViewport Updates (`/components/chat/StreamViewport.tsx`)

- Added `WidgetItemRenderer` component that:
  - Receives `WidgetItem` from the thread
  - Renders the widget using `WidgetRenderer`
  - Handles widget actions by calling `sendWidgetAction` from ChatProvider

#### ChatProvider Updates (`/components/chat/ChatProvider.tsx`)

- Added `sendWidgetAction()` method to handle widget action events
- Widget actions trigger new streaming responses from the server
- Actions are sent as `threads.custom_action` requests with:
  - `item_id`: ID of the widget that triggered the action
  - `action`: The action configuration (type, payload, handler)
  - `thread_id`: Current thread ID

## Data Flow

### 1. Widget Rendering Flow

```
Backend                     Frontend
  |                            |
  |--- thread.item.done ------>|
  |    (type: "widget")        |
  |                            |
  |                      WidgetItem stored
  |                      in ChatProvider
  |                            |
  |                      StreamViewport
  |                      renders widget
  |                            |
  |                      WidgetRenderer
  |                      creates UI
```

### 2. Widget Action Flow

```
User clicks button
      |
ButtonComponent.onClick()
      |
handleAction(action, itemId)
      |
sendWidgetAction(threadId, itemId, action)
      |
POST /chatkit
  {
    type: "threads.custom_action",
    params: {
      item_id: "wdg_xxx",
      action: { type: "approval", payload: {...} },
      thread_id: "thr_xxx"
    }
  }
      |
Backend processes action
      |
<--- SSE Stream ----
  thread.item.added (task update)
  thread.item.done (assistant message)
  etc.
```

## Widget Example

Here's how the approval widget from your example is structured:

```json
{
  "type": "Card",
  "key": "approval_request",
  "padding": 0,
  "children": [
    {
      "type": "Col",
      "align": "center",
      "gap": 4,
      "padding": 4,
      "children": [
        {
          "type": "Col",
          "align": "center",
          "gap": 1,
          "children": [
            {
              "type": "Box",
              "padding": 3,
              "radius": "full",
              "background": "yellow-400",
              "children": [
                {
                  "type": "Icon",
                  "name": "info",
                  "color": "white",
                  "size": "3xl"
                }
              ]
            },
            {
              "type": "Title",
              "value": "Approval Required"
            },
            {
              "type": "Text",
              "value": "This action requires your approval...",
              "color": "secondary"
            }
          ]
        }
      ]
    },
    {
      "type": "Markdown",
      "value": "```py\\n{...}\\n```"
    },
    {
      "type": "Divider",
      "spacing": 2
    },
    {
      "type": "Row",
      "children": [
        {
          "type": "Button",
          "label": "Approve",
          "block": true,
          "onClickAction": {
            "type": "approval",
            "payload": {...},
            "handler": "server"
          }
        },
        {
          "type": "Button",
          "label": "No",
          "variant": "outline",
          "block": true,
          "onClickAction": {
            "type": "approval",
            "payload": {...},
            "handler": "server"
          }
        }
      ]
    }
  ]
}
```

## Styling

The widget system uses:

- **Tailwind CSS** for utility classes
- **Radix UI** for accessible component primitives (Button, Select, Checkbox, etc.)
- **shadcn/ui** components as the foundation
- **CSS-in-JS** for dynamic styles that can't be expressed in Tailwind

## Future Enhancements

1. **Client-side Action Handlers**: Support for `handler: "client"` actions
2. **DatePicker Widget**: Full implementation with calendar UI
3. **Chart Widgets**: Integration with Recharts for data visualization
4. **Form Validation**: Add validation support for form inputs
5. **Loading States**: Add loading indicators for buttons during async actions
6. **Error Handling**: Better error feedback for failed actions
7. **Animations**: Enhanced transitions between widget states
8. **Accessibility**: ARIA attributes and keyboard navigation improvements

## Testing

To test the widget rendering:

1. Start the backend server with widget support
2. Send a message that triggers a widget response
3. Interact with widget buttons/controls
4. Verify that actions trigger new streaming responses
5. Check browser console for action logs and errors

## Files Created/Modified

### New Files:
- `/app/frontend/banking-web/src/components/widgets/types.ts`
- `/app/frontend/banking-web/src/components/widgets/utils.ts`
- `/app/frontend/banking-web/src/components/widgets/WidgetRenderer.tsx`
- `/app/frontend/banking-web/src/components/widgets/index.ts`

### Modified Files:
- `/app/frontend/banking-web/src/components/chat/StreamViewport.tsx`
- `/app/frontend/banking-web/src/components/chat/ChatProvider.tsx`
- `/app/frontend/banking-web/src/components/chat/types.ts`
