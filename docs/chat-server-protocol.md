# React Chat UI Technical Specification

## Overview

This document defines the technical specification for a React-based chat user interface that manages streamed events from a chat server. The system implements a real-time conversational interface capable of handling Server-Sent Events (SSE) and JSON responses from a single unified endpoint.

### Protocol Origins

This chat protocol is a fork of **OpenAI's Chatkit**, an open-source framework for building conversational AI interfaces. We extend and adapt the core Chatkit architecture to better support agent-framework from msft and ass support for client managed widgets and  multi-agent workflows.

**Credit**: The foundational event streaming model, thread management patterns, and API design are based on [OpenAI Chatkit](https://github.com/openai/chatkit-js). We are grateful to OpenAI for open-sourcing this protocol and enabling the community to build upon their excellent work.


## System Architecture

### Communication Protocol

#### Endpoint Configuration
- **Single Unified Endpoint**: All client-server communication flows through one endpoint
- **Transport Protocols**: 
  - JSON for non-streaming requests
  - Server-Sent Events (SSE) for real-time streaming updates
- **Request Method**: POST for all operations

#### Response Handling
The server's `respond` method supports multiple response types:
- Text messages with markdown formatting
- Progress updates during processing
- Tool invocations and results
- Rich display widgets
- Workflow visualization
- Error notifications
- Message retry management

## Event Types and Handlers

### 1. Thread Management Events

#### Thread Lifecycle Events

**`thread.created`**
```typescript
interface ThreadCreatedEvent {
  type: 'thread.created';
  thread: Thread;
}
```
- **Purpose**: Emitted when a new conversation thread is initialized
- **UI Action**: Create new thread container, initialize state
- **React State**: Add thread to threads list, set as active thread

**`thread.updated`**
```typescript
interface ThreadUpdatedEvent {
  type: 'thread.updated';
  thread: Thread;
}
```
- **Purpose**: Thread metadata changed (title, status, etc.)
- **UI Action**: Update thread display, refresh sidebar
- **React State**: Merge updated thread properties

**`thread.item.added`**
```typescript
interface ThreadItemAddedEvent {
  type: 'thread.item.added';
  item: ThreadItem;
}
```
- **Purpose**: New item (message, widget, workflow) added to thread
- **UI Action**: Append item to message list, scroll to bottom
- **React State**: Push item to thread items array

**`thread.item.updated`**
```typescript
interface ThreadItemUpdatedEvent {
  type: 'thread.item.updated';
  item_id: string;
  update: ThreadItemUpdate;
}
```
- **Purpose**: Incremental updates to existing thread items
- **UI Action**: Apply delta updates without full re-render
- **React State**: Update specific item properties by ID

**`thread.item.done`**
```typescript
interface ThreadItemDoneEvent {
  type: 'thread.item.done';
  item: ThreadItem;
}
```
- **Purpose**: Signals completion of a thread item
- **UI Action**: Remove loading indicators, finalize rendering
- **React State**: Mark item as complete, update status

**`thread.item.removed`**
```typescript
interface ThreadItemRemovedEvent {
  type: 'thread.item.removed';
  item_id: string;
}
```
- **Purpose**: Item deleted from thread
- **UI Action**: Animate removal, update layout
- **React State**: Filter out item from array

**`thread.item.replaced`**
```typescript
interface ThreadItemReplacedEvent {
  type: 'thread.item.replaced';
  item: ThreadItem;
}
```
- **Purpose**: Replace entire item (e.g., regeneration)
- **UI Action**: Swap item with transition
- **React State**: Replace item at same position

### 2. Message Content Events

#### Assistant Message Streaming

**`assistant_message.content_part.added`**
```typescript
interface AssistantMessageContentPartAdded {
  type: 'assistant_message.content_part.added';
  content_index: number;
  content: AssistantMessageContent;
}
```
- **Purpose**: New content block started in assistant message
- **UI Action**: Initialize new content container
- **React State**: Add content part to message structure

**`assistant_message.content_part.text_delta`**
```typescript
interface AssistantMessageContentPartTextDelta {
  type: 'assistant_message.content_part.text_delta';
  content_index: number;
  delta: string;
}
```
- **Purpose**: Incremental text streaming (token-by-token)
- **UI Action**: Append text to content part, typewriter effect
- **React State**: Concatenate delta to existing text
- **Performance**: Use debounced rendering for smooth updates

**`assistant_message.content_part.annotation_added`**
```typescript
interface AssistantMessageContentPartAnnotationAdded {
  type: 'assistant_message.content_part.annotation_added';
  content_index: number;
  annotation_index: number;
  annotation: Annotation;
}
```
- **Purpose**: Add citation/reference to message content
- **UI Action**: Display inline reference marker
- **React State**: Add annotation to content part

**`assistant_message.content_part.done`**
```typescript
interface AssistantMessageContentPartDone {
  type: 'assistant_message.content_part.done';
  content_index: number;
  content: AssistantMessageContent;
}
```
- **Purpose**: Content part finalized
- **UI Action**: Apply final formatting, enable interactions
- **React State**: Mark content part as complete

### 3. Widget Events

**`widget.root.updated`**
```typescript
interface WidgetRootUpdated {
  type: 'widget.root.updated';
  widget: WidgetRoot;
}
```
- **Purpose**: Widget structure changed
- **UI Action**: Re-render widget tree
- **React State**: Replace widget root

**`widget.component.updated`**
```typescript
interface WidgetComponentUpdated {
  type: 'widget.component.updated';
  component_id: string;
  component: WidgetComponent;
}
```
- **Purpose**: Individual widget component changed
- **UI Action**: Update specific component
- **React State**: Update component by ID in widget tree

**`widget.streaming_text.value_delta`**
```typescript
interface WidgetStreamingTextValueDelta {
  type: 'widget.streaming_text.value_delta';
  component_id: string;
  delta: string;
  done: boolean;
}
```
- **Purpose**: Stream text into widget component
- **UI Action**: Append text to widget field
- **React State**: Concatenate delta, mark done when complete

### 4. Workflow Events

**`workflow.task.added`**
```typescript
interface WorkflowTaskAdded {
  type: 'workflow.task.added';
  task_index: number;
  task: Task;
}
```
- **Purpose**: New step added to workflow visualization
- **UI Action**: Render new task card
- **React State**: Insert task at index

**`workflow.task.updated`**
```typescript
interface WorkflowTaskUpdated {
  type: 'workflow.task.updated';
  task_index: number;
  task: Task;
}
```
- **Purpose**: Task status/content changed
- **UI Action**: Update task display, change indicators
- **React State**: Update task at index

### 5. System Events

**`stream_options`**
```typescript
interface StreamOptionsEvent {
  type: 'stream_options';
  stream_options: StreamOptions;
}

interface StreamOptions {
  allow_cancel: boolean;
}
```
- **Purpose**: Configure stream behavior at runtime
- **UI Action**: Enable/disable cancel button
- **React State**: Update stream options

**`progress_update`**
```typescript
interface ProgressUpdateEvent {
  type: 'progress_update';
  icon: IconName | null;
  text: string;
}
```
- **Purpose**: Show intermediate processing status. They're a great way to give ephemeral feedback to users about what is happening without littering the chat thread unnecessarily.
- **UI Action**: Displays a non-persistent shimmer text title that will only display as long as it is the latest item in the thread. You can send multiple progress_update items and they will nicely rotate between each other
- **React State**: Update progress display

**`client_effect`**
```typescript
interface ClientEffectEvent {
  type: 'client_effect';
  name: string;
  data: Record<string, any>;
}
```
- **Purpose**: Trigger client-side actions (navigation, notifications)
- **UI Action**: Execute side effect (redirect, show toast, etc.)
- **React State**: Handle effect based on name

**`error`**
```typescript
interface ErrorEvent {
  type: 'error';
  code: ErrorCode | 'custom';
  message: string | null;
  allow_retry: boolean;
}
```
- **Purpose**: Notify of processing errors
- **UI Action**: Display error message, show retry button if allowed
- **React State**: Set error state, enable retry option

**`notice`**
```typescript
interface NoticeEvent {
  type: 'notice';
  level: 'info' | 'warning' | 'danger';
  message: string; // Supports markdown
  title: string | null;
}
```
- **Purpose**: Display user notifications
- **UI Action**: Show banner/toast with appropriate styling
- **React State**: Add to notifications queue

## Request Types

### Streaming Requests

These requests trigger SSE streaming responses:

**Create Thread**
```typescript
interface ThreadsCreateReq {
  type: 'threads.create';
  params: {
    input: UserMessageInput;
  };
  metadata?: Record<string, any>;
}
```

> **Note**: When creating a thread, the response is streamed via SSE. The stream includes the `thread.created` event followed by subsequent events as the assistant processes the initial message.

**Example Request:**
```json
{
  "type": "threads.create",
  "params": {
    "input": {
      "content": [
        {
          "type": "input_text",
          "text": "can you pay this bill for me"
        }
      ],
      "quoted_text": "",
      "attachments": ["atc_c02562d2"],
      "inference_options": {}
    }
  }
}
```

**Example Response Stream:**
```
data: {"type":"thread.created","thread":{"id":"thr_f470d530","created_at":"2025-11-27T16:55:21.898537","status":{"type":"active"},"metadata":{},"items":{"data":[],"has_more":false}}}

data: {"type":"thread.item.done","item":{"id":"msg_86628adc","thread_id":"thr_f470d530","created_at":"2025-11-27T16:55:21.899896","type":"user_message","content":[{"type":"input_text","text":"can you pay this bill for me"}],"attachments":[{"id":"atc_c02562d2","name":"gori.png","mime_type":"image/png","type":"image","preview_url":"https://ca-web-z576swbdi2iwk.greenwave-20f5c76e.francecentral.azurecontainerapps.io/preview/atc_c02562d2"}],"quoted_text":"","inference_options":{}}}

data: {"type":"stream_options","stream_options":{"allow_cancel":true}}

data: {"type":"progress_update","icon":"atom","text":"Processing your request ..."}

data: {"type":"thread.item.added","item":{"id":"call_4okrzGmgK8sTV1lBndLp61F1","thread_id":"thr_f470d530","created_at":"2025-11-27T16:55:24.459113","type":"task","task":{"status_indicator":"none","type":"custom","title":"Extracting data from the uploaded image...","icon":"search"}}}

data: {"type":"thread.item.added","item":{"id":"call_4okrzGmgK8sTV1lBndLp61F1","thread_id":"thr_f470d530","created_at":"2025-11-27T16:55:33.089565","type":"task","task":{"status_indicator":"none","type":"custom","title":"Data extracted from the uploaded image","icon":"check-circle-filled"}}}

data: {"type":"thread.item.added","item":{"id":"msg_e4ba1d6c","thread_id":"thr_f470d530","created_at":"2025-11-27T16:55:34.073562","type":"assistant_message","content":[{"annotations":[],"text":"I've","type":"output_text"}]}}

data: {"type":"thread.item.updated","item_id":"itm_02960f91","update":{"type":"assistant_message.content_part.text_delta","content_index":1,"delta":" extracted"}}

data: {"type":"thread.item.updated","item_id":"itm_858da320","update":{"type":"assistant_message.content_part.text_delta","content_index":2,"delta":" the"}}

data: {"type":"thread.item.updated","item_id":"itm_4f7c5809","update":{"type":"assistant_message.content_part.text_delta","content_index":3,"delta":" following"}}

data: {"type":"thread.item.done","item":{"id":"msg_e4ba1d6c","thread_id":"thr_f470d530","created_at":"2025-11-27T16:55:34.688740","type":"assistant_message","content":[{"annotations":[],"text":"I've extracted the following details from your bill:\n\n| Field           | Value                  |\n|-----------------|-----------------------|\n| Payee Name      | GORI                  |\n| Invoice Number  | 9524011000817857      |\n| Invoice Date    | 2024-05-08            |\n| Amount Due      | €85,20                |\n\nPlease confirm that these details are correct before I proceed. If any information is missing or incorrect, let me know. Once confirmed, I'll check your previous payments to ensure this bill hasn't already been paid.","type":"output_text"}]}}
```

**Add User Message**
```typescript
interface ThreadsAddUserMessageReq {
  type: 'threads.add_user_message';
  params: {
    input: UserMessageInput;
    thread_id: string;
  };
  metadata?: Record<string, any>;
}
```

**Example Request:**
```json
{
  "type": "threads.add_user_message",
  "params": {
    "input": {
      "content": [
        {
          "type": "input_text",
          "text": "yep they are"
        }
      ],
      "quoted_text": "",
      "attachments": [],
      "inference_options": {}
    },
    "thread_id": "thr_f470d530"
  }
}
```

**Example Response Stream:**
```
data: {"type":"thread.item.done","item":{"id":"msg_c680fff7","thread_id":"thr_f470d530","created_at":"2025-11-27T17:23:33.253488","type":"user_message","content":[{"type":"input_text","text":"yep they are"}],"attachments":[],"quoted_text":"","inference_options":{}}}

data: {"type":"progress_update","icon":"atom","text":"Processing your request ..."}

data: {"type":"thread.item.added","item":{"id":"call_5hzlr2NFljifxip0fznyPqAG","thread_id":"thr_f470d530","created_at":"2025-11-27T17:23:37.295380","type":"task","task":{"status_indicator":"none","type":"custom","title":"Looking up your account for your user name...","icon":"search"}}}

data: {"type":"thread.item.added","item":{"id":"call_YyoD6SAaIbLJwk2Z3YsObbJQ","thread_id":"thr_f470d530","created_at":"2025-11-27T17:23:37.298180","type":"task","task":{"status_indicator":"none","type":"custom","title":"Searching transactions for the recipient...","icon":"search"}}}

data: {"type":"thread.item.added","item":{"id":"msg_b8348cfd","thread_id":"thr_f470d530","created_at":"2025-11-27T17:23:40.819366","type":"assistant_message","content":[{"annotations":[],"text":"This","type":"output_text"}]}}

data: {"type":"thread.item.updated","item_id":"itm_e4bf155b","update":{"type":"assistant_message.content_part.text_delta","content_index":1,"delta":" bill"}}

data: {"type":"thread.item.updated","item_id":"itm_3933007a","update":{"type":"assistant_message.content_part.text_delta","content_index":2,"delta":" for"}}

data: {"type":"thread.item.updated","item_id":"itm_fe8ba4a6","update":{"type":"assistant_message.content_part.text_delta","content_index":3,"delta":" G"}}

data: {"type":"thread.item.updated","item_id":"itm_c1b13677","update":{"type":"assistant_message.content_part.text_delta","content_index":4,"delta":"ORI"}}

data: {"type":"thread.item.done","item":{"id":"msg_b8348cfd","thread_id":"thr_f470d530","created_at":"2025-11-27T17:23:41.443057","type":"assistant_message","content":[{"annotations":[],"text":"This bill for GORI (invoice number: 9524011000817857, amount: €85,20) has already been paid on 2025-11-25 using your Primary Platinum Visa credit card.\n\nHere are the payment details:\n\n| Date & Time         | Recipient | Invoice Number         | Amount   | Payment Method     | Status |\n|---------------------|-----------|------------------------|----------|--------------------|--------|\n| 2025-11-25 14:04:14 | GORI      | 9524011000817857       | €85,20   | Primary Platinum Visa | Paid   |\n\nNo further payment is required for this bill. If you need to pay a different bill, please upload it or provide the details.","type":"output_text"}]}}
```

**Add Tool Output**
```typescript
interface ThreadsAddClientToolOutputReq {
  type: 'threads.add_client_tool_output';
  params: {
    thread_id: string;
    result: any;
  };
  metadata?: Record<string, any>;
}
```

**Retry After Item**
```typescript
interface ThreadsRetryAfterItemReq {
  type: 'threads.retry_after_item';
  params: {
    thread_id: string;
    item_id: string;
  };
  metadata?: Record<string, any>;
}
```

**Custom Action**
```typescript
interface ThreadsCustomActionReq {
  type: 'threads.custom_action';
  params: {
    thread_id: string;
    item_id: string | null;
    action: Action<string, any>;
  };
  metadata?: Record<string, any>;
}
```
**Custom Action - Request Example**
```json
data: {"type":"progress_update","icon":"atom","text":"Processing your request ..."}
data: {"type":"thread.item.added","item":{"id":"msg_705ad562","thread_id":"thr_c56118de","created_at":"2025-11-27T18:20:43.344879","type":"assistant_message","content":[{"annotations":[],"text":"The","type":"output_text"}]}}
data: {"type":"thread.item.updated","item_id":"itm_c8891202","update":{"type":"assistant_message.content_part.text_delta","content_index":1,"delta":" payment"}}

data: {"type":"thread.item.updated","item_id":"itm_15b09cb7","update":{"type":"assistant_message.content_part.text_delta","content_index":2,"delta":" could"}}

data: {"type":"thread.item.updated","item_id":"itm_a5bcce20","update":{"type":"assistant_message.content_part.text_delta","content_index":3,"delta":" not"}}

data: {"type":"thread.item.updated","item_id":"itm_5faf3e0e","update":{"type":"assistant_message.content_part.text_delta","content_index":4,"delta":" be"}}

data: {"type":"thread.item.updated","item_id":"itm_7280aacb","update":{"type":"assistant_message.content_part.text_delta","content_index":5,"delta":" processed"}}

data: {"type":"thread.item.done","item":{"id":"msg_705ad562","thread_id":"thr_c56118de","created_at":"2025-11-27T18:20:43.518367","type":"assistant_message","content":[{"annotations":[],"text":"The payment could not be processed ","type":"output_text"}]}}
```
**Custom Action - Response Example**
```json
{}
```
### Non-Streaming Requests

These requests return immediate JSON responses:

**Get Thread by ID**
```typescript
interface ThreadsGetByIdReq {
  type: 'threads.get_by_id';
  params: {
    thread_id: string;
  };
  metadata?: Record<string, any>;
}
```

**Example Request:**
```json
{
  "type": "threads.get_by_id",
  "params": {
    "thread_id": "thr_12c3ba2d"
  }
}
```

**Example Response:**
```json
{
  "id": "thr_12c3ba2d",
  "created_at": "2025-11-27T16:44:46.180370",
  "status": {
    "type": "active"
  },
  "metadata": {},
  "items": {
    "data": [
      {
        "id": "msg_974428ee",
        "thread_id": "thr_12c3ba2d",
        "created_at": "2025-11-27T16:44:46.181719",
        "type": "user_message",
        "content": [
          {
            "type": "input_text",
            "text": "how much I have on my account"
          }
        ],
        "attachments": [],
        "quoted_text": "",
        "inference_options": {}
      },
      {
        "id": "msg_12aed261",
        "thread_id": "thr_12c3ba2d",
        "created_at": "2025-11-27T16:44:49.869394",
        "type": "assistant_message",
        "content": [
          {
            "annotations": [],
            "text": "Here are the details of your account:\n\n| Account Holder | Currency | Balance     |\n|----------------|----------|-------------|\n| Bob User       | EUR      | €10,000.00  |\n\nIf you need more information (transactions, payment methods, etc.), just let me know!",
            "type": "output_text"
          }
        ]
      }
    ],
    "has_more": false,
    "after": "msg_12aed261"
  }
}
```

**List Threads**
```typescript
interface ThreadsListReq {
  type: 'threads.list';
  params: {
    limit?: number;
    order?: 'asc' | 'desc';
    after?: string;
  };
  metadata?: Record<string, any>;
}
```

***Example Request:***
```json
{
  "type": "threads.list",
  "params": {
    "limit": 9999,
    "order": "desc"
  }
}
```

***Example Response:***
```json
{
  "data": [
    {
      "title": "can you pay this bill for me",
      "id": "thr_5a1bad4f",
      "created_at": "2025-11-27T14:23:46.724911",
      "status": {
        "type": "active"
      },
      "metadata": {},
      "items": {
        "data": [],
        "has_more": false
      }
    },
    {
      "title": "how much I have on my account",
      "id": "thr_aa16ec0c",
      "created_at": "2025-11-27T14:22:10.241056",
      "status": {
        "type": "active"
      },
      "metadata": {},
      "items": {
        "data": [],
        "has_more": false
      }
    }
  ],
  "has_more": false,
  "after": "thr_aa16ec0c"
}
```

**List Items**
```typescript
interface ItemsListReq {
  type: 'items.list';
  params: {
    thread_id: string;
    limit?: number;
    order?: 'asc' | 'desc';
    after?: string;
  };
  metadata?: Record<string, any>;
}
```

**Submit Feedback**
```typescript
interface ItemsFeedbackReq {
  type: 'items.feedback';
  params: {
    thread_id: string;
    item_ids: string[];
    kind: 'positive' | 'negative';
  };
  metadata?: Record<string, any>;
}
```

**Example Request:**
```json
{
  "type": "items.feedback",
  "params": {
    "thread_id": "thr_f470d530",
    "item_ids": [
      "call_5hzlr2NFljifxip0fznyPqAG",
      "call_YyoD6SAaIbLJwk2Z3YsObbJQ",
      "call_J24dZeyNay3hjc065ljEZfMj",
      "call_k5W5uhdTghzAPQVeyohZllVL",
      "msg_b8348cfd"
    ],
    "kind": "positive"
  }
}
```

**Example Response:**
```json
{}
```

**Create Attachment**
```typescript
interface AttachmentsCreateReq {
  type: 'attachments.create';
  params: {
    name: string;
    size: number;
    mime_type: string;
  };
  metadata?: Record<string, any>;
}
```

> **Note**: This request is triggered when the user clicks the attachment icon and selects a file. Attachment upload uses a **two-phase approach**:
> 1. **Phase 1**: The client sends the `attachments.create` request with file metadata
> 2. **Phase 2**: Upon receiving the response with `upload_url`, the client uploads the actual file bytes to that URL with multipart/form-data field so that the server can physically store the file
Furthermore, and additional call is made to preview_url to render thumbnails of an image attached to a user message
**Example Request:**
```json
{
  "type": "attachments.create",
  "params": {
    "name": "gori.png",
    "size": 377958,
    "mime_type": "image/png"
  }
}
```

**Example Response:**
```json
{
  "id": "atc_c02562d2",
  "name": "gori.png",
  "mime_type": "image/png",
  "upload_url": "https://ca-web-z576swbdi2iwk.greenwave-20f5c76e.francecentral.azurecontainerapps.io/upload/atc_c02562d2",
  "type": "image",
  "preview_url": "https://ca-web-z576swbdi2iwk.greenwave-20f5c76e.francecentral.azurecontainerapps.io/preview/atc_c02562d2"
}
```

**Delete Attachment**
```typescript
interface AttachmentsDeleteReq {
  type: 'attachments.delete';
  params: {
    attachment_id: string;
  };
  metadata?: Record<string, any>;
}
```

**Example Request:**
```json
{
  "type": "attachments.delete",
  "params": {
    "attachment_id": "atc_9b2b9b06"
  }
}
```

**Example Response:**
```json
{}
```

**Update Thread**
```typescript
interface ThreadsUpdateReq {
  type: 'threads.update';
  params: {
    thread_id: string;
    title: string;
  };
  metadata?: Record<string, any>;
}
```

**Delete Thread**
```typescript
interface ThreadsDeleteReq {
  type: 'threads.delete';
  params: {
    thread_id: string;
  };
  metadata?: Record<string, any>;
}
```

## Data Models

### Thread Structure

```typescript
interface Thread {
  id: string;
  title: string | null;
  created_at: string; // ISO 8601 datetime
  status: ThreadStatus;
  metadata: Record<string, any>;
  items: Page<ThreadItem>;
}

type ThreadStatus = 
  | { type: 'active' }
  | { type: 'locked'; reason?: string }
  | { type: 'closed'; reason?: string };

interface Page<T> {
  data: T[];
  has_more: boolean;
  after: string | null;
}
```

### Thread Items

```typescript
type ThreadItem =
  | UserMessageItem
  | AssistantMessageItem
  | ClientToolCallItem
  | WidgetItem
  | WorkflowItem
  | TaskItem
  | EndOfTurnItem;

interface UserMessageItem {
  type: 'user_message';
  id: string;
  thread_id: string;
  created_at: string;
  content: UserMessageContent[];
  attachments: Attachment[];
  quoted_text: string | null;
  inference_options: InferenceOptions;
}

interface AssistantMessageItem {
  type: 'assistant_message';
  id: string;
  thread_id: string;
  created_at: string;
  content: AssistantMessageContent[];
}

interface ClientToolCallItem {
  type: 'client_tool_call';
  id: string;
  thread_id: string;
  created_at: string;
  status: 'pending' | 'completed';
  call_id: string;
  name: string;
  arguments: Record<string, any>;
  output: any | null;
}

interface WidgetItem {
  type: 'widget';
  id: string;
  thread_id: string;
  created_at: string;
  widget: WidgetRoot;
  copy_text: string | null;
}


interface WorkflowItem {
  type: 'workflow';
  id: string;
  thread_id: string;
  created_at: string;
  workflow: Workflow;
}

interface TaskItem {
  type: 'task';
  id: string;
  thread_id: string;
  created_at: string;
  task: Task;
}

interface EndOfTurnItem {
  type: 'end_of_turn';
  id: string;
  thread_id: string;
  created_at: string;
}
```
**Example WidgetItem Response:**
```
data: {"type":"thread.item.done","item":{"id":"wdg_550b6350","thread_id":"thr_c56118de","created_at":"2025-11-27T18:11:44.870265","type":"widget","widget":{"key":"approval_request","type":"Card","children":[{"children":[{"children":[{"type":"Icon","name":"info","color":"white","size":"3xl"}],"padding":3.0,"radius":"full","background":"yellow-400","type":"Box"},{"children":[{"type":"Title","value":"Approval Required"},{"type":"Text","value":"This action requires your approval before proceeding.","color":"secondary"},{"type":"Markdown","value":"**processPayment**"}],"align":"center","gap":1,"type":"Col"}],"align":"center","gap":4,"padding":4.0,"type":"Col"},{"type":"Markdown","value":"```py\n{'account_id': '1010', 'amount': 103.25, 'description': 'payment for invoice 411417740', 'timestamp': '2025-11-27 18:11:41', 'recipient_name': 'Organizer', 'payment_type': 'CreditCard', 'card_id': '66666', 'status': 'paid', 'category': 'subscriptions'}\n```"},{"type":"Divider","spacing":2},{"children":[{"type":"Button","label":"Approve","onClickAction":{"type":"approval","payload":{"tool_name":"processPayment","tool_args":{"account_id":"1010","amount":103.25,"description":"payment for invoice 411417740","timestamp":"2025-11-27 18:11:41","recipient_name":"Organizer","payment_type":"CreditCard","card_id":"66666","status":"paid","category":"subscriptions"},"approved":true,"call_id":"call_DDg5KQ3pB2Exkc7WbMz41q5u","request_id":"call_DDg5KQ3pB2Exkc7WbMz41q5u"},"handler":"server","loadingBehavior":"auto"},"block":true},{"type":"Button","label":"No","onClickAction":{"type":"approval","payload":{"tool_name":"processPayment","tool_args":{"account_id":"1010","amount":103.25,"description":"payment for invoice 411417740","timestamp":"2025-11-27 18:11:41","recipient_name":"Organizer","payment_type":"CreditCard","card_id":"66666","status":"paid","category":"subscriptions"},"approved":false,"call_id":"call_DDg5KQ3pB2Exkc7WbMz41q5u","request_id":"call_DDg5KQ3pB2Exkc7WbMz41q5u"},"handler":"server","loadingBehavior":"auto"},"variant":"outline","block":true}],"type":"Row"}],"padding":0.0}}}
```

This example shows a widget displaying an approval request card with:
- An icon and title indicating approval is required
- Markdown content showing the payment details in a code block
- Two interactive buttons ("Approve" and "No") that trigger server-side actions
- The widget uses a Card layout with nested components (Box, Col, Row, Icon, Title, Text, Markdown, Divider, Button)
- Button actions include payload data for the `processPayment` tool with full transaction details

### Message Content

```typescript
interface AssistantMessageContent {
  type: 'output_text';
  text: string;
  annotations: Annotation[];
}

type UserMessageContent =
  | { type: 'input_text'; text: string }
  | { 
      type: 'input_tag';
      id: string;
      text: string;
      data: Record<string, any>;
      group: string | null;
      interactive: boolean;
    };

interface Annotation {
  type: 'annotation';
  source: URLSource | FileSource | EntitySource;
  index: number | null;
}

interface URLSource {
  type: 'url';
  title: string;
  url: string;
  description: string | null;
  timestamp: string | null;
  attribution: string | null;
  group: string | null;
}

interface FileSource {
  type: 'file';
  title: string;
  filename: string;
  description: string | null;
  timestamp: string | null;
  group: string | null;
}

interface EntitySource {
  type: 'entity';
  id: string;
  title: string;
  icon: IconName | null;
  data: Record<string, any>;
  description: string | null;
  timestamp: string | null;
  group: string | null;
}
```

### Attachments

```typescript
type Attachment =
  | FileAttachment
  | ImageAttachment;

interface FileAttachment {
  type: 'file';
  id: string;
  name: string;
  mime_type: string;
  upload_url: string | null;
}

interface ImageAttachment {
  type: 'image';
  id: string;
  name: string;
  mime_type: string;
  upload_url: string | null;
  preview_url: string;
}
```


```typescript
interface Workflow {
  type: 'custom' | 'reasoning';
  tasks: Task[];
  summary: WorkflowSummary | null;
  expanded: boolean;
}

type WorkflowSummary =
  | { title: string; icon: IconName | null }
  | { duration: number }; // seconds

type Task =
  | CustomTask
  | SearchTask
  | ThoughtTask
  | FileTask
  | ImageTask;

interface BaseTask {
  status_indicator: 'none' | 'loading' | 'complete';
}

interface CustomTask extends BaseTask {
  type: 'custom';
  title: string | null;
  icon: IconName | null;
  content: string | null;
}

interface SearchTask extends BaseTask {
  type: 'web_search';
  title: string | null;
  title_query: string | null;
  queries: string[];
  sources: URLSource[];
}

interface ThoughtTask extends BaseTask {
  type: 'thought';
  title: string | null;
  content: string;
}

interface FileTask extends BaseTask {
  type: 'file';
  title: string | null;
  sources: FileSource[];
}

interface ImageTask extends BaseTask {
  type: 'image';
  title: string | null;
}
```

### User Input

```typescript
interface UserMessageInput {
  content: UserMessageContent[];
  attachments: string[]; // Attachment IDs
  quoted_text: string | null;
  inference_options: InferenceOptions;
}

interface InferenceOptions {
  tool_choice: ToolChoice | null;
  model: string | null;
}

interface ToolChoice {
  id: string;
}
```
## Requirements

### User interface
1. **Attachment Management**: When a user selects an attachment to upload, a preview thumbnail should be generated and displayed in the chat input area using the file bytes local to the browser, before the actual upload occurs. The thumbnail has a "x" icon on top left to remove it. Multiple attachments can be selected and previewed before sending the message. when the message is sent attachments are shown along with the text message.if it's a image attachment an image preview is provided in the user sent message, otherwise if it's a file attachment a badge with file name + extension is shown. when multiple attachments are sent, they are shown in a collapsed view with a "+X more" badge that can be expanded to see all attachments.
2. **Thread Management**: Display create a new thread icon and thread history. Clicking the new thread icon creates a new thread and switches to it. Clicking the thread history icon replace the chat body with a list of past threads. User can select one of the past threads to load its history in the chat body.
3. **Starter prompts**: Display a list of starter prompts when there are no threads. Clicking a prompt creates a new thread with that prompt as the first user message. A starter prompt can have an icon on the left and  a tittle
4. **UI callbacks**: Provide UI callbacks for events like onMessageSent, onThreadCreated, onAttachmentAdded, onAttachmentRemoved, onError, onThreadDone etc.
5. **Resizable chat component**: The chat component should be resizable by dragging its edges or corners.
6. **Widget Rendering**: Render widget components within the chat thread, supporting various widget types (e.g., cards, buttons, images). Widgets should be interactive and support actions like button clicks. those are custom actions that get sent back to the server when clicked. see Custom Action request type for more details.
7. **Theming Support**: Support light and dark themes, with customizable colors and fonts.
8. **Metadata Handling**: Allow passing custom metadata with thread creation and message sending requests.
9. **Allow ghost user messages**: Allow sending an user message without displaying it in the chat thread. This is useful for system messages or background instructions. when this option is enabled, the user message is sent to the server but not rendered in the chat UI.
10. 

### Streaming Text Display

1. **Typewriter Effect**: Render text deltas smoothly without janky re-renders
2. **Debounced Updates**: Batch rapid deltas for performance
3. **Cursor Indicator**: Show blinking cursor during active streaming. send message icon changes to a stop icon when streaming is in progress and `allow_cancel` is true
4. **Markdown Parsing**: Parse markdown incrementally as text streams
5. **HTML parsing**: text content can be both markdown or html. Parse and render accordingly

### Error Handling

1. **Error Display**: Show inline error messages with appropriate styling
2. **Retry Mechanism**: Enable retry button when `allow_retry` is true
3. **Graceful Degradation**: Handle connection losses gracefully
4. **Timeout Handling**: Set reasonable timeouts for streaming responses

### Progress Indication

1. **task**: Displays a task text title, which will shimmer while it is the latest item in the thread. These titles are permanent members of the thread, and can use a custom icon, and can have optional expandable content (which is a markdown string). Use task for tool calls or arbitrary actions that you want to remain visible in the thread, and keep a record of. Tasks can initially be rendered as pending framing (e.g., "Fetching records...") and then updated to a past tense state (e.g., "Found 56 records).
2. **workflow**: If you are running a multi-step task, you can group tasks together into a workflow. Workflow has two different styles, based on whether you pass a summary immediately, or at the very end of the workflow. The workflow summary is shown in the collapsed state of the workflow, and can either be a title + icon, or a duration (in seconds) that the workflow took to complete. you can expand/collapse the workflow to see all the individual tasks inside it. Use workflow when you are running complex, multi-step tasks that would be help to group and display to the user. Lots of flexibility and power here to play around with.
3. **progress updates**: Show intermediate processing status. They're a great way to give ephemeral feedback to users about what is happening without littering the chat thread unnecessarily. Displays a non-persistent shimmer text title that will only display as long as it is the latest item in the thread. You can send multiple progress_update items and they will nicely rotate 
4. **Cancel Support**: Display cancel button when streaming can be cancelled


### Accessibility

1. **Keyboard Navigation**: Full keyboard support for all interactions
2. **Screen Reader Support**: ARIA labels and live regions for dynamic content
3. **Focus Management**: Proper focus handling during streaming updates
4. **High Contrast**: Support for high contrast mode

### Performance Optimization

1. **Virtual Scrolling**: Implement for long message threads
2. **Memoization**: Use React.memo for message components
3. **Lazy Loading**: Load thread history on demand
4. **Debouncing**: Debounce rapid streaming updates

## Security Considerations

1. **XSS Prevention**: Sanitize markdown and user input
2. **CORS Configuration**: Proper CORS headers for SSE
3. **Authentication**: Token-based auth for API requests
4. **Rate Limiting**: Client-side rate limiting for requests
5. **File Upload Validation**: Validate file types and sizes

## React Implementation Architecture
#### Implementation Progress
1. **Step 1 – Core Components**: Added the base chat surface (`app/frontend/banking-web/src/components/chat/*`). `ChatProvider` (demo data + streaming stub) wraps `ChatShell`, which composes `ShellHeader`, `ConversationPane`, `ProgressDock`, `StreamViewport`, and `Composer`. `CustomSupport.tsx` now renders this stack for live review.
2. **Step 2 – Thread Management**: Enabled the header controls per spec. `ShellHeader` now starts new conversations and toggles a dedicated `HistoryView`, which replaces the conversation pane when active. `HistoryView` lists all threads, highlights the active one, and offers starter prompts when the list is empty. `ChatProvider` manages thread creation, selection, and history state (including starter prompts and summary snippets).
3. **Step 3 – Attachments Management**: The composer now includes the plus-style attachment affordance to the left of the textarea (`Composer.tsx`). Users can add up to five local files, see inline cards (with remove controls), and send them together with or without text. Image uploads (PNG/JPEG/WebP/etc.) show live thumbnails in the composer and in-message attachment chips, while other formats fall back to the paperclip badge (with name + size). `ChatProvider` persists attachment metadata on each `ThreadItem`, allowing `StreamViewport` bubbles to render the same previews so stakeholders can exercise the entire upload flow before wiring it to the real backend endpoints.

### Stack Alignment
- **Runtime**: React 18 + Vite (same as `frontend/banking-web`) ensuring fast HMR and tree-shakable builds.
- **Styling**: Tailwind CSS with shadcn/ui component presets layered on Radix primitives for consistent theming, focus rings, and accessibility.
- **State/Data**: TanStack Query for imperative JSON requests, lightweight Zustand store (or React context) for real-time thread state, and `fetchEventSource` (SSE over POST) for streaming responses.
- **Utility Libraries**: `react-resizable-panels` for the draggable shell, `lucide-react` icon set, `sonner`/`@radix-ui/react-toast` for notices, and `@radix-ui/react-scroll-area` for virtualized panes.

### High-Level Topology
```
<ChatShell>
  ├─ <ShellHeader />            // new-thread button + thread-history toggle
  └─ <ShellBody>
    ├─ <HistoryView />       // replaces conversation pane when history icon active
      └─ <ConversationPane>    // default view when not showing history
        ├─ <ProgressDock /> // now renders before messages to surface tasks/progress
        ├─ <StreamViewport />
      └─ <ComposerDock />
        ├─ <AttachmentTray />
        └─ <Composer />
</ChatShell>
```
- `ChatShell` stays resizable using `ResizablePanelGroup`, but thread controls now live in `ShellHeader` (a shadcn `Toolbar` with Radix `Tooltip` for icons).
- The “history” icon toggles `HistoryView`, which temporarily replaces the conversation pane; selecting a thread hides history and rehydrates the stream.
- `ProgressDock` renders above the stream so users always see current tasks/progress updates before new tokens arrive. The SSE feed drives both the dock and the message list simultaneously—`progress_update`, `task`, and `workflow` events land in the dock while assistant messages, widgets, and other thread items land in the stream viewport.
- `StreamViewport` continues to render `ThreadItem` variants via shadcn `Card`, `Accordion`, `Tabs`, `Badge`, `Alert`, and Radix `Collapsible` for widgets/workflows.

### Data Flow
1. **Command Bus** (`useChatClient`): wraps POST calls to the unified endpoint. Exposes `createThread`, `addUserMessage`, `retryAfterItem`, `customAction`, etc., each returning a `Promise` resolved through TanStack Query mutations so UI callbacks can respond to success/error.
2. **Stream Service** (`useThreadStream(requestPayload)`): posts the exact JSON payload shown in the earlier request samples (keeping `thread_id` inside `params`) and consumes the SSE response via `fetchEventSource`, parsing discriminated unions defined in the Python event schema module into a normalized Zustand store keyed by `threadId`. The store shape mirrors the Pydantic models (threads, items, attachments, workflows, widgets) to keep React reconciliation minimal.
3. **Selectors**: UI components subscribe to derived selectors (e.g., `selectActiveThreadItems`, `selectPendingTasks`) so the same stream simultaneously hydrates both `ProgressDock` (tasks/workflows/progress updates) and `StreamViewport` (messages, widgets, tool calls) without redundant renders.
4. **Side Effects**: `client_effect`, `notice`, and `error` events fan out to Radix `Toast` providers and optional host callbacks (`onError`, `onNotice`).

### Core Hooks / Services
```ts
// chat/streams/useThreadStream.ts
import { useEffect } from 'react';
import { fetchEventSource } from '@microsoft/fetch-event-source';
import { useChatStore } from '../state/chatStore';

export function useThreadStream(request: ChatReq | null) {
  const upsertEvent = useChatStore((s) => s.upsertEvent);

  useEffect(() => {
    if (!request) return;
    const controller = new AbortController();

    fetchEventSource('/chatkit', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(request), // thread_id always lives inside request.params per protocol
      signal: controller.signal,
      onmessage: (event) => {
        const payload = JSON.parse(event.data) as ThreadStreamEvent;
        const threadId = request.params?.thread_id ?? payload.thread?.id;
        if (threadId) {
          upsertEvent(threadId, payload);
        }
      },
      onerror: (error) => {
        controller.abort();
        useChatStore.getState().setConnectionState('error');
        throw error;
      },
    });

    return () => controller.abort();
  }, [request, upsertEvent]);
}
```
- `request` is any of the streaming payloads shown earlier (e.g., `ThreadsCreateReq`, `ThreadsAddUserMessageReq`), keeping `thread_id` inside `params` exactly like the documented samples.
- `upsertEvent` routes each union member to specialized reducers (e.g., `handleAssistantDelta`, `handleWidgetUpdate`).
- Mutations for JSON-only operations share a centralized `apiClient` built on `fetch` with `AbortController`, so cancel actions (surfaced via `allow_cancel`) terminate the same POST body contract.

### shadcn + Radix Composition
- **Message bubbles**: shadcn `Card` + `Markdown` component (rehype plugins) + Radix `ContextMenu` for quick actions (copy, retry, feedback).
- **Attachments**: shadcn `Badge`, `AspectRatio`, and Radix `Dialog` for preview lightboxes. Collapsed multi-attachment badge uses `Popover` to reveal the remaining files.
- **Widgets**: map server `widget` schema to shadcn primitives (`Card`, `Button`, `Separator`, `Toggle`). Button clicks marshal `customAction` payloads back to the endpoint.
- **Workflows/tasks**: Radix `Accordion` with shadcn `Timeline` patterns to show expandable steps, icons from `lucide-react`, and shimmer animation via Tailwind for pending states.
- **Starter prompts**: shadcn `Button` variants with left-aligned `lucide-react` icons; clicking triggers `createThread` + `useThreadStream` bootstrap.

### Thread Creation & History UX
- `ShellHeader` hosts two primary icon buttons (e.g., `Plus`, `History`). Both are shadcn `Button` components styled as icon ghosts with Radix `Tooltip` labels.
- Clicking the **new-thread icon** immediately calls `createThread` (optionally prefilled via starter prompt). Once the API confirms, the conversation pane rehydrates with the fresh stream.
- Clicking the **history icon** swaps the `ConversationPane` with `HistoryView`. This view lists past threads using shadcn `Card` tiles inside a Radix `ScrollArea`, showing title, timestamp, last item preview, and metadata badges. Selecting a tile emits `onThreadSelected`, hides the history view, and reattaches the SSE stream for the chosen thread.
- Because history replaces the main body rather than existing in a sidebar, the layout stays compact and satisfies the requirement that the conversation pane is temporarily replaced whenever users browse history.

### Attachment & Upload Workflow
1. User selects files; `AttachmentTray` uses `FileReader` to show immediate previews (image `<img>` or file badge) using shadcn `HoverCard` for metadata and “×” removal via Radix `IconButton`.
2. For each file, post an `attachments.create` request (phase 1); on response, upload bytes to `upload_url` (Phase 2). While uploading, show progress ring using Radix `Progress`.
3. Once server confirms, store `attachment_id` in composer state;
4. when the user sends the message, `addUserMessage` includes all `attachment_id`s alongside text content.
5. `StreamViewport` renders attachments in-message usingpreview logic as the composer, ensuring consistency. Make the size of image atatchements in StreamViewpoprt externally configurable via props. In the view port, the size should be bigger than in the composer.


### UI Callbacks & Context
- Provide `ChatProvider` that accepts callbacks (`onThreadCreated`, `onMessageSent`, `onAttachmentAdded`, `onAttachmentRemoved`, `onError`, `onThreadDone`). The provider wires these callbacks into the mutation/stream reducers so host applications (e.g., other banking dashboards) can react without prop-drilling.
- Expose a `useChat()` hook returning high-level commands plus derived state (active thread, streaming flag, cancel handler) for leaf components.

### Theming, Accessibility, and Layout
- Theme tokens reuse `banking-web` Tailwind config (`brand`, `muted`, `card`). shadcn’s `ThemeProvider` + `next-themes` toggles dark/light while Radix primitives ensure contrast and focus states.
- Announce streaming updates via `aria-live="polite"` regions; blinking cursor implemented with Tailwind keyframes but disabled when OS prefers reduced motion.
- Keyboard shortcuts: `/` focuses the composer, `Esc` cancels streaming when `allow_cancel` is true, and arrow navigation moves between thread previews; Radix `RovingFocusGroup` aids compliance.
- Virtual scrolling leverages Radix `ScrollArea` + `react-virtual` to clip long histories without sacrificing accessibility.


