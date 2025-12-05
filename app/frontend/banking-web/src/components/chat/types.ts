// ============================================================================
// Thread & Status
// ============================================================================
export type ThreadStatus =
  | { type: "active" }
  | { type: "locked"; reason?: string }
  | { type: "closed"; reason?: string };

export interface Thread {
  id: string;
  title: string | null;
  created_at: string;
  status: ThreadStatus;
  metadata?: Record<string, unknown>;
}

export interface ThreadListItem {
  id: string;
  title: string | null;
  created_at: string;
  status: ThreadStatus;
  metadata?: Record<string, unknown>;
  items: {
    data: ThreadItem[];
    has_more: boolean;
  };
}

export interface ThreadListResponse {
  data: ThreadListItem[];
  has_more: boolean;
  after?: string;
}

export interface ThreadDetailResponse {
  id: string;
  title: string | null;
  created_at: string;
  status: ThreadStatus;
  metadata?: Record<string, unknown>;
  items: {
    data: ThreadItem[];
    has_more: boolean;
    after?: string;
  };
}

// ============================================================================
// Attachments
// ============================================================================
export type Attachment = FileAttachment | ImageAttachment;

export interface FileAttachment {
  type: "file";
  id: string;
  name: string;
  mime_type: string;
  upload_url?: string | null;
}

export interface ImageAttachment {
  type: "image";
  id: string;
  name: string;
  mime_type: string;
  upload_url?: string | null;
  preview_url: string;
}

// For backwards compat with existing composer
export interface AttachmentMeta {
  id: string;
  name: string;
  size: number;
  mimeType: string;
  previewUrl?: string;
  uploadStatus?: "pending" | "uploading" | "uploaded" | "error";
  uploadProgress?: number;
  serverPreviewUrl?: string; // Preview URL from server after upload
}

// ============================================================================
// Message Content
// ============================================================================
export interface AssistantMessageContent {
  type: "output_text";
  text: string;
  annotations: Annotation[];
}

export type UserMessageContent =
  | { type: "input_text"; text: string }
  | {
      type: "input_tag";
      id: string;
      text: string;
      data: Record<string, unknown>;
      group: string | null;
      interactive: boolean;
    };

export interface Annotation {
  type: "annotation";
  source: URLSource | FileSource | EntitySource;
  index: number | null;
}

export interface URLSource {
  type: "url";
  title: string;
  url: string;
  description?: string | null;
  timestamp?: string | null;
  attribution?: string | null;
  group?: string | null;
}

export interface FileSource {
  type: "file";
  title: string;
  filename: string;
  description?: string | null;
  timestamp?: string | null;
  group?: string | null;
}

export interface EntitySource {
  type: "entity";
  id: string;
  title: string;
  icon?: string | null;
  data: Record<string, unknown>;
  description?: string | null;
  timestamp?: string | null;
  group?: string | null;
}

// ============================================================================
// Task Types
// ============================================================================
export interface BaseTask {
  status_indicator: "none" | "loading" | "complete";
}

export interface CustomTask extends BaseTask {
  type: "custom";
  title: string | null;
  icon?: string | null;
  content?: string | null;
}

export interface SearchTask extends BaseTask {
  type: "web_search";
  title: string | null;
  icon?: string | null;
  title_query?: string | null;
  queries: string[];
  sources: URLSource[];
}

export interface ThoughtTask extends BaseTask {
  type: "thought";
  title: string | null;
  icon?: string | null;
  content: string;
}

export interface FileTask extends BaseTask {
  type: "file";
  title: string | null;
  icon?: string | null;
  sources: FileSource[];
}

export interface ImageTask extends BaseTask {
  type: "image";
  title: string | null;
  icon?: string | null;
}

export type Task = CustomTask | SearchTask | ThoughtTask | FileTask | ImageTask;

// ============================================================================
// Workflow
// ============================================================================
export type WorkflowSummary =
  | { title: string; icon?: string | null }
  | { duration: number };

export interface Workflow {
  type: "custom" | "reasoning";
  tasks: Task[];
  summary: WorkflowSummary | null;
  expanded: boolean;
}

// ============================================================================
// Widget (simplified placeholder for now)
// ============================================================================
export interface WidgetRoot {
  key: string;
  type: string;
  children?: unknown[];
  [key: string]: unknown;
}

// ============================================================================
// Thread Items
// ============================================================================
export interface ThreadItemBase {
  id: string;
  thread_id: string;
  created_at: string;
}

export interface UserMessageItem extends ThreadItemBase {
  type: "user_message";
  content: UserMessageContent[];
  attachments: Attachment[];
  quoted_text?: string | null;
  inference_options?: unknown;
}

export interface AssistantMessageItem extends ThreadItemBase {
  type: "assistant_message";
  content: AssistantMessageContent[];
  streaming?: boolean;
}

export interface TaskItem extends ThreadItemBase {
  type: "task";
  task: Task;
}

export interface WorkflowItem extends ThreadItemBase {
  type: "workflow";
  workflow: Workflow;
}

export interface WidgetItem extends ThreadItemBase {
  type: "widget";
  widget: WidgetRoot; // Server-defined widget DSL
  copy_text?: string | null;
}

export interface ClientWidgetItem extends ThreadItemBase {
  type: "client_widget";
  name: string; // Component name registered in widget registry
  args?: Record<string, unknown>; // Arguments passed to client widget component
  copy_text?: string | null;
}

export interface ClientToolCallItem extends ThreadItemBase {
  type: "client_tool_call";
  status: "pending" | "completed";
  call_id: string;
  name: string;
  arguments: Record<string, unknown>;
  output?: unknown | null;
}

export interface EndOfTurnItem extends ThreadItemBase {
  type: "end_of_turn";
}

export interface ErrorItem extends ThreadItemBase {
  type: "error";
  code: string;
  message: string | null;
  allow_retry: boolean;
  http_status?: number; // Optional HTTP status code for HTTP errors
}

// Configuration for which HTTP status codes should allow retry
export interface RetryConfig {
  // Array of HTTP status codes that should allow retry
  retryableStatusCodes?: number[];
  // Default: [408, 429, 500, 502, 503, 504]
}

export type ThreadItem =
  | UserMessageItem
  | AssistantMessageItem
  | TaskItem
  | WorkflowItem
  | WidgetItem
  | ClientWidgetItem
  | ClientToolCallItem
  | EndOfTurnItem
  | ErrorItem;

// ============================================================================
// Starter Prompts
// ============================================================================
export interface StarterPrompt {
  id: string;
  title: string;
  description?: string;
  icon?: string;
  content: string;
}

// ============================================================================
// Composer Configuration
// ============================================================================
export interface ComposerConfig {
  placeholder?: string;
  buttonSize?: "sm" | "md" | "lg";
  showAttachmentCounter?: boolean;
  maxAttachments?: number;
  showAttachmentTitle?: boolean;
  showAttachmentSize?: boolean;
}

// ============================================================================
// Welcome Header Configuration
// ============================================================================
export interface WelcomeHeaderConfig {
  icon?: React.ReactNode;
  title?: string;
  subtitle?: string;
}

// ============================================================================
// Shell Container Configuration
// ============================================================================
export interface ShellContainerConfig {
  /** Show/hide the outer container border */
  showBorder?: boolean;
  /** Show/hide rounded corners */
  showRoundedCorners?: boolean;
  /** Show/hide shadow */
  showShadow?: boolean;
  /** Background color class (e.g., 'bg-background', 'bg-transparent') */
  backgroundColor?: string;
  /** Custom border class (e.g., 'border-border/60') */
  borderClass?: string;
  /** Custom rounded corner class (e.g., 'rounded-3xl') */
  roundedClass?: string;
  /** Custom shadow class (e.g., 'shadow-2xl') */
  shadowClass?: string;
}

// ============================================================================
// Chat Context
// ============================================================================
export interface ChatContextValue {
  threads: Thread[];
  activeThreadId: string | null;
  activeThread?: Thread;
  items: ThreadItem[];
  isStreaming: boolean;
  hasReceivedStreamEvent: boolean;
  historyOpen: boolean;
  starterPrompts: StarterPrompt[];
  chatServerUrl?: string;
  progressUpdate: { icon: string | null; text: string } | null;
  attachmentImageSize: "sm" | "md" | "lg";
  maxVisibleAttachments: number;
  composerConfig?: ComposerConfig;
  welcomeHeaderConfig?: WelcomeHeaderConfig;
  shellContainerConfig?: ShellContainerConfig;
  sendMessage: (text: string, attachments?: AttachmentMeta[]) => void;
  cancelStreaming: () => void;
  retryLastMessage: () => void;
  sendWidgetAction: (threadId: string, itemId: string, action: any) => void;
  createThread: (initialMessage?: string, options?: { title?: string }) => void;
  selectThread: (threadId: string) => void;
  toggleHistory: () => void;
  openHistory: () => void;
  closeHistory: () => void;
  getThreadItems: (threadId: string) => ThreadItem[];
  onAttachmentAdded?: (attachment: AttachmentMeta) => void;
  onAttachmentRemoved?: (attachmentId: string) => void;
  onThreadCreated?: (thread: Thread) => void;
  onThreadStarted?: (threadId: string) => void;
  onThreadItemAdded?: (item: ThreadItem) => void;
  onResponseEnd?: (threadId: string) => void;
  onMessageSent?: (message: { text: string; attachments?: AttachmentMeta[] }) => void;
  onError?: (error: { message: string; code?: string; threadId?: string }) => void;
}
