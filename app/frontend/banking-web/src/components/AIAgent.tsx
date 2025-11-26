
import { useState, useRef, useEffect } from "react";
import { MessageCircle, Send, X, Minimize2, Maximize2, Bot, Sparkles } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { ChatKit, useChatKit,ColorScheme } from "@openai/chatkit-react";
import {
  CHATKIT_API_URL,
  CHATKIT_API_DOMAIN_KEY,
  STARTER_PROMPTS,
  PLACEHOLDER_INPUT,
  GREETING,
} from "../lib/config";
import { useAgentResponse } from "@/context/AgentResponseContext";


export default function AIAgent() {
  const [isOpen, setIsOpen] = useState(false);
  const [isMinimized, setIsMinimized] = useState(false);
  const [showInvitation, setShowInvitation] = useState(true);
  const { triggerOnResponseEnd } = useAgentResponse();

  const chatkit = useChatKit({
    api: { url: CHATKIT_API_URL, 
           domainKey: CHATKIT_API_DOMAIN_KEY,
           uploadStrategy: { type: "two_phase" }  },
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
      triggerOnResponseEnd();
    },
    onThreadChange: () => {
      console.log("Thread changed");
    },
    onError: ({ error }) => {
      // ChatKit handles displaying the error to the user
      console.error("ChatKit error", error);
    },
  });

  
  if (!isOpen) {
    return (
      <div className="fixed bottom-6 right-6 z-50">
        {/* Invitation Card */}
        {showInvitation && (
          <div className="mb-4 mr-4 animate-slide-up">
            <Card className="bg-white border border-slate-200 shadow-professional-lg p-4 w-72">
              <div className="flex items-start space-x-3">
                <div className="h-8 w-8 rounded-full bg-blue-100 flex items-center justify-center flex-shrink-0">
                  <Sparkles className="h-4 w-4 text-blue-600" />
                </div>
                <div className="flex-1 min-w-0">
                  <h3 className="text-sm font-semibold text-slate-900 mb-1">AI Assistant Ready</h3>
                  <p className="text-xs text-slate-600 mb-2">Need help with banking tasks? I can assist with payments, account info, and more.</p>
                  <Button 
                    size="sm" 
                    onClick={() => {
                      setIsOpen(true);
                      setShowInvitation(false);
                    }}
                    className="text-xs h-7 bg-blue-600 hover:bg-blue-700"
                  >
                    Start Chat
                  </Button>
                </div>
                <button
                  onClick={() => setShowInvitation(false)}
                  className="text-slate-400 hover:text-slate-600 transition-colors"
                >
                  <X className="h-4 w-4" />
                </button>
              </div>
            </Card>
          </div>
        )}

        {/* Chat Button */}
        <Button
          onClick={() => setIsOpen(true)}
          className="h-14 w-14 rounded-full bg-blue-600 hover:bg-blue-700 shadow-professional-lg animate-bounce-gentle"
          size="icon"
        >
          <MessageCircle className="h-6 w-6" />
        </Button>
      </div>
    );
  }

  return (
    <Card className={`fixed bottom-6 right-6 bg-white border border-slate-200 shadow-professional-lg transition-all duration-300 animate-scale-in z-50 ${
      isMinimized ? "h-16 w-80" : "h-[500px] w-96"
    }`}>
      {/* Header */}
      <div className="flex items-center justify-between p-4 border-b border-slate-200 bg-gradient-to-r from-blue-50 to-indigo-50">
        <div className="flex items-center space-x-3">
          <div className="h-8 w-8 rounded-full bg-blue-600 flex items-center justify-center">
            <Bot className="h-5 w-5 text-white" />
          </div>
          <div>
            <h3 className="font-semibold text-sm text-slate-900">AI Banking Assistant</h3>
            <p className="text-xs text-slate-600 flex items-center">
              <span className="w-2 h-2 bg-green-500 rounded-full mr-1"></span>
              Online & Ready to Help
            </p>
          </div>
        </div>
        <div className="flex items-center space-x-1">
          <Button
            variant="ghost"
            size="icon"
            onClick={() => setIsMinimized(!isMinimized)}
            className="h-8 w-8 hover:bg-slate-100"
          >
            {isMinimized ? <Maximize2 className="h-4 w-4" /> : <Minimize2 className="h-4 w-4" />}
          </Button>
          <Button
            variant="ghost"
            size="icon"
            onClick={() => setIsOpen(false)}
            className="h-8 w-8 hover:bg-slate-100"
          >
            <X className="h-4 w-4" />
          </Button>
        </div>
      </div>

      {/* ChatKit Container with proper height constraints */}
     
        <div className="h-[calc(500px-73px)] overflow-hidden">
          <ChatKit control={chatkit.control} />
        </div>
  
       
    </Card>
  );
}
