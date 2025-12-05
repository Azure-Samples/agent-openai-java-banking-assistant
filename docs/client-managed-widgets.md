# Client-Managed Widget System

## Overview

The widget system now supports two rendering modes:

1. **Server-managed widgets** (default): Widgets are defined using a DSL on the server and rendered dynamically on the client
2. **Client-managed widgets** (new): Pre-built React components are registered on the client and referenced by name from the server

## Server-Side: Sending Widgets

The server can send widgets in two modes:

### Server-Managed Widgets (DSL-Based)

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

See `widgets-rendering.md` for complete DSL documentation.

### Client-Managed Widgets (React Components)

Client-managed widgets are pre-built React components registered on the client. The server only sends a component name and arguments.

**Structure:**
```python
{
    "type": "client_widget",  # Indicates client-managed widget
    "name": "tool-approval-request",  # Name of the registered component
    "args": {  # Arguments passed to the component
        "tool_name": "processPayment",
        "tool_args": {...},
        "call_id": "call_123",
        "request_id": "req_456"
    }
}
```

**Key characteristics:**
- Item type is `"client_widget"` (not `"widget"`)
- Uses `name` to specify the registered React component
- Uses `args` to pass data to the component
- No `widget` or `render` fields
- Component must be registered in the client widget registry

### Field Reference

**Server-Managed (DSL):**
- `type`: Always `"widget"`
- `widget`: Root widget component definition (required)

**Client-Managed (React):**
- `type`: Always `"client_widget"`
- `name`: String identifier for the registered React component (required)
- `args`: Dictionary of arguments passed to the component (optional)

## Client-Side: Using Client-Managed Widgets

### Built-in Widgets

The system includes these pre-registered widgets:

#### `tool-approval-request`

Displays a tool execution approval UI with approve/reject buttons.

**Required args:**
- `tool_name`: Name of the tool to approve
- `tool_args`: Arguments for the tool
- `call_id`: Tool call ID
- `request_id`: Request ID

**Optional args:**
- `title`: Custom title (default: "Approval Required")
- `description`: Custom description

**Example server response:**
```json
{
    "type": "client_widget",
    "name": "tool-approval-request",
    "args": {
        "tool_name": "processPayment",
        "tool_args": {
            "account_id": "1010",
            "amount": 103.25
        },
        "call_id": "call_abc123",
        "request_id": "req_xyz789"
    }
}
```

### Registering Custom Widgets

#### Option 1: Via ChatProvider

```typescript
import { ChatProvider, type ClientWidgetComponent } from "@/components/chat";
import { MyCustomWidget } from "./MyCustomWidget";

function App() {
  return (
    <ChatProvider
      customWidgets={{
        "my-widget": MyCustomWidget,
        "another-widget": AnotherWidget,
      }}
    >
      {/* ... */}
    </ChatProvider>
  );
}
```

#### Option 2: Via Widget Registry

```typescript
import { widgetRegistry } from "@/components/widgets";
import { MyCustomWidget } from "./MyCustomWidget";

// Register at app initialization
widgetRegistry.register("my-widget", MyCustomWidget);
```

### Creating Custom Widgets

Custom widgets must implement the `ClientWidgetComponent` interface:

```typescript
import { ClientWidgetProps, useSendWidgetAction } from "@/components/widgets";
import { Card } from "@/components/ui/card";
import { Button } from "@/components/ui/button";

export function MyCustomWidget({ args, itemId }: ClientWidgetProps) {
  const { title, description } = args;
  const sendWidgetAction = useSendWidgetAction();

  const handleClick = () => {
    sendWidgetAction(itemId, {
      type: "my-action",
      payload: { data: "example" },
    });
  };

  return (
    <Card className="p-4">
      <h3>{title as string}</h3>
      <p>{description as string}</p>
      <Button onClick={handleClick}>Click Me</Button>
    </Card>
  );
}
```

### Widget Props

All client widgets receive these props:

- `args`: Record<string, unknown> - Arguments from the server
- `itemId`: string - Widget item ID for tracking

**Note:** Client widgets are responsible for managing their own actions using the `useSendWidgetAction` hook. This gives them direct access to the chat context and eliminates the need for prop drilling.

### Sending Actions

Client widgets handle their own actions using the `useSendWidgetAction` hook. This hook:

1. Accesses the chat context (thread ID, streaming state)
2. Formats the action as a proper `threads.custom_action` request
3. Sends the request to the ChatKit server
4. Initiates streaming response handling
5. Provides lifecycle callbacks for action events

#### Basic Usage

```typescript
import { useSendWidgetAction } from "@/components/widgets";

// In your widget component
export function MyWidget({ args, itemId }: ClientWidgetProps) {
  const sendWidgetAction = useSendWidgetAction();

  const handleClick = () => {
    sendWidgetAction(itemId, {
      type: "my-action-type",
      payload: {
        // Your action data
        data: "value",
      },
    });
  };

  // ... rest of component
}
```

**Action format:**
```typescript
sendWidgetAction(itemId, {
  type: string,              // Action type identifier
  payload?: object,          // Optional action data
  handler?: "server" | "client",  // Default: "server"
  loadingBehavior?: "auto" | "manual", // Default: "auto"
});
```

This automatically sends a `threads.custom_action` request to the server and starts streaming the response.

#### Lifecycle Callbacks

The `useSendWidgetAction` hook supports optional callbacks for monitoring widget action lifecycle events:

```typescript
import { useSendWidgetAction } from "@/components/widgets";
import { useState } from "react";

export function MyWidget({ args, itemId }: ClientWidgetProps) {
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const sendWidgetAction = useSendWidgetAction({
    onThreadStarted: () => {
      // Called when streaming begins
      setIsLoading(true);
      setError(null);
    },
    onThreadEnded: () => {
      // Called when streaming completes
      setIsLoading(false);
    },
    onError: (error) => {
      // Called when an error occurs
      setIsLoading(false);
      setError(error.message);
    }
  });

  // ... rest of component
}
```

**Callback types:**
```typescript
interface WidgetActionCallbacks {
  onThreadStarted?: () => void;
  onThreadEnded?: () => void;
  onError?: (error: { message: string; code?: string }) => void;
}
```

**Common use cases for callbacks:**
- **Disable buttons after submission** to prevent double-submit
- **Show/hide loading indicators** during action processing
- **Display error messages** when actions fail
- **Enable retry on error** by re-enabling UI elements
- **Track action state** for analytics or debugging

**Example: Preventing double-submit:**
```typescript
export function ApprovalWidget({ args, itemId }: ClientWidgetProps) {
  const [isDisabled, setIsDisabled] = useState(false);
  const [loadingButton, setLoadingButton] = useState<'approve' | 'reject' | null>(null);

  const sendWidgetAction = useSendWidgetAction({
    onThreadStarted: () => {
      console.log('Action started');
    },
    onThreadEnded: () => {
      // Permanently disable buttons after successful completion
      setIsDisabled(true);
      setLoadingButton(null);
    },
    onError: (error) => {
      // Re-enable buttons on error to allow retry
      setIsDisabled(false);
      setLoadingButton(null);
      console.error('Action failed:', error.message);
    }
  });

  const handleApprove = () => {
    setLoadingButton('approve');
    sendWidgetAction(itemId, {
      type: "approval",
      payload: { approved: true },
    });
  };

  return (
    <Button 
      onClick={handleApprove} 
      disabled={isDisabled || loadingButton !== null}
      loading={loadingButton === 'approve'}
    >
      Approve
    </Button>
  );
}
```

**Note:** Callbacks only trigger for actions initiated by this specific widget instance. They won't fire for other streaming activity in the chat.

### Utility Functions

The system provides helper functions in `widgetUtils.ts`:

```typescript
import { useSendWidgetAction, formatAsPython, createPythonCodeBlock } from "@/components/widgets";

// Hook to send actions (use inside component)
const sendWidgetAction = useSendWidgetAction();
sendWidgetAction(itemId, {
  type: "my-action-type",
  payload: { data: "value" },
});

// Format object as Python string
const pythonStr = formatAsPython({ key: "value" });
// Result: "{'key': 'value'}"

// Create a Python code block
const codeBlock = createPythonCodeBlock("print('hello')");
// Result: "```py\nprint('hello')\n```"
```

**Additional Data Loading:**

For widgets that need to load data from custom APIs (not through the ChatKit action flow), you are responsible for implementing your own data fetching logic:

```typescript
export function MyDataWidget({ args }: ClientWidgetProps) {
  const [data, setData] = useState(null);

  useEffect(() => {
    // Your custom API call
    fetch('/api/my-data')
      .then(res => res.json())
      .then(setData);
  }, []);

  // ... render with data
}
```

## Benefits of Client-Managed Widgets

1. **Full React Control**: Use any React patterns, hooks, or libraries
2. **Better Performance**: No runtime DSL parsing for complex UIs
3. **Type Safety**: TypeScript support for widget props and args
4. **Reusability**: Share components across multiple widget instances
5. **Custom Styling**: Full control over styling and theming
6. **Rich Interactions**: Complex user interactions without server round-trips

## When to Use Each Mode

### Use Server-Managed (DSL)
- **Simple, declarative UIs**: Forms, cards, lists, text displays
- **Dynamic layouts**: Widget structure determined at runtime based on data
- **Rapid prototyping**: Quick iteration without client code changes
- **Configuration-driven**: UI defined by server logic or business rules
- **Content-focused**: Displaying data with standard UI patterns
- **Backend control**: Server decides the complete UI structure

**Examples:**
- Approval requests with dynamic tool information
- Data cards with varying field sets
- Forms generated from schema
- Status displays with conditional elements

### Use Client-Managed (React)
- **Complex interactions**: Multi-step workflows, real-time updates
- **Rich visualizations**: Charts, graphs, custom data displays
- **Custom animations**: Transitions, loading states, micro-interactions
- **Heavy client logic**: Validation, calculations, state management
- **Reusable patterns**: Components used across multiple widget instances
- **Performance critical**: Large datasets or frequent updates
- **Custom styling**: Pixel-perfect designs or brand-specific UIs

**Examples:**
- Interactive data dashboards
- Multi-step approval workflows
- Custom form builders
- Real-time collaboration widgets

## Comparison: Server vs Client Managed

### Server-Managed (DSL) Example

**Server sends complete UI definition:**
```python
{
    "type": "widget",
    "widget": {
        "type": "Card",
        "padding": 4,
        "children": [
            {
                "type": "Col",
                "align": "center",
                "gap": 2,
                "children": [
                    {
                        "type": "Icon",
                        "name": "info",
                        "size": "3xl",
                        "color": "blue-500"
                    },
                    {
                        "type": "Title",
                        "value": "Approval Required",
                        "size": "lg"
                    },
                    {
                        "type": "Text",
                        "value": "Do you want to proceed?",
                        "color": "secondary"
                    }
                ]
            },
            {
                "type": "Row",
                "gap": 2,
                "children": [
                    {
                        "type": "Button",
                        "label": "Approve",
                        "block": True,
                        "onClickAction": {
                            "type": "approval",
                            "payload": {"approved": True}
                        }
                    },
                    {
                        "type": "Button",
                        "label": "Reject",
                        "block": True,
                        "variant": "outline",
                        "onClickAction": {
                            "type": "approval",
                            "payload": {"approved": False}
                        }
                    }
                ]
            }
        ]
    }
}
```

**Pros:**
- ✅ No client code changes needed
- ✅ Server has full control over UI
- ✅ Easy to modify without deployments
- ✅ Consistent rendering across instances

**Cons:**
- ❌ Verbose JSON structure
- ❌ Runtime parsing overhead
- ❌ Limited to available DSL components
- ❌ Harder to implement complex interactions

### Client-Managed (React) Example

**Server sends component reference:**
```python
{
    "type": "client_widget",
    "name": "tool-approval-request",
    "args": {
        "title": "Approval Required",
        "description": "Do you want to proceed?",
        "tool_name": "processPayment",
        "call_id": "call_123"
    }
}
```

**Client React component:**
```tsx
export function ToolApprovalRequest({ args, itemId }: ClientWidgetProps) {
  const { title, description, tool_name, call_id } = args;
  const sendWidgetAction = useSendWidgetAction();

  const handleApprove = () => {
    sendWidgetAction(itemId, {
      type: "approval",
      payload: { approved: true, call_id },
    });
  };

  const handleReject = () => {
    sendWidgetAction(itemId, {
      type: "approval",
      payload: { approved: false, call_id },
    });
  };

  return (
    <Card className="p-4">
      <div className="flex flex-col items-center gap-2">
        <Info className="h-12 w-12 text-blue-500" />
        <h3 className="text-lg font-semibold">{title}</h3>
        <p className="text-sm text-muted-foreground">{description}</p>
      </div>
      <div className="flex gap-2 mt-4">
        <Button className="flex-1" onClick={handleApprove}>
          Approve
        </Button>
        <Button className="flex-1" variant="outline" onClick={handleReject}>
          Reject
        </Button>
      </div>
    </Card>
  );
}
```

**Pros:**
- ✅ Clean, readable React code
- ✅ Better performance (no runtime parsing)
- ✅ Full React ecosystem access
- ✅ Type-safe with TypeScript
- ✅ Easy to add complex interactions

**Cons:**
- ❌ Requires client deployment for changes
- ❌ Must be registered before use
- ❌ Less flexible for dynamic UIs
