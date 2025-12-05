/**
 * Utility functions for client-managed widgets
 * 
 * These utilities help client-managed widgets interact with the ChatKit server
 * by providing convenient functions for sending actions and formatting data.
 */

import { useChat } from "@/components/chat/ChatProvider";
import { useCallback, useEffect, useRef } from "react";

/**
 * Callbacks for widget action lifecycle events
 */
export interface WidgetActionCallbacks {
  onThreadStarted?: () => void;
  onThreadEnded?: () => void;
  onError?: (error: { message: string; code?: string }) => void;
}

/**
 * Hook to send widget actions to the ChatKit server
 * This handles the complete flow of sending a threads.custom_action request
 * with support for lifecycle callbacks
 * 
 * @param callbacks - Optional callbacks for action lifecycle events
 * @returns Function to send actions with proper context
 * 
 * @example
 * const sendWidgetAction = useSendWidgetAction({
 *   onThreadStarted: () => console.log('Thread started'),
 *   onThreadEnded: () => console.log('Thread ended'),
 *   onError: (error) => console.error('Error:', error)
 * });
 * sendWidgetAction(itemId, { type: "approval", payload: {...} });
 */
export function useSendWidgetAction(callbacks?: WidgetActionCallbacks) {
  const { sendWidgetAction, activeThreadId, isStreaming } = useChat();
  const wasStreamingRef = useRef(false);
  const callbacksRef = useRef(callbacks);
  const hasActionBeenSentRef = useRef(false);
  
  // Keep callbacks ref up to date
  useEffect(() => {
    callbacksRef.current = callbacks;
  }, [callbacks]);
  
  // Monitor streaming state changes - only if this widget has sent an action
  useEffect(() => {
    // Only react to streaming changes if this widget initiated an action
    if (!hasActionBeenSentRef.current) {
      return;
    }
    
    // Thread started (not streaming -> streaming)
    if (isStreaming && !wasStreamingRef.current) {
      callbacksRef.current?.onThreadStarted?.();
    }
    // Thread ended (streaming -> not streaming)
    else if (!isStreaming && wasStreamingRef.current) {
      callbacksRef.current?.onThreadEnded?.();
      // Reset the flag after thread ends
      hasActionBeenSentRef.current = false;
    }
    
    wasStreamingRef.current = isStreaming;
  }, [isStreaming]);
  
  return useCallback((itemId: string, action: {
    type: string;
    payload?: Record<string, unknown>;
    handler?: "server" | "client";
    loadingBehavior?: "auto" | "manual";
  }) => {
    if (!activeThreadId) {
      console.error("No active thread - cannot send widget action");
      callbacksRef.current?.onError?.({
        message: "No active thread - cannot send widget action",
        code: "NO_ACTIVE_THREAD"
      });
      return;
    }
    
    // Mark that this widget has sent an action
    hasActionBeenSentRef.current = true;
    
    // Format action with defaults
    const formattedAction = {
      type: action.type,
      payload: action.payload || {},
      handler: action.handler || "server",
      loadingBehavior: action.loadingBehavior || "auto",
    };
    
    // Send to ChatKit server via threads.custom_action
    sendWidgetAction(activeThreadId, itemId, formattedAction);
  }, [sendWidgetAction, activeThreadId]);
}

/**
 * Format object as Python-style string representation
 * Useful for displaying arguments in code blocks
 * @param obj - Object to format
 * @returns Python-style string
 */
export function formatAsPython(obj: unknown): string {
  return JSON.stringify(obj, null, 2)
    .replace(/"/g, "'")
    .replace(/null/g, "None")
    .replace(/true/g, "True")
    .replace(/false/g, "False");
}

/**
 * Create a markdown code block with Python syntax
 * @param content - Content to display in code block
 * @returns Markdown formatted code block
 */
export function createPythonCodeBlock(content: string): string {
  return `\`\`\`py\n${content}\n\`\`\``;
}
