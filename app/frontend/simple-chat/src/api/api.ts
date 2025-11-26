import { ChatAppResponse, ChatAppResponseOrError, ChatAppRequest } from "./models";
import { useLogin } from "../authConfig";

const BACKEND_URI = import.meta.env.VITE_BACKEND_URI ? import.meta.env.VITE_BACKEND_URI : "";

function getHeaders(idToken: string | undefined, stream:boolean): Record<string, string> {
    var headers: Record<string, string> = {
        "Content-Type": "application/json"
    };
    // If using login, add the id token of the logged in account as the authorization
    if (useLogin) {
        if (idToken) {
            headers["Authorization"] = `Bearer ${idToken}`
        }
    }

    if (stream) {
        headers["Accept"] = "application/x-ndjson";
    } else {
        headers["Accept"] = "application/json";
    }

    return headers;
}

export async function askApi(request: ChatAppRequest, idToken: string | undefined): Promise<ChatAppResponse> {
    const response = await fetch(`${BACKEND_URI}/ask`, {
        method: "POST",
        headers: getHeaders(idToken, request.stream || false),
        body: JSON.stringify(request)
    });

    const parsedResponse: ChatAppResponseOrError = await response.json();
    if (response.status > 299 || !response.ok) {
        throw Error(parsedResponse.error || "Unknown error");
    }

    return parsedResponse as ChatAppResponse;
}

export async function chatApi(request: ChatAppRequest, idToken: string | undefined): Promise<Response> {
    return await fetch(`${BACKEND_URI}/chat`, {
        method: "POST",
        headers: getHeaders(idToken, request.stream || false),
        body: JSON.stringify(request)
    });
}

export function getCitationFilePath(citation: string): string {
    return `${BACKEND_URI}/content/${citation}`;
}

export function uploadAttachment(file: File): Promise<string> {
    const formData = new FormData();
    formData.append("file", file);

    return fetch(`${BACKEND_URI}/content`, {
        method: "POST",
        body: formData
    }).then(response => {
        if (response.status > 299 || !response.ok) {
            throw Error("Failed to upload attachment");
        }
        return response.text();
    });
}

export function getImage(name: string): string {
        return `${BACKEND_URI}/content/${name}`;
    }

