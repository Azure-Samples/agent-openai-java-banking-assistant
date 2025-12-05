/**
 * API client for non-streaming ChatKit operations
 */

export interface AttachmentsCreateRequest {
  type: "attachments.create";
  params: {
    name: string;
    size: number;
    mime_type: string;
  };
  metadata?: Record<string, unknown>;
}

export interface AttachmentsCreateResponse {
  id: string;
  name: string;
  mime_type: string;
  upload_url: string;
  type: "file" | "image";
  preview_url?: string;
}

export interface AttachmentsDeleteRequest {
  type: "attachments.delete";
  params: {
    attachment_id: string;
  };
  metadata?: Record<string, unknown>;
}

// eslint-disable-next-line @typescript-eslint/no-empty-object-type
export interface AttachmentsDeleteResponse {
  // Empty response
}

export class ChatApiClient {
  private baseUrl: string;

  constructor(baseUrl: string = "/chatkit") {
    this.baseUrl = baseUrl;
  }

  /**
   * Phase 1: Create attachment metadata and get upload URL
   */
  async createAttachment(
    name: string,
    size: number,
    mimeType: string
  ): Promise<AttachmentsCreateResponse> {
    const request: AttachmentsCreateRequest = {
      type: "attachments.create",
      params: {
        name,
        size,
        mime_type: mimeType,
      },
    };

    const response = await fetch(this.baseUrl, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(request),
    });

    if (!response.ok) {
      const errorText = await response.text();
      throw new Error(`Failed to create attachment: ${response.statusText} - ${errorText}`);
    }

    return response.json();
  }

  /**
   * Phase 2: Upload file bytes to the provided upload URL
   */
  async uploadAttachmentBytes(
    uploadUrl: string,
    file: File,
    onProgress?: (progress: number) => void
  ): Promise<void> {
    return new Promise((resolve, reject) => {
      const xhr = new XMLHttpRequest();

      // Track upload progress
      if (onProgress) {
        xhr.upload.addEventListener("progress", (event) => {
          if (event.lengthComputable) {
            const progress = (event.loaded / event.total) * 100;
            onProgress(progress);
          }
        });
      }

      xhr.addEventListener("load", () => {
        if (xhr.status >= 200 && xhr.status < 300) {
          resolve();
        } else {
          reject(new Error(`Upload failed with status ${xhr.status}: ${xhr.statusText}`));
        }
      });

      xhr.addEventListener("error", () => {
        reject(new Error("Network error during upload"));
      });

      xhr.addEventListener("abort", () => {
        reject(new Error("Upload aborted"));
      });

      xhr.open("POST", uploadUrl);

      // Create FormData with the file
      const formData = new FormData();
      formData.append("file", file);

      xhr.send(formData);
    });
  }

  /**
   * Delete an attachment
   */
  async deleteAttachment(attachmentId: string): Promise<void> {
    const request: AttachmentsDeleteRequest = {
      type: "attachments.delete",
      params: {
        attachment_id: attachmentId,
      },
    };

    const response = await fetch(this.baseUrl, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(request),
    });

    if (!response.ok) {
      const errorText = await response.text();
      throw new Error(`Failed to delete attachment: ${response.statusText} - ${errorText}`);
    }

    await response.json();
  }

  /**
   * Complete two-phase upload workflow
   */
  async uploadAttachment(
    file: File,
    onProgress?: (progress: number) => void
  ): Promise<AttachmentsCreateResponse> {
    // Phase 1: Create attachment and get upload URL
    const attachment = await this.createAttachment(
      file.name,
      file.size,
      file.type || "application/octet-stream"
    );

    // Phase 2: Upload file bytes
    await this.uploadAttachmentBytes(attachment.upload_url, file, onProgress);

    return attachment;
  }
}
