import { useRef } from "react";
import { ChatKit, useChatKit,ColorScheme } from "@openai/chatkit-react";
import {
  CHATKIT_API_URL,
  CHATKIT_API_DOMAIN_KEY,
  STARTER_PROMPTS,
  PLACEHOLDER_INPUT,
  GREETING,
} from "../lib/config";


export default function Support() {

  console.log("Using chatkit domain key:", CHATKIT_API_DOMAIN_KEY);
  const chatkit = useChatKit({
    api: { url: CHATKIT_API_URL, 
        domainKey: CHATKIT_API_DOMAIN_KEY,
        uploadStrategy: { type: "two_phase" } },
    theme: {
      colorScheme: "light",
      color: {
        grayscale: {
          hue: 220,
          tint: 6,
          shade:  -4,
       
        },
        surface: {
            background: "#F0F4F8 ",
            foreground: "#ffffff",
        }
        ,
        accent: {
          primary: "#0f172a",
          level: 1,
        },
      },
      radius: "round",
    },
    startScreen: {
      greeting: GREETING,
      prompts: STARTER_PROMPTS,
    },
    composer: {
      placeholder: PLACEHOLDER_INPUT,
      attachments: {
        enabled: true,
        accept: { "image/*": [".png", ".jpg", ".jpeg"] },
      }
    },
    threadItemActions: {
      feedback: true,
    },
    onClientTool: async (invocation) => {
      console.log("Client tool invoked:", invocation);
      return {};
    },
    onResponseEnd: () => {
      console.log("Response ended");
    },
    onThreadChange: () => {
      console.log("Thread changed");
    },
    onError: ({ error }) => {
      // ChatKit handles displaying the error to the user
      console.error("ChatKit error", error);
    },
  });

  return (
    <div className="relative h-full w-full flex items-center justify-center p-8 bg-slate-50">
      <div className="w-full max-w-4xl h-[calc(100vh-8rem)] border-2 border-slate-300 rounded-lg overflow-hidden bg-white shadow-xl">
        <ChatKit control={chatkit.control} className="block h-full w-full" />
      </div>
    </div>
  );
}
