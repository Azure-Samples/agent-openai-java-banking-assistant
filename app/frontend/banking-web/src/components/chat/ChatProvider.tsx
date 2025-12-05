import { createContext, useCallback, useContext, useEffect, useMemo, useRef, useState } from "react";
import type { AttachmentMeta, ChatContextValue, StarterPrompt, Thread, ThreadItem, ThreadListResponse, ThreadDetailResponse, RetryConfig, ComposerConfig, WelcomeHeaderConfig, ShellContainerConfig } from "./types";
import { useThreadStream, type StreamEvent } from "./useThreadStream";
import { widgetRegistry, type ClientWidgetComponent } from "@/components/chat/widgets";

const ChatContext = createContext<ChatContextValue | undefined>(undefined);

const generateId = (prefix: string) => `${prefix}_${Math.random().toString(36).slice(2, 10)}`;

const now = () => new Date().toISOString();

interface ChatProviderProps {
  children: React.ReactNode;
  starterPrompts?: StarterPrompt[];
  chatServerUrl?: string;
  threadListLimit?: number;
  threadListOrder?: "asc" | "desc";
  retryConfig?: RetryConfig;
  attachmentImageSize?: "sm" | "md" | "lg";
  maxVisibleAttachments?: number;
  composerConfig?: ComposerConfig;
  customWidgets?: Record<string, ClientWidgetComponent>; // Custom widget components to register
  welcomeHeaderConfig?: WelcomeHeaderConfig;
  shellContainerConfig?: ShellContainerConfig;
  onAttachmentAdded?: (attachment: AttachmentMeta) => void;
  onAttachmentRemoved?: (attachmentId: string) => void;
  onThreadCreated?: (thread: Thread) => void;
  onThreadStarted?: (threadId: string) => void;
  onThreadItemAdded?: (item: ThreadItem) => void;
  onResponseEnd?: (threadId: string) => void;
  onMessageSent?: (message: { text: string; attachments?: AttachmentMeta[] }) => void;
  onError?: (error: { message: string; code?: string; threadId?: string }) => void;
}

export function ChatProvider({ 
  children, 
  starterPrompts = [], 
  chatServerUrl,
  threadListLimit = 9999,
  threadListOrder = "desc",
  retryConfig,
  attachmentImageSize = "lg",
  maxVisibleAttachments = 3,
  composerConfig,
  customWidgets,
  welcomeHeaderConfig,
  shellContainerConfig,
  onAttachmentAdded,
  onAttachmentRemoved,
  onThreadCreated,
  onThreadStarted,
  onThreadItemAdded,
  onResponseEnd,
  onMessageSent,
  onError,
}: ChatProviderProps) {
  const [threads, setThreads] = useState<Thread[]>([]);
  const [activeThreadId, setActiveThreadId] = useState<string | null>(null);
  const [threadItems, setThreadItems] = useState<Record<string, ThreadItem[]>>({});
  const [isStreaming, setIsStreaming] = useState(false);
  const [hasReceivedStreamEvent, setHasReceivedStreamEvent] = useState(false);
  const [historyOpen, setHistoryOpen] = useState(false);
  const [currentRequest, setCurrentRequest] = useState<any>(null);
  const [progressUpdate, setProgressUpdate] = useState<{ icon: string | null; text: string } | null>(null);
  const streamingAssistantRef = useRef<{ threadId: string; itemId: string; contentIndex: number } | null>(null);
  const lastMessageRef = useRef<{ text: string; attachments?: AttachmentMeta[] } | null>(null);
  const streamingThreadRef = useRef<string | null>(null); // Track which thread is currently streaming

  // Register custom widgets on mount
  useEffect(() => {
    if (customWidgets) {
      Object.entries(customWidgets).forEach(([name, component]) => {
        widgetRegistry.register(name, component);
      });
    }
  }, [customWidgets]);

  const activeThread = useMemo(() => threads.find((thread) => thread.id === activeThreadId), [threads, activeThreadId]);
  const items = useMemo(() => {
    const result = activeThreadId ? threadItems[activeThreadId] ?? [] : [];
    return result;
  }, [threadItems, activeThreadId]);

  // Handle SSE events
  const handleStreamEvent = useCallback((event: StreamEvent) => {
    console.log("SSE Event:", event);

    switch (event.type) {
      case "thread.created": {
        const thread = event.thread as Thread;
        setThreads((prev) => [thread, ...prev.filter((t) => t.id !== thread.id)]);
        setActiveThreadId(thread.id);
        setThreadItems((prev) => ({ ...prev, [thread.id]: [] }));
        // Track this as the streaming thread
        streamingThreadRef.current = thread.id;
        
        // Notify callbacks
        if (onThreadCreated) {
          onThreadCreated(thread);
        }
        if (onThreadStarted) {
          onThreadStarted(thread.id);
        }
        break;
      }

      case "thread.updated": {
        const thread = event.thread as Thread;
        setThreads((prev) => 
          prev.map((t) => (t.id === thread.id ? { ...t, title: thread.title } : t))
        );
        break;
      }

      case "thread.item.done": {
        const item = event.item as ThreadItem;
        
        // For assistant messages, replace the streaming item with the final one
        if (item.type === "assistant_message") {
          setThreadItems((prev) => {
            const existingItems = prev[item.thread_id] ?? [];
            const itemIndex = existingItems.findIndex((i) => i.id === item.id);
            
            if (itemIndex !== -1) {
              // Replace the streaming item with the final complete item (streaming: false)
              const updatedItems = [...existingItems];
              updatedItems[itemIndex] = { ...item, streaming: false };
              return {
                ...prev,
                [item.thread_id]: updatedItems,
              };
            }
            
            // If not found (shouldn't happen), append it
            return {
              ...prev,
              [item.thread_id]: [...existingItems, { ...item, streaming: false }],
            };
          });
          
          // Clear streaming reference
          streamingAssistantRef.current = null;
        } else {
          // For other item types, just append
          setThreadItems((prev) => ({
            ...prev,
            [item.thread_id]: [...(prev[item.thread_id] ?? []), item],
          }));
        }
        break;
      }

      case "thread.item.added": {
        const item = event.item as ThreadItem;
        
        // Mark that we've received a content event
        setHasReceivedStreamEvent(true);
        
        if (item.type === "assistant_message") {
          // Initialize streaming assistant message
          streamingAssistantRef.current = {
            threadId: item.thread_id,
            itemId: item.id,
            contentIndex: 0,
          };
        }

        setThreadItems((prev) => {
          const existingItems = prev[item.thread_id] ?? [];
          
          // For task items with the same ID, replace the existing one
          // This allows tasks to update from pending (e.g., "Fetching records...") 
          // to completed state (e.g., "Found 56 records")
          if (item.type === "task") {
            const existingIndex = existingItems.findIndex((i) => i.id === item.id);
            if (existingIndex !== -1) {
              const updatedItems = [...existingItems];
              updatedItems[existingIndex] = item;
              return {
                ...prev,
                [item.thread_id]: updatedItems,
              };
            }
          }
          
          // For assistant messages, mark as streaming
          const itemToAdd = item.type === "assistant_message" 
            ? { ...item, streaming: true }
            : item;
          
          // For all other items (or new tasks), append to the list
          return {
            ...prev,
            [item.thread_id]: [...existingItems, itemToAdd],
          };
        });
        
        // Clear progress update when a real item is added
        setProgressUpdate(null);
        
        // Notify callback
        if (onThreadItemAdded) {
          onThreadItemAdded(item);
        }
        break;
      }

      case "thread.item.updated": {
        const { item_id, update } = event;
        
        if (update.type === "assistant_message.content_part.text_delta") {
          // Append delta to the first (and only) content part
          // Note: content_index in the delta is just a counter, not an array index
          setThreadItems((prev) => {
            const streaming = streamingAssistantRef.current;
            if (!streaming) return prev;

            const items = prev[streaming.threadId] ?? [];
            return {
              ...prev,
              [streaming.threadId]: items.map((item) => {
                if (item.id === streaming.itemId && item.type === "assistant_message") {
                  const content = [...item.content];
                  // Always append to content[0] - the first content part
                  if (content[0]) {
                    content[0] = {
                      ...content[0],
                      text: content[0].text + update.delta,
                    };
                  }
                  return { ...item, content };
                }
                return item;
              }),
            };
          });
        }
        break;
      }

      case "progress_update": {
        console.log("Progress update event:", event);
        setHasReceivedStreamEvent(true);
        setProgressUpdate({ icon: event.icon, text: event.text });
        break;
      }

      case "stream_options": {
        // Stream options like allow_cancel
        console.log("Stream options:", event.stream_options);
        break;
      }

      case "error": {
        console.error("Stream error:", event.message);
       
        // Mark that we've received a stream event so the error is shown
        setHasReceivedStreamEvent(true);
        
        // Use the thread that's currently streaming, not the active thread
        // The active thread might have changed if the user clicked elsewhere
        let threadId = streamingThreadRef.current || activeThreadId;
        
        // If we still don't have a thread ID and have a current request
        if (!threadId && currentRequest) {
          // For threads.add_user_message, extract from params
          if (currentRequest.type === "threads.add_user_message") {
            threadId = currentRequest.params.thread_id;
          }
        }
        
        
        // Add error as a thread item
        const errorItem: ThreadItem = {
          id: generateId("err"),
          thread_id: threadId || "error",
          created_at: now(),
          type: "error",
          code: event.code || "custom",
          message: event.message || "An error occurred",
          allow_retry: event.allow_retry ?? false,
          http_status: event.http_status, // Include HTTP status if present
        };
        
       
        
        // Always add error item - even if we don't have a thread ID yet
        // This ensures errors during thread creation are visible
        if (threadId) {
          setThreadItems((prev) => {
            const updated = {
              ...prev,
              [threadId]: [...(prev[threadId] ?? []), errorItem],
            };
            return updated;
          });
        } else {
          // For errors during thread creation, create a temporary error-only view
          // by using a special "error" thread ID
          setThreadItems((prev) => {
            const updated = {
              ...prev,
              error: [errorItem],
            };
            
            return updated;
          });
          setActiveThreadId("error");
          console.log("Set activeThreadId to 'error'");
        }
        
        setIsStreaming(false);
        streamingAssistantRef.current = null;
        setProgressUpdate(null);
        
        // Notify callback
        if (onError) {
          onError({
            message: event.message || "An error occurred",
            code: event.code,
            threadId: threadId || undefined,
          });
        }
        break;
      }
    }
  }, [activeThreadId]);

  // Use the streaming hook
  const { cancel: cancelStream } = useThreadStream({
    url: chatServerUrl || "/chatkit",
    request: currentRequest,
    onEvent: handleStreamEvent,
    onError: (error) => {
      console.error("Stream error:", error);
      setIsStreaming(false);
      // Don't reset hasReceivedStreamEvent here - let the UI continue showing any items
      streamingAssistantRef.current = null;
      setProgressUpdate(null);
    },
    onComplete: () => {
      console.log("Stream completed");
      setIsStreaming(false);
      // Don't reset hasReceivedStreamEvent - we want to keep showing items even after stream completes
      streamingAssistantRef.current = null;
      const completedThreadId = streamingThreadRef.current;
      streamingThreadRef.current = null;
      setProgressUpdate(null);
      setCurrentRequest(null);
      
      // Notify callback
      if (onResponseEnd && completedThreadId) {
        onResponseEnd(completedThreadId);
      }
    },
    enabled: currentRequest !== null,
    retryConfig,
  });

  // Load threads from server
  const loadThreads = useCallback(async () => {
    try {
      const response = await fetch(chatServerUrl || "/chatkit", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          type: "threads.list",
          params: {
            limit: threadListLimit,
            order: threadListOrder,
          },
        }),
      });

      if (!response.ok) {
        console.error("Failed to load threads:", response.statusText);
        return;
      }

      const result: ThreadListResponse = await response.json();
      
      // Convert ThreadListItem to Thread
      const loadedThreads: Thread[] = result.data.map((item) => ({
        id: item.id,
        title: item.title,
        created_at: item.created_at,
        status: item.status,
        metadata: item.metadata,
      }));

      setThreads(loadedThreads);
      
      // If there are threads and none is active, select the first one
      if (loadedThreads.length > 0 && !activeThreadId) {
        setActiveThreadId(loadedThreads[0].id);
      }
    } catch (error) {
      console.error("Error loading threads:", error);
    }
  }, [chatServerUrl, activeThreadId, threadListLimit, threadListOrder]);

  // Load threads on mount
  useEffect(() => {
    loadThreads();
  }, []);

  const submitMessage = (threadId: string | null, text: string, attachments?: AttachmentMeta[]) => {
    const trimmed = text.trim();
    if (!trimmed && !attachments?.length) return;

    // Store last message for retry functionality
    lastMessageRef.current = { text: trimmed, attachments };
    
    setIsStreaming(true);
    setHasReceivedStreamEvent(false);

    // Prepare user message input
    const input = {
      content: [{ type: "input_text", text: trimmed }],
      attachments: attachments?.map((a) => a.id) ?? [],
      quoted_text: "",
      inference_options: {},
    };

    // Check if thread has any messages - if not, it's a new thread
    const hasMessages = threadId ? (threadItems[threadId]?.length ?? 0) > 0 : false;

    // Create request based on whether we have an existing thread with messages
    const request = threadId && hasMessages
      ? {
          type: "threads.add_user_message",
          params: {
            input,
            thread_id: threadId,
          },
        }
      : {
          type: "threads.create",
          params: {
            input,
          },
        };
    // Track which thread we're streaming to
    if (threadId) {
      streamingThreadRef.current = threadId;
    }

    // Start the stream
    setCurrentRequest(request);
  };

  const sendMessage = (text: string, attachments?: AttachmentMeta[]) => {
    submitMessage(activeThreadId, text, attachments);
    
    // Notify callback
    if (onMessageSent) {
      onMessageSent({ text, attachments });
    }
  };

  const cancelStreaming = () => {
    cancelStream();
    setIsStreaming(false);
    setHasReceivedStreamEvent(false);
    setCurrentRequest(null);
    streamingAssistantRef.current = null;
    setProgressUpdate(null);
  };

  const retryLastMessage = () => {
    if (!lastMessageRef.current) return;
    const { text, attachments } = lastMessageRef.current;
    submitMessage(activeThreadId, text, attachments);
  };

  const sendWidgetAction = (threadId: string, itemId: string, action: any) => {
    setIsStreaming(true);
    setHasReceivedStreamEvent(false);
    
    // Track which thread we're streaming to
    streamingThreadRef.current = threadId;
    
    // Create the action request
    const request = {
      type: "threads.custom_action",
      params: {
        item_id: itemId,
        action,
        thread_id: threadId,
      },
    };
    
    // Start the stream
    setCurrentRequest(request);
  };

  const createThread = (initialMessage?: string, options?: { title?: string }) => {
    setHistoryOpen(false);
    setHasReceivedStreamEvent(false); // Reset stream event flag for new thread

    if (initialMessage) {
      // Create a new thread with the initial message via API
      // Set activeThreadId to null so submitMessage sends threads.create
      setActiveThreadId(null);
      submitMessage(null, initialMessage);
    } else {
      // Create an empty thread state - no local thread ID yet
      // The thread will be created on the server when the first message is sent
      setActiveThreadId(null);
    }
  };

  const selectThread = async (threadId: string) => {
    setActiveThreadId(threadId);
    setHistoryOpen(false);

    // If thread items are not loaded, fetch them
    if (!threadItems[threadId] || threadItems[threadId].length === 0) {
      try {
        const response = await fetch(chatServerUrl || "/chatkit", {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
          },
          body: JSON.stringify({
            type: "threads.get_by_id",
            params: {
              thread_id: threadId,
            },
          }),
        });

        if (!response.ok) {
          console.error("Failed to load thread details:", response.statusText);
          return;
        }

        const result: ThreadDetailResponse = await response.json();
        
        // Update thread with full details including title
        setThreads((prev) =>
          prev.map((t) => (t.id === threadId ? { ...t, title: result.title } : t))
        );

        // Load thread items
        if (result.items?.data) {
          setThreadItems((prev) => ({
            ...prev,
            [threadId]: result.items.data,
          }));
        }
      } catch (error) {
        console.error("Error loading thread details:", error);
      }
    }
  };

  const openHistory = () => setHistoryOpen(true);
  const closeHistory = () => setHistoryOpen(false);
  const toggleHistory = () => setHistoryOpen((prev) => !prev);

  const getThreadItems = useCallback((threadId: string) => threadItems[threadId] ?? [], [threadItems]);

  const value: ChatContextValue = {
    threads,
    activeThreadId,
    activeThread,
    items,
    isStreaming,
    hasReceivedStreamEvent,
    historyOpen,
    starterPrompts,
    chatServerUrl,
    progressUpdate,
    attachmentImageSize,
    maxVisibleAttachments,
    composerConfig,
    welcomeHeaderConfig,
    shellContainerConfig,
    sendMessage,
    cancelStreaming,
    retryLastMessage,
    sendWidgetAction,
    createThread,
    selectThread,
    toggleHistory,
    openHistory,
    closeHistory,
    getThreadItems,
    onAttachmentAdded,
    onAttachmentRemoved,
    onThreadCreated,
    onThreadStarted,
    onThreadItemAdded,
    onResponseEnd,
    onMessageSent,
    onError,
  };

  return <ChatContext.Provider value={value}>{children}</ChatContext.Provider>;
}

export const useChat = () => {
  const context = useContext(ChatContext);
  if (!context) {
    throw new Error("useChat must be used within a ChatProvider");
  }
  return context;
};
