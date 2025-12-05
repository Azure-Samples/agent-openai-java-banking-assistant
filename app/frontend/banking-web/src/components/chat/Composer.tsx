import { useEffect, useMemo, useRef, useState } from "react";
import { ArrowUp, Loader2, Paperclip, Plus, Square, X } from "lucide-react";

import { Button } from "@/components/ui/button";
import { Textarea } from "@/components/ui/textarea";
import { Progress } from "@/components/ui/progress";
import { useChat } from "./ChatProvider";
import { ChatApiClient } from "./apiClient";
import type { AttachmentMeta } from "./types";

interface ComposerAttachment {
  id: string; // Server-assigned attachment ID (from phase 1)
  file: File;
  localPreviewUrl?: string; // Local browser preview URL
  serverPreviewUrl?: string; // Server preview URL (from phase 1)
  uploadStatus: "pending" | "uploading" | "uploaded" | "error";
  uploadProgress: number;
}

type ButtonSize = "sm" | "md" | "lg";

interface ButtonDimensions {
  height: string;
  width: string;
  iconSize: string;
  rounded: string;
}

interface ComposerProps {
  placeholder?: string;
  buttonSize?: ButtonSize;
  showAttachmentCounter?: boolean;
  maxAttachments?: number;
  showAttachmentTitle?: boolean;
  showAttachmentSize?: boolean;
}

const BUTTON_DIMENSIONS: Record<ButtonSize, ButtonDimensions> = {
  sm: { height: "h-8", width: "w-8", iconSize: "h-3 w-3", rounded: "rounded-full" },
  md: { height: "h-10", width: "w-10", iconSize: "h-4 w-4", rounded: "rounded-full" },
  lg: { height: "h-12", width: "w-12", iconSize: "h-5 w-5", rounded: "rounded-full" },
};

const DEFAULT_MAX_ATTACHMENTS = 5;
const DEFAULT_PLACEHOLDER = "Type your message...";

export function Composer({
  placeholder = DEFAULT_PLACEHOLDER,
  buttonSize = "lg",
  showAttachmentCounter = true,
  maxAttachments = DEFAULT_MAX_ATTACHMENTS,
  showAttachmentTitle = true,
  showAttachmentSize = true,
}: ComposerProps = {}) {
  const { sendMessage, cancelStreaming, isStreaming, chatServerUrl, onAttachmentAdded, onAttachmentRemoved } = useChat();
  const [value, setValue] = useState("");
  const [attachments, setAttachments] = useState<ComposerAttachment[]>([]);
  const fileInputRef = useRef<HTMLInputElement | null>(null);
  const attachmentsRef = useRef<ComposerAttachment[]>([]);
  const apiClientRef = useRef<ChatApiClient | null>(null);

  // Get button dimensions based on size
  const buttonDimensions = BUTTON_DIMENSIONS[buttonSize];
  
  // Adjust textarea min-height based on button size and counter visibility
  const textareaMinHeight = buttonSize === "sm" && !showAttachmentCounter ? "min-h-[32px]" : "min-h-[64px]";

  // Initialize API client
  if (!apiClientRef.current) {
    apiClientRef.current = new ChatApiClient(chatServerUrl);
  }

  const cleanupAttachment = (attachment: ComposerAttachment) => {
    if (attachment.localPreviewUrl) {
      URL.revokeObjectURL(attachment.localPreviewUrl);
    }
  };

  useEffect(() => {
    attachmentsRef.current = attachments;
  }, [attachments]);

  useEffect(() => {
    return () => {
      attachmentsRef.current.forEach(cleanupAttachment);
    };
  }, []);

  const handleFilesSelected = async (fileList: FileList | null) => {
    if (!fileList?.length) return;
    const files = Array.from(fileList);
    
    for (const file of files) {
      if (attachments.length >= maxAttachments) {
        break;
      }

      // Create local preview immediately
      const localPreviewUrl = file.type.startsWith("image/") ? URL.createObjectURL(file) : undefined;
      
      // Generate temporary ID for the attachment
      const tempId = `temp_${Math.random().toString(36).slice(2, 8)}`;
      
      // Add attachment with pending status
      const pendingAttachment: ComposerAttachment = {
        id: tempId,
        file,
        localPreviewUrl,
        uploadStatus: "pending",
        uploadProgress: 0,
      };

      setAttachments((prev) => [...prev, pendingAttachment]);

      // Start upload workflow asynchronously
      uploadAttachment(tempId, file, localPreviewUrl);
    }
  };

  const uploadAttachment = async (tempId: string, file: File, localPreviewUrl?: string) => {
    try {
      // Update status to uploading
      setAttachments((prev) =>
        prev.map((att) =>
          att.id === tempId ? { ...att, uploadStatus: "uploading" as const } : att
        )
      );

      const apiClient = apiClientRef.current;
      if (!apiClient) {
        throw new Error("API client not initialized");
      }

      // Phase 1 & 2: Upload attachment with progress tracking
      const response = await apiClient.uploadAttachment(file, (progress) => {
        setAttachments((prev) =>
          prev.map((att) =>
            att.id === tempId ? { ...att, uploadProgress: progress } : att
          )
        );
      });

      // Update with server-assigned ID and mark as uploaded
      setAttachments((prev) =>
        prev.map((att) =>
          att.id === tempId
            ? {
                ...att,
                id: response.id,
                serverPreviewUrl: response.preview_url,
                uploadStatus: "uploaded" as const,
                uploadProgress: 100,
              }
            : att
        )
      );

      // Notify parent component
      if (onAttachmentAdded) {
        onAttachmentAdded({
          id: response.id,
          name: file.name,
          size: file.size,
          mimeType: file.type || "application/octet-stream",
          previewUrl: localPreviewUrl,
          serverPreviewUrl: response.preview_url,
          uploadStatus: "uploaded",
          uploadProgress: 100,
        });
      }
    } catch (error) {
      console.error("Failed to upload attachment:", error);
      
      // Mark as error
      setAttachments((prev) =>
        prev.map((att) =>
          att.id === tempId ? { ...att, uploadStatus: "error" as const } : att
        )
      );
    }
  };

  const handleAttachmentButtonClick = () => {
    fileInputRef.current?.click();
  };

  const removeAttachment = async (id: string) => {
    const attachment = attachments.find((att) => att.id === id);
    if (!attachment) return;

    // If uploaded, delete from server
    if (attachment.uploadStatus === "uploaded" && !id.startsWith("temp_")) {
      try {
        const apiClient = apiClientRef.current;
        if (apiClient) {
          await apiClient.deleteAttachment(id);
        }
      } catch (error) {
        console.error("Failed to delete attachment from server:", error);
      }
    }

    // Remove from local state
    setAttachments((prev) => {
      const remaining = prev.filter((item) => item.id !== id);
      const removed = prev.find((item) => item.id === id);
      if (removed) {
        cleanupAttachment(removed);
      }
      return remaining;
    });

    // Notify parent component
    if (onAttachmentRemoved) {
      onAttachmentRemoved(id);
    }
  };

  const clearAttachments = () => {
    attachments.forEach(cleanupAttachment);
    setAttachments([]);
  };

  const canSubmit = useMemo(() => {
    return Boolean(value.trim()) || attachments.length > 0;
  }, [attachments.length, value]);

  const handleSend = () => {
    if (!canSubmit) return;

    // Only send attachments that are successfully uploaded
    const uploadedAttachments = attachments.filter((att) => att.uploadStatus === "uploaded");
    
    const attachmentPayload: AttachmentMeta[] = uploadedAttachments.map((attachment) => ({
      id: attachment.id,
      name: attachment.file.name,
      size: attachment.file.size,
      mimeType: attachment.file.type || "application/octet-stream",
      previewUrl: attachment.localPreviewUrl,
      serverPreviewUrl: attachment.serverPreviewUrl,
      uploadStatus: attachment.uploadStatus,
      uploadProgress: attachment.uploadProgress,
    }));

    sendMessage(value, attachmentPayload);
    setValue("");
    clearAttachments();
  };

  const handleKeyDown = (event: React.KeyboardEvent<HTMLTextAreaElement>) => {
    if (event.key === "Enter" && !event.shiftKey) {
      event.preventDefault();
      handleSend();
    }
  };

  const containerPadding = buttonSize === "sm" && !showAttachmentCounter ? "px-4 py-1.5" : "px-4 py-3";
  const innerPadding = buttonSize === "sm" && !showAttachmentCounter ? "px-3 py-1" : "px-3 py-2";
  const itemsGap = buttonSize === "sm" && !showAttachmentCounter ? "gap-2" : "gap-3";
  const textareaPadding = buttonSize === "sm" && !showAttachmentCounter ? "py-0" : "py-2";

  return (
    <div className={`flex flex-col gap-3 ${containerPadding}`}>
      <div className={`rounded-2xl border border-border/50 bg-muted/20 ${innerPadding} shadow-inner`}>
        {attachments.length > 0 && (
          <div className="mb-3 flex flex-wrap gap-3 border-b border-dashed border-border/60 pb-3">
            {attachments.map((attachment) => {
              const isImage = attachment.file.type.startsWith("image/");
              const isUploading = attachment.uploadStatus === "uploading";
              const isError = attachment.uploadStatus === "error";
              const showDetails = showAttachmentTitle || showAttachmentSize;
              
              // Compact layout when no title/size shown
              if (!showDetails) {
                return (
                  <div
                    key={attachment.id}
                    className="group relative"
                  >
                    {isImage && attachment.localPreviewUrl ? (
                      <div className="h-12 w-12 overflow-hidden rounded-xl border border-border/70 bg-background">
                        <img src={attachment.localPreviewUrl} alt={attachment.file.name} className="h-full w-full object-cover" />
                      </div>
                    ) : (
                      <div className="flex h-12 w-12 items-center justify-center rounded-xl border border-border/70 bg-background">
                        <Paperclip className="h-4 w-4 text-muted-foreground" />
                      </div>
                    )}
                    {isUploading && (
                      <div className="absolute inset-0 flex items-center justify-center rounded-xl bg-background/80">
                        <Loader2 className="h-4 w-4 animate-spin text-primary" />
                      </div>
                    )}
                    {!isUploading && (
                      <button
                        type="button"
                        onClick={() => removeAttachment(attachment.id)}
                        className="absolute -right-1 -top-1 h-5 w-5 rounded-full border border-border bg-background shadow-sm opacity-0 group-hover:opacity-100 transition-opacity flex items-center justify-center text-muted-foreground hover:text-foreground"
                        aria-label={`Remove ${attachment.file.name}`}
                      >
                        <X className="h-3 w-3" />
                      </button>
                    )}
                  </div>
                );
              }
              
              // Full layout with details
              return (
                <div
                  key={attachment.id}
                  className={`group flex items-center gap-3 rounded-2xl border ${
                    isError ? "border-destructive/50" : "border-dashed border-border"
                  } bg-muted/40 px-3 py-2 text-xs shadow-sm`}
                >
                  <div className="relative">
                    {isImage && attachment.localPreviewUrl ? (
                      <div className="h-12 w-12 overflow-hidden rounded-xl border border-border/70 bg-background">
                        <img src={attachment.localPreviewUrl} alt={attachment.file.name} className="h-full w-full object-cover" />
                      </div>
                    ) : (
                      <div className="flex h-10 w-10 items-center justify-center rounded-xl border border-border/70 bg-background">
                        <Paperclip className="h-4 w-4 text-muted-foreground" />
                      </div>
                    )}
                    {isUploading && (
                      <div className="absolute inset-0 flex items-center justify-center rounded-xl bg-background/80">
                        <Loader2 className="h-4 w-4 animate-spin text-primary" />
                      </div>
                    )}
                  </div>
                  <div className="max-w-[160px] flex-1 space-y-1">
                    {showAttachmentTitle && (
                      <p className="truncate font-medium text-foreground">{attachment.file.name}</p>
                    )}
                    {showAttachmentSize && (
                      <p className="text-[11px] text-muted-foreground">{(attachment.file.size / 1024).toFixed(1)} KB</p>
                    )}
                    {isUploading && (
                      <Progress value={attachment.uploadProgress} className="h-1" />
                    )}
                    {isError && (
                      <p className="text-[11px] text-destructive">Upload failed</p>
                    )}
                  </div>
                  <button
                    type="button"
                    onClick={() => removeAttachment(attachment.id)}
                    className="text-muted-foreground transition hover:text-foreground"
                    aria-label={`Remove ${attachment.file.name}`}
                    disabled={isUploading}
                  >
                    <X className="h-3 w-3" />
                  </button>
                </div>
              );
            })}
          </div>
        )}
        <div className={`flex items-end ${itemsGap}`}>
          <div className="flex flex-col items-center gap-1">
            <Button
              type="button"
              variant="ghost"
              size="icon"
              onClick={handleAttachmentButtonClick}
              className={`${buttonDimensions.height} ${buttonDimensions.width} rounded-xl border border-dashed border-border bg-background/80 text-muted-foreground`}
              disabled={attachments.length >= maxAttachments}
            >
              <Plus className={buttonDimensions.iconSize} />
              <span className="sr-only">Add attachment</span>
            </Button>
            {showAttachmentCounter && (
              <span className="text-[10px] uppercase tracking-wide text-muted-foreground">
                {attachments.length}/{maxAttachments}
              </span>
            )}
            <input
              ref={fileInputRef}
              type="file"
              multiple
              className="hidden"
              onChange={(event) => {
                handleFilesSelected(event.target.files);
                event.target.value = "";
              }}
            />
          </div>
          <Textarea
            value={value}
            onChange={(event) => setValue(event.target.value)}
            onKeyDown={handleKeyDown}
            placeholder={placeholder}
            className={`${textareaMinHeight} max-h-40 flex-1 resize-none border-none bg-transparent px-0 ${textareaPadding} text-sm shadow-none focus-visible:ring-0 focus-visible:ring-offset-0`}
          />
          {isStreaming && (
            <Button
              type="button"
              size="icon"
              onClick={cancelStreaming}
              className={`${buttonDimensions.height} ${buttonDimensions.width} ${buttonDimensions.rounded} bg-destructive text-destructive-foreground shadow-sm`}
            >
              <Square className={buttonDimensions.iconSize} />
              <span className="sr-only">Stop streaming</span>
            </Button>
          )}
          <Button
            type="button"
            size="icon"
            onClick={handleSend}
            className={`${buttonDimensions.height} ${buttonDimensions.width} ${buttonDimensions.rounded} border border-primary/20 bg-primary/10 text-primary shadow-sm backdrop-blur-sm disabled:border-transparent disabled:bg-muted-foreground/50 disabled:text-white`}
            disabled={isStreaming || !canSubmit}
          >
            {isStreaming ? <Loader2 className={`${buttonDimensions.iconSize} animate-spin`} /> : <ArrowUp className={buttonDimensions.iconSize} />}
            <span className="sr-only">Send message</span>
          </Button>
        </div>
      </div>
    </div>
  );
}
