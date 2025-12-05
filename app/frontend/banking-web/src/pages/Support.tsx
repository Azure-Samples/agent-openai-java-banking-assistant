import { ChatProvider, ChatShell, type RetryConfig, type ShellHeaderConfig, type WelcomeHeaderConfig } from "@/components/chat";
import type { StarterPrompt } from "@/components/chat/types";
import { Sparkles } from "lucide-react"; // Example: import custom icon

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

export default function Support() {
  // Configure your chat server URL here
  const chatServerUrl = import.meta.env.VITE_CHAT_SERVER_URL || "/chatkit";

  // Configure which HTTP status codes should allow retry
  // Default: [408, 429, 500, 502, 503, 504]
  const retryConfig: RetryConfig = {
    retryableStatusCodes: [408, 429, 500, 502, 503, 504],
  };

  // Configure header appearance and visibility
  // All properties are optional - omit to use defaults
  const headerConfig: ShellHeaderConfig = {
    showIcon: true,                          // Show/hide left icon badge
    // icon: Bot,                            // Custom icon (import from lucide-react)
    showTitle: true,                         // Show/hide title label
    titleLabel: "Banking copilot",           // Custom title text
    showActiveThread: true,                  // Show/hide active thread name
    activeThreadFallback: "Untitled thread", // Text when no thread selected
    showNewThreadButton: true,               // Show/hide new thread button
    showHistoryButton: true,                 // Show/hide history toggle button
    // customContent: <div>Custom Header</div> // Completely replace header content
  };

  // Configure welcome header (shown when no messages exist)
  // All properties are optional - omit to use defaults
  const welcomeHeaderConfig: WelcomeHeaderConfig = {
    icon: <Sparkles className="h-8 w-8 text-primary" />,
    title: "Welcome to Banking Assistant",
    subtitle: "Choose a prompt to get started",
  };

  return (
    <div className="relative flex h-full min-h-screen w-full items-center justify-center bg-slate-100 p-6">
      <div className="h-[720px] w-full max-w-5xl">
        <ChatProvider 
          starterPrompts={BANKING_STARTER_PROMPTS} 
          chatServerUrl={chatServerUrl}
          retryConfig={retryConfig}
          attachmentImageSize="lg"
          maxVisibleAttachments={3}
          welcomeHeaderConfig={welcomeHeaderConfig}
        >
          <ChatShell headerConfig={headerConfig} />
        </ChatProvider>
      </div>
    </div>
  );
}
