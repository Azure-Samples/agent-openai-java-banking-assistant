
import { useState, useRef, useEffect } from "react";
import { MessageCircle, Send, X, Minimize2, Maximize2, Bot, Sparkles, GripVertical } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";

import { useAgentResponse } from "@/context/AgentResponseContext";

import { ChatProvider, ChatShell, type RetryConfig, type ShellHeaderConfig, type ComposerConfig, type ShellContainerConfig } from "@/components/chat";
import type { StarterPrompt, ThreadItem, Thread } from "@/components/chat/types";



export default function AIAgent() {
  const [isOpen, setIsOpen] = useState(false);
  const [isMinimized, setIsMinimized] = useState(false);
  const [showInvitation, setShowInvitation] = useState(true);
  const [dimensions, setDimensions] = useState({ width: 384, height: 500 }); // 96*4=384px (w-96)
  const [isResizing, setIsResizing] = useState(false);
  const { triggerOnResponseEnd } = useAgentResponse();
  const resizeRef = useRef<{ startX: number; startY: number; startWidth: number; startHeight: number } | null>(null);

  const chatServerUrl = import.meta.env.VITE_CHAT_SERVER_URL || "/chatkit";

  const BANKING_STARTER_PROMPTS: StarterPrompt[] = [
  {
    id: "pay-bill",
    title: "Pay a bill",
    description: "Upload an invoice or share the details",
    icon: "🧾",
    content: "Pay my latest Alpine Utilities invoice for this month",
  },
  {
    id: "card-trend",
    title: "Review card spend",
    description: "Summaries, trends, and anomalies",
    icon: "💳",
    content: "Summarize my Platinum Visa spending from the past 30 days",
  },
  {
    id: "transactions-search",
    title: "Investigate payments",
    description: "Search through your payments based on various criteria.",
    icon: "🛡️",
    content: "when was last time I've paid contoso?",
  },
];
  
    // Configure which HTTP status codes should allow retry
    // Default: [408, 429, 500, 502, 503, 504]
    const retryConfig: RetryConfig = {
      retryableStatusCodes: [408, 429, 500, 502, 503, 504],
    };
  
    // Configure header appearance and visibility
    // All properties are optional - omit to use defaults
    const headerConfig: ShellHeaderConfig = {
      showHeader: false,                        // Show/hide entire header
      showIcon: false,                          // Show/hide left icon badge
      // icon: Bot,                            // Custom icon (import from lucide-react)
      showTitle: false,                         // Show/hide title label           // Custom title text
      showActiveThread: false,                  // Show/hide active thread name
      activeThreadFallback: "Untitled thread", // Text when no thread selected
      showNewThreadButton: true,               // Show/hide new thread button
      showHistoryButton: false,                 // Show/hide history toggle button
      // customContent: <div>Custom Header</div> // Completely replace header content
    };

    // Configure composer appearance and behavior
    // All properties are optional - omit to use defaults
    const composerConfig: ComposerConfig = {
      placeholder: "Type your message...",
      buttonSize: "sm",                        // "sm" | "md" | "lg"
      showAttachmentCounter: false,            // Show/hide attachment counter
      maxAttachments: 5,                       // Maximum number of attachments
      showAttachmentTitle: false,               // Show/hide attachment filename
      showAttachmentSize: false,                // Show/hide attachment file size
    };

    // Configure shell container for embedded mode
    // Remove border, rounded corners, and shadow since AIAgent Card provides them
    const shellContainerConfig: ShellContainerConfig = {
      showBorder: false,
      showRoundedCorners: false,
      showShadow: false,
      backgroundColor: "bg-transparent",
    };



  const handleResponseEnd = (threadId: string) => {
    console.log("Response ended for thread:", threadId);
    // Call existing context callback if needed
    triggerOnResponseEnd();
    // Add your custom logic here
  };

  const handleThreadStarted = (threadId: string) => {
    console.log("Thread started:", threadId);
     triggerOnResponseEnd();
  }


  // Handle resize functionality
  useEffect(() => {
    let animationFrameId: number | null = null;

    const handleMouseMove = (e: MouseEvent) => {
      if (!isResizing || !resizeRef.current || isMinimized) return;

      // Cancel any pending animation frame
      if (animationFrameId !== null) {
        cancelAnimationFrame(animationFrameId);
      }

      // Use requestAnimationFrame for smooth, immediate updates
      animationFrameId = requestAnimationFrame(() => {
        if (!resizeRef.current) return;

        const deltaX = resizeRef.current.startX - e.clientX;
        const deltaY = resizeRef.current.startY - e.clientY;

        const newWidth = Math.max(320, Math.min(800, resizeRef.current.startWidth + deltaX));
        const newHeight = Math.max(400, Math.min(window.innerHeight - 100, resizeRef.current.startHeight + deltaY));

        setDimensions({ width: newWidth, height: newHeight });
      });
    };

    const handleMouseUp = () => {
      setIsResizing(false);
      resizeRef.current = null;
      if (animationFrameId !== null) {
        cancelAnimationFrame(animationFrameId);
      }
    };

    if (isResizing) {
      document.addEventListener('mousemove', handleMouseMove);
      document.addEventListener('mouseup', handleMouseUp);
      document.body.style.cursor = 'nwse-resize';
      document.body.style.userSelect = 'none';
    }

    return () => {
      document.removeEventListener('mousemove', handleMouseMove);
      document.removeEventListener('mouseup', handleMouseUp);
      document.body.style.cursor = '';
      document.body.style.userSelect = '';
      if (animationFrameId !== null) {
        cancelAnimationFrame(animationFrameId);
      }
    };
  }, [isResizing, isMinimized]);

  const handleResizeStart = (e: React.MouseEvent) => {
    e.preventDefault();
    setIsResizing(true);
    resizeRef.current = {
      startX: e.clientX,
      startY: e.clientY,
      startWidth: dimensions.width,
      startHeight: dimensions.height,
    };
  };
  
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
    <Card 
      className="fixed bottom-6 right-6 bg-white border border-slate-200 shadow-professional-lg animate-scale-in z-50 rounded-2xl overflow-hidden"
      style={{
        width: isMinimized ? '320px' : `${dimensions.width}px`,
        height: isMinimized ? '64px' : `${dimensions.height}px`,
        transition: isResizing ? 'none' : 'all 300ms',
      }}
    >
      {/* Resize Handle - Only visible when not minimized */}
      {!isMinimized && (
        <div
          onMouseDown={handleResizeStart}
          className="absolute top-0 left-0 w-6 h-6 cursor-nwse-resize hover:bg-blue-100 transition-colors group z-10"
          title="Drag to resize"
        >
          <GripVertical className="h-4 w-4 text-slate-400 group-hover:text-blue-600 rotate-45 absolute top-1 left-1" />
        </div>
      )}
      
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

      {/* ChatKit Container - hide when minimized but keep mounted to preserve state */}
      <div 
        className={`${isMinimized ? 'opacity-0 pointer-events-none h-0' : 'opacity-100'}`}
        style={{ 
          height: isMinimized ? '0' : `${dimensions.height - 73}px`,
          transition: isResizing ? 'none' : 'all 300ms',
        }}
      >
        <ChatProvider 
          starterPrompts={BANKING_STARTER_PROMPTS} 
          chatServerUrl={chatServerUrl}
          retryConfig={retryConfig}
          attachmentImageSize="lg"
          maxVisibleAttachments={3}
          composerConfig={composerConfig}
          shellContainerConfig={shellContainerConfig}
          onResponseEnd={handleResponseEnd}
          onThreadStarted={handleThreadStarted}
        >
          <ChatShell headerConfig={headerConfig} />
        </ChatProvider>
      </div>
    </Card>
  );
}
