import { useEffect, useRef } from "react";
import type { RetryConfig } from "./types";

export interface StreamEvent {
  type: string;
  [key: string]: any;
}

export interface UseThreadStreamOptions {
  url: string;
  request: any;
  onEvent: (event: StreamEvent) => void;
  onError?: (error: Error) => void;
  onComplete?: () => void;
  enabled: boolean;
  retryConfig?: RetryConfig;
}

// Default retryable HTTP status codes
const DEFAULT_RETRYABLE_STATUS_CODES = [408, 429, 500, 502, 503, 504];

// Convert HTTP error to ErrorEvent format
function createHttpErrorEvent(status: number, statusText: string, retryableStatusCodes: number[]): StreamEvent {
  const isRetryable = retryableStatusCodes.includes(status);
  
  let message: string;
  if (status === 429) {
    message = "Too many requests. Please wait a moment and try again.";
  } else if (status === 408) {
    message = "Request timeout. Please try again.";
  } else if (status >= 500) {
    message = "Server error occurred. Please try again.";
  } else if (status === 401) {
    message = "Authentication required. Please log in again.";
  } else if (status === 403) {
    message = "Access denied. You don't have permission to perform this action.";
  } else if (status === 404) {
    message = "Resource not found. The requested resource could not be found.";
  } else {
    message = `HTTP error ${status}: ${statusText || 'Unknown error'}`;
  }
  
  return {
    type: "error",
    code: "http_error",
    message,
    allow_retry: isRetryable,
    http_status: status,
  };
}

/**
 * Custom hook for handling Server-Sent Events (SSE) streaming from the chat server.
 * Automatically manages connection lifecycle and event parsing.
 */
export function useThreadStream({ url, request, onEvent, onError, onComplete, enabled, retryConfig }: UseThreadStreamOptions) {
  const abortControllerRef = useRef<AbortController | null>(null);
  const onEventRef = useRef(onEvent);
  const onErrorRef = useRef(onError);
  const onCompleteRef = useRef(onComplete);
  const retryConfigRef = useRef(retryConfig);

  // Update refs when callbacks change
  useEffect(() => {
    onEventRef.current = onEvent;
    onErrorRef.current = onError;
    onCompleteRef.current = onComplete;
    retryConfigRef.current = retryConfig;
  });

  useEffect(() => {
    if (!enabled || !request) {
      return;
    }

    // Create a new AbortController for this stream
    abortControllerRef.current = new AbortController();
    const { signal } = abortControllerRef.current;

    const startStream = async () => {
      try {
        const response = await fetch(url, {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
            Accept: "text/event-stream",
          },
          body: JSON.stringify(request),
          signal,
        });

        if (!response.ok) {
          // Convert HTTP error to error event instead of throwing
          const retryableStatusCodes = retryConfigRef.current?.retryableStatusCodes ?? DEFAULT_RETRYABLE_STATUS_CODES;
          const errorEvent = createHttpErrorEvent(response.status, response.statusText, retryableStatusCodes);
          
          // Emit the error event so it's handled like SSE errors
          onEventRef.current(errorEvent);
          
          // Also call onComplete to clean up streaming state
          onCompleteRef.current?.();
          return;
        }

        const reader = response.body?.getReader();
        const decoder = new TextDecoder();

        if (!reader) {
          throw new Error("Response body is not readable");
        }

        let buffer = "";

        while (true) {
          const { done, value } = await reader.read();

          if (done) {
            onCompleteRef.current?.();
            break;
          }

          // Decode the chunk and add to buffer
          buffer += decoder.decode(value, { stream: true });

          // Process complete messages (lines starting with "data: ")
          const lines = buffer.split("\n");
          buffer = lines.pop() || ""; // Keep incomplete line in buffer

          for (const line of lines) {
            const trimmed = line.trim();
            
            // SSE events start with "data: "
            if (trimmed.startsWith("data: ")) {
              const jsonStr = trimmed.substring(6); // Remove "data: " prefix
              
              try {
                const event = JSON.parse(jsonStr) as StreamEvent;
                onEventRef.current(event);
              } catch (parseError) {
                console.error("Failed to parse SSE event:", jsonStr, parseError);
              }
            }
          }
        }
      } catch (error) {
        if (error instanceof Error) {
          if (error.name === "AbortError") {
            console.log("Stream aborted");
          } else {
            console.error("Stream error:", error);
            onErrorRef.current?.(error);
          }
        }
      }
    };

    startStream();

    // Cleanup function
    return () => {
      abortControllerRef.current?.abort();
    };
  }, [url, request, enabled]); // Only depend on url, request, and enabled - callbacks are stable via refs

  // Return cancel function
  const cancel = () => {
    abortControllerRef.current?.abort();
  };

  return { cancel };
}
