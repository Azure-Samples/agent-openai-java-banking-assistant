import { useState, useEffect, useRef } from "react";
import {
  AlertCircle,
  Atom,
  BookOpen,
  BookMarked,
  Beaker,
  Bug,
  Calendar,
  ChartBar,
  Check,
  CheckCircle,
  CircleCheckBig,
  ChevronLeft,
  ChevronRight,
  CircleHelp,
  Compass,
  PartyPopper,
  Box,
  FileText,
  MoreHorizontal,
  Circle,
  Globe,
  Key,
  Images,
  Info,
  LifeBuoy,
  Lightbulb,
  Mail,
  MapPin,
  Map,
  User,
  NotebookPen,
  NotebookText,
  File,
  Phone,
  Plus,
  UserCircle,
  IdCard,
  Star,
  Search,
  Sparkle,
  Sparkles,
  SquareCode,
  Image,
  Square,
  Briefcase,
  SlidersHorizontal,
  Award,
  Edit,
  PenLine,
  Pencil,
  Zap,
  Loader2,
  Paperclip,
  RefreshCw,
} from "lucide-react";

import { ScrollArea } from "@/components/ui/scroll-area";
import { Accordion, AccordionContent, AccordionItem, AccordionTrigger } from "@/components/ui/accordion";
import { Card } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { cn } from "@/lib/utils";
import { useChat } from "./ChatProvider";
import { Markdown } from "./Markdown";
import { useDebounce } from "./useDebounce";
import { useMemo } from "react";
import { WidgetRenderer } from "@/components/chat/widgets";
import type { ActionConfig, WidgetRoot } from "@/components/chat/widgets/types";
import type {
  AssistantMessageItem,
  Attachment,
  ClientToolCallItem,
  ClientWidgetItem,
  ErrorItem,
  TaskItem,
  ThreadItem,
  UserMessageItem,
  WidgetItem,
  WorkflowItem,
} from "./types";

// ============================================================================
// Utilities
// ============================================================================
const formatFileSize = (size: number) => {
  if (size >= 1_000_000) return `${(size / 1_000_000).toFixed(1)} MB`;
  if (size >= 1_000) return `${(size / 1_000).toFixed(1)} KB`;
  return `${size} B`;
};

const getIconForName = (iconName?: string | null): React.ReactNode => {
  const iconClass = "h-4 w-4";
  
  // Map icon names to Lucide components
  switch (iconName) {
    case "agent":
      return <UserCircle className={iconClass} />;
    case "analytics":
      return <ChartBar className={iconClass} />;
    case "atom":
      return <Atom className={iconClass} />;
    case "batch":
      return <Box className={iconClass} />;
    case "bolt":
      return <Zap className={iconClass} />;
    case "book-open":
      return <BookOpen className={iconClass} />;
    case "book-closed":
      return <BookMarked className={iconClass} />;
    case "book-clock":
      return <Calendar className={iconClass} />;
    case "bug":
      return <Bug className={iconClass} />;
    case "calendar":
      return <Calendar className={iconClass} />;
    case "chart":
      return <ChartBar className={iconClass} />;
    case "check":
      return <Check className={iconClass} />;
    case "check-circle":
      return <CheckCircle className={iconClass} />;
    case "check-circle-filled":
      return <CircleCheckBig className={iconClass} />;
    case "chevron-left":
      return <ChevronLeft className={iconClass} />;
    case "chevron-right":
      return <ChevronRight className={iconClass} />;
    case "circle-question":
      return <CircleHelp className={iconClass} />;
    case "compass":
      return <Compass className={iconClass} />;
    case "confetti":
      return <PartyPopper className={iconClass} />;
    case "cube":
      return <Box className={iconClass} />;
    case "document":
      return <FileText className={iconClass} />;
    case "dots-horizontal":
      return <MoreHorizontal className={iconClass} />;
    case "empty-circle":
      return <Circle className={iconClass} />;
    case "globe":
      return <Globe className={iconClass} />;
    case "keys":
      return <Key className={iconClass} />;
    case "lab":
      return <Beaker className={iconClass} />;
    case "images":
      return <Images className={iconClass} />;
    case "info":
      return <Info className={iconClass} />;
    case "lifesaver":
      return <LifeBuoy className={iconClass} />;
    case "lightbulb":
      return <Lightbulb className={iconClass} />;
    case "mail":
      return <Mail className={iconClass} />;
    case "map-pin":
      return <MapPin className={iconClass} />;
    case "maps":
      return <Map className={iconClass} />;
    case "name":
      return <IdCard className={iconClass} />;
    case "notebook":
      return <NotebookText className={iconClass} />;
    case "notebook-pencil":
      return <NotebookPen className={iconClass} />;
    case "page-blank":
      return <File className={iconClass} />;
    case "phone":
      return <Phone className={iconClass} />;
    case "plus":
      return <Plus className={iconClass} />;
    case "profile":
      return <User className={iconClass} />;
    case "profile-card":
      return <IdCard className={iconClass} />;
    case "star":
      return <Star className={iconClass} />;
    case "star-filled":
      return <Star className={cn(iconClass, "fill-current")} />;
    case "search":
      return <Search className={iconClass} />;
    case "sparkle":
      return <Sparkle className={iconClass} />;
    case "sparkle-double":
      return <Sparkles className={iconClass} />;
    case "square-code":
      return <SquareCode className={iconClass} />;
    case "square-image":
      return <Image className={iconClass} />;
    case "square-text":
      return <Square className={iconClass} />;
    case "suitcase":
      return <Briefcase className={iconClass} />;
    case "settings-slider":
      return <SlidersHorizontal className={iconClass} />;
    case "user":
      return <User className={iconClass} />;
    case "wreath":
      return <Award className={iconClass} />;
    case "write":
      return <Edit className={iconClass} />;
    case "write-alt":
      return <PenLine className={iconClass} />;
    case "write-alt2":
      return <Pencil className={iconClass} />;
    case "loader-2":
      return <Loader2 className={cn(iconClass, "animate-spin")} />;
    default:
      return iconName ? <span className="text-lg">{iconName}</span> : null;
  }
};

// ============================================================================
// Attachment Rendering
// ============================================================================
interface AttachmentListProps {
  attachments: Attachment[];
  isUser: boolean;
  imageSize?: "sm" | "md" | "lg";
  maxVisible?: number;
}

function AttachmentList({ attachments, isUser, imageSize = "md", maxVisible }: AttachmentListProps) {
  const [expanded, setExpanded] = useState(false);
  
  const visibleAttachments = maxVisible && !expanded ? attachments.slice(0, maxVisible) : attachments;
  const remainingCount = maxVisible ? attachments.length - maxVisible : 0;
  const showExpandButton = maxVisible && remainingCount > 0 && !expanded;

  // Image size classes
  const imageSizeClasses = {
    sm: "h-16 w-16",
    md: "h-32 w-32",
    lg: "h-48 w-48",
  };

  return (
    <div className="mt-3 flex flex-col gap-2">
      <div className={cn("flex flex-wrap gap-2", imageSize === "lg" && "gap-3")}>
        {visibleAttachments.map((attachment) => (
          <div
            key={attachment.id}
            className={cn(
              "flex items-center gap-3 rounded-2xl border border-dashed px-3 py-2 text-xs",
              isUser
                ? "border-primary-foreground/40 bg-primary-foreground/10 text-primary-foreground"
                : "border-border/70 bg-muted/40 text-foreground",
            )}
          >
            {attachment.type === "image" && attachment.preview_url ? (
              <div className={cn("overflow-hidden rounded-xl border border-border/40 bg-background", imageSizeClasses[imageSize])}>
                <img src={attachment.preview_url} alt={attachment.name} className="h-full w-full object-cover" />
              </div>
            ) : (
              <div className="flex items-center gap-2">
                <Paperclip className={cn("h-3.5 w-3.5", isUser ? "text-primary-foreground/70" : "text-muted-foreground")} />
                <div className="flex flex-col">
                  <span className="font-medium">{attachment.name}</span>
                </div>
              </div>
            )}
          </div>
        ))}
      </div>
      
      {showExpandButton && (
        <Button
          variant="ghost"
          size="sm"
          onClick={() => setExpanded(true)}
          className={cn(
            "self-start text-xs",
            isUser ? "text-primary-foreground/70 hover:text-primary-foreground" : "text-muted-foreground"
          )}
        >
          +{remainingCount} more
        </Button>
      )}
    </div>
  );
}

// ============================================================================
// User Message Renderer
// ============================================================================
interface UserMessageRendererProps {
  item: UserMessageItem;
  attachmentImageSize?: "sm" | "md" | "lg";
  maxVisibleAttachments?: number;
}

function UserMessageRenderer({ 
  item, 
  attachmentImageSize = "md",
  maxVisibleAttachments = 3,
}: UserMessageRendererProps) {
  const textContent = item.content.find((c) => c.type === "input_text");
  const text = textContent && textContent.type === "input_text" ? textContent.text : "";

  return (
    <div className="ml-auto max-w-xl animate-in fade-in-0 slide-in-from-bottom-2 duration-300">
      <div className="rounded-2xl border border-primary/20 bg-primary/10 px-4 py-3 text-sm text-foreground shadow-sm backdrop-blur-sm">
        {text && <p className="whitespace-pre-line leading-relaxed">{text}</p>}
        {!!item.attachments?.length && (
          <AttachmentList 
            attachments={item.attachments} 
            isUser 
            imageSize={attachmentImageSize}
            maxVisible={maxVisibleAttachments}
          />
        )}
      </div>
    </div>
  );
}

// ============================================================================
// Assistant Message Renderer with Typewriter Effect
// ============================================================================
function AssistantMessageRenderer({ item }: { item: AssistantMessageItem }) {
  const fullText = useMemo(() => item.content.map((c) => c.text).join(""), [item.content]);
  const isStreaming = item.streaming ?? false;
  
  // Use slower debounce (200ms) for more visible typewriter effect
  // This creates a noticeable delay that makes streaming obvious
  const displayText = useDebounce(fullText, isStreaming ? 200 : 0);

  return (
    <div className="w-full animate-in fade-in-0 slide-in-from-bottom-2 duration-300">
      <div className="relative">
        <Markdown content={displayText} />
        {isStreaming && (
          <span className="ml-1 inline-block h-4 w-[2px] animate-pulse bg-primary" />
        )}
      </div>
    </div>
  );
}

// ============================================================================
// Task Item Renderer
// ============================================================================
function TaskItemRenderer({ item, isLastTask }: { item: TaskItem; isLastTask: boolean }) {
  const { task } = item;
  const icon = getIconForName(task.icon);
  const isLoading = task.status_indicator === "loading" || task.status_indicator === "none";
  const isComplete = task.status_indicator === "complete";
  
  // Apply shimmer only if this is the last task in the list and it's loading (or none, which means in progress)
  const shouldShimmer = isLastTask && isLoading;
  
  // Debug logging
  useEffect(() => {
    console.log(`Task "${task.title}" - status: ${task.status_indicator}, isLastTask: ${isLastTask}, isLoading: ${isLoading}, shouldShimmer: ${shouldShimmer}`);
  }, [isLastTask, isLoading, shouldShimmer, task.title, task.status_indicator]);

  return (
    <Card
      className={cn(
        "flex items-center gap-3 border-none px-4 py-1.5 text-sm shadow-none animate-in fade-in-0 slide-in-from-bottom-2 duration-300",
        isComplete ? "bg-secondary/20 text-secondary-foreground" : "border-border/70 bg-background",
      )}
    >
      {icon && <span aria-hidden>{icon}</span>}
      <div className="flex-1">
        <p className={cn("font-medium leading-tight", shouldShimmer && "animate-shimmer")}>{task.title}</p>
        {task.type === "custom" && task.content && <p className="text-xs text-muted-foreground">{task.content}</p>}
      </div>
      {isComplete && <CheckCircle className="h-4 w-4 text-green-600" />}
    </Card>
  );
}

// ============================================================================
// Workflow Item Renderer
// ============================================================================
function WorkflowItemRenderer({ item }: { item: WorkflowItem }) {
  const { workflow } = item;
  const summaryText =
    workflow.summary && "title" in workflow.summary
      ? workflow.summary.title
      : workflow.summary && "duration" in workflow.summary
        ? `Completed in ${workflow.summary.duration}s`
        : "Workflow";

  return (
    <Card className="border-border/70 bg-card px-4 py-3 text-sm shadow-sm animate-in fade-in-0 slide-in-from-bottom-2 duration-300">
      <Accordion type="single" collapsible defaultValue={workflow.expanded ? "workflow" : undefined}>
        <AccordionItem value="workflow" className="border-none">
          <AccordionTrigger className="py-2 text-sm font-medium hover:no-underline">{summaryText}</AccordionTrigger>
          <AccordionContent>
            <div className="space-y-2">
              {workflow.tasks.map((task, idx) => {
                const taskIcon = getIconForName(task.icon);
                return (
                  <div key={idx} className="flex items-start gap-3 rounded-lg border border-border/50 bg-muted/30 px-3 py-2 text-xs">
                    {taskIcon && <span className="mt-0.5 flex-shrink-0">{taskIcon}</span>}
                    <div className="flex-1">
                      <p className="font-medium">{task.title}</p>
                      {task.type === "custom" && task.content && <p className="mt-1 text-muted-foreground">{task.content}</p>}
                    </div>
                    {task.status_indicator === "complete" && <CheckCircle className="h-3.5 w-3.5 text-green-600" />}
                  </div>
                );
              })}
            </div>
          </AccordionContent>
        </AccordionItem>
      </Accordion>
    </Card>
  );
}

// ============================================================================
// Server Widget Item Renderer
// ============================================================================
function ServerWidgetRenderer({ item }: { item: WidgetItem }) {
  const { sendWidgetAction } = useChat();
  
  // Handle widget actions - this will trigger a new streaming response from the server
  const handleAction = (action: ActionConfig, itemId?: string) => {
    const id = itemId || item.id;
    console.log("Widget action triggered:", action.type, id);
    sendWidgetAction(item.thread_id, id, action);
  };
  
  // Get size constraint based on widget root size property
  const getWidgetSizeClass = () => {
    const widget = item.widget as { size?: string };
    const size = widget?.size || "md"; // Default to "md" if not specified
    
    const sizeMap: Record<string, string> = {
      sm: "max-w-sm", // 384px
      md: "max-w-md", // 448px
      lg: "max-w-lg", // 512px
      full: "w-full",
    };
    
    return sizeMap[size] || "max-w-md";
  };
  
  return (
    <div className={getWidgetSizeClass()}>
      <WidgetRenderer
        widget={item.widget as WidgetRoot}
        itemId={item.id}
        onAction={handleAction}
      />
    </div>
  );
}

// ============================================================================
// Client Widget Item Renderer
// ============================================================================
import { widgetRegistry } from "@/components/chat/widgets/WidgetRegistry";

function ClientWidgetRenderer({ item }: { item: ClientWidgetItem }) {
  // Import and render the client widget
  const ClientWidget = widgetRegistry.get(item.name);
  
  if (!ClientWidget) {
    console.error(`Client widget '${item.name}' not found in registry`);
    return (
      <div className="text-sm text-destructive">
        Error: Widget component '{item.name}' not registered
      </div>
    );
  }
  
  // Get size constraint - for client widgets, default to md
  const getWidgetSizeClass = () => {
    // Client widgets can use standard sizing
    return "max-w-md";
  };
  
  // Client widgets handle their own actions via useSendWidgetAction hook
  return (
    <div className={getWidgetSizeClass()}>
      <ClientWidget
        args={item.args || {}}
        itemId={item.id}
      />
    </div>
  );
}

// ============================================================================
// Client Tool Call Renderer
// ============================================================================
function ClientToolCallRenderer({ item }: { item: ClientToolCallItem }) {
  return (
    <Card className="border-border/70 bg-muted/40 px-4 py-3 text-sm shadow-sm animate-in fade-in-0 slide-in-from-bottom-2 duration-300">
      <div className="flex items-center justify-between">
        <div>
          <p className="font-medium">Tool: {item.name}</p>
          <p className="text-xs text-muted-foreground">Call ID: {item.call_id}</p>
        </div>
        <Badge variant={item.status === "completed" ? "default" : "outline"}>{item.status}</Badge>
      </div>
    </Card>
  );
}

// ============================================================================
// Error Item Renderer
// ============================================================================
function ErrorItemRenderer({ item }: { item: ErrorItem }) {
  const { retryLastMessage } = useChat();

  return (
    <Card className="border-destructive/50 bg-destructive/5 px-4 py-3 shadow-sm animate-in fade-in-0 slide-in-from-bottom-2 duration-300">
      <div className="flex items-start gap-3">
        <AlertCircle className="h-5 w-5 flex-shrink-0 text-destructive" />
        <div className="flex-1">
          <p className="font-medium text-destructive">Error</p>
          {item.message && <p className="mt-1 text-sm text-foreground">{item.message}</p>}
          <p className="mt-1 text-xs text-muted-foreground">
            {item.http_status ? `HTTP ${item.http_status}` : `Code: ${item.code}`}
          </p>
        </div>
        {item.allow_retry && (
          <Button
            variant="outline"
            size="sm"
            onClick={retryLastMessage}
            className="flex-shrink-0 gap-2"
          >
            <RefreshCw className="h-3.5 w-3.5" />
            Retry
          </Button>
        )}
      </div>
    </Card>
  );
}

// ============================================================================
// Progress Update (Ephemeral)
// ============================================================================
function ProgressUpdate({ icon, text }: { icon: string | null; text: string }) {
  const iconComponent = icon ? getIconForName(icon) : null;
  
  return (
    <div className="flex items-center gap-3 animate-in fade-in-0 slide-in-from-bottom-2 duration-300">
      {iconComponent && <span className="animate-pulse flex-shrink-0">{iconComponent}</span>}
      <p className="animate-pulse text-sm text-muted-foreground">{text}</p>
    </div>
  );
}

// ============================================================================
// Starter Prompts
// ============================================================================
function StarterPrompts() {
  const { starterPrompts, sendMessage, welcomeHeaderConfig } = useChat();

  const handlePromptClick = (content: string) => {
    sendMessage(content);
  };

  return (
    <div className="mx-auto flex w-full max-w-2xl flex-col gap-6 py-12 animate-in fade-in-0 slide-in-from-bottom-4 duration-500">
      {welcomeHeaderConfig && (
        <div className="flex flex-col items-center gap-3 text-center">
          {welcomeHeaderConfig.icon && (
            <div className="flex h-16 w-16 items-center justify-center rounded-2xl bg-gradient-to-br from-primary/20 to-primary/5 backdrop-blur-sm">
              {welcomeHeaderConfig.icon}
            </div>
          )}
          {(welcomeHeaderConfig.title || welcomeHeaderConfig.subtitle) && (
            <div>
              {welcomeHeaderConfig.title && (
                <h2 className="text-xl font-semibold text-foreground">{welcomeHeaderConfig.title}</h2>
              )}
              {welcomeHeaderConfig.subtitle && (
                <p className="mt-1 text-sm text-muted-foreground">{welcomeHeaderConfig.subtitle}</p>
              )}
            </div>
          )}
        </div>
      )}

      <div className="grid gap-3 sm:grid-cols-1">
        {starterPrompts.map((prompt) => (
          <button
            key={prompt.id}
            onClick={() => handlePromptClick(prompt.content)}
            className="group flex items-start gap-4 rounded-2xl border border-border/50 bg-card p-4 text-left shadow-sm transition-all hover:border-primary/40 hover:bg-accent hover:shadow-md active:scale-[0.98]"
          >
            <div className="flex h-12 w-12 flex-shrink-0 items-center justify-center rounded-xl bg-primary/10 text-2xl transition-colors group-hover:bg-primary/15">
              {prompt.icon}
            </div>
            <div className="flex-1">
              <h3 className="font-medium text-foreground">{prompt.title}</h3>
              <p className="mt-0.5 text-sm text-muted-foreground">{prompt.description}</p>
            </div>
          </button>
        ))}
      </div>
    </div>
  );
}

// ============================================================================
// Item Renderer (Router)
// ============================================================================
interface ItemRendererProps {
  item: ThreadItem;
  attachmentImageSize?: "sm" | "md" | "lg";
  maxVisibleAttachments?: number;
  isLastTask?: boolean;
}

function ItemRenderer({ 
  item, 
  attachmentImageSize = "md",
  maxVisibleAttachments = 3,
  isLastTask = false,
}: ItemRendererProps) {
  switch (item.type) {
    case "user_message":
      return (
        <UserMessageRenderer 
          item={item} 
          attachmentImageSize={attachmentImageSize}
          maxVisibleAttachments={maxVisibleAttachments}
        />
      );
    case "assistant_message":
      return <AssistantMessageRenderer item={item} />;
    case "task":
      return <TaskItemRenderer item={item} isLastTask={isLastTask} />;
    case "workflow":
      return <WorkflowItemRenderer item={item} />;
    case "widget":
      return <ServerWidgetRenderer item={item} />;
    case "client_widget":
      return <ClientWidgetRenderer item={item} />;
    case "client_tool_call":
      return <ClientToolCallRenderer item={item} />;
    case "error":
      return <ErrorItemRenderer item={item} />;
    case "end_of_turn":
      return null;
    default:
      return null;
  }
}

// ============================================================================
// Loading Indicator
// ============================================================================
function LoadingIndicator() {
  return (
    <div className="flex items-center gap-1 animate-in fade-in-0 slide-in-from-bottom-2 duration-300">
      <span className="h-2 w-2 rounded-full bg-primary/60 animate-pulse" style={{ animationDelay: "0ms" }} />
      <span className="h-2 w-2 rounded-full bg-primary/60 animate-pulse" style={{ animationDelay: "150ms" }} />
      <span className="h-2 w-2 rounded-full bg-primary/60 animate-pulse" style={{ animationDelay: "300ms" }} />
    </div>
  );
}

// ============================================================================
// StreamViewport
// ============================================================================
interface StreamViewportProps {
  attachmentImageSize?: "sm" | "md" | "lg";
  maxVisibleAttachments?: number;
}

export function StreamViewport({ 
  attachmentImageSize = "md",
  maxVisibleAttachments = 3,
}: StreamViewportProps) {
  const { items, progressUpdate, isStreaming, hasReceivedStreamEvent } = useChat();

  // Track task IDs that have been replaced (to prevent shimmer on replaced tasks)
  const [replacedTaskIds, setReplacedTaskIds] = useState<Set<string>>(new Set());
  const prevTaskIdsRef = useRef<Set<string>>(new Set());

  // Detect when tasks are replaced (same ID appears again)
  useEffect(() => {
    const currentTaskIds = new Set<string>();
    items.forEach(item => {
      if (item.type === "task") {
        currentTaskIds.add(item.id);
        // If this task ID existed before, it's a replacement
        if (prevTaskIdsRef.current.has(item.id)) {
          setReplacedTaskIds(prev => new Set(prev).add(item.id));
          console.log(`Task ${item.id} was replaced`);
        }
      }
    });
    prevTaskIdsRef.current = currentTaskIds;
  }, [items]);

  // Show loading indicator only when streaming but no events received yet (and no progress update)
  const showLoading = isStreaming && !hasReceivedStreamEvent && !progressUpdate;

  // Show starter prompts only if there are no items AND we're not streaming and haven't received any events
  const showStarterPrompts = items.length === 0 && !isStreaming && !hasReceivedStreamEvent && !progressUpdate;

  // Find the index of the last task item to apply shimmer effect
  const lastTaskIndex = useMemo(() => {
    for (let i = items.length - 1; i >= 0; i--) {
      if (items[i].type === "task") {
        console.log(`Last task found at index ${i}:`, items[i]);
        return i;
      }
    }
    console.log("No task items found in list");
    return -1;
  }, [items]);

  return (
    <ScrollArea className="h-full w-full">
      <div className="flex min-h-full flex-col gap-1 px-4 py-6 pb-24">
        {showStarterPrompts ? (
          <StarterPrompts />
        ) : (
          <>
            {items.map((item, index) => {
              // Determine if this is the last task for shimmer effect
              // Don't shimmer if the task was replaced (same ID arrived again)
              const wasReplaced = item.type === "task" && replacedTaskIds.has(item.id);
              const isLastTask = item.type === "task" && index === lastTaskIndex && !wasReplaced;
              
              return (
                <ItemRenderer 
                  key={item.id} 
                  item={item} 
                  attachmentImageSize={attachmentImageSize}
                  maxVisibleAttachments={maxVisibleAttachments}
                  isLastTask={isLastTask}
                />
              );
            })}
            {progressUpdate && <ProgressUpdate icon={progressUpdate.icon} text={progressUpdate.text} />}
            {showLoading && <LoadingIndicator />}
          </>
        )}
      </div>
    </ScrollArea>
  );
}
