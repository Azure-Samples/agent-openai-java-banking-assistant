import { type LucideIcon, Sparkles, SquarePen, History } from "lucide-react";
import type { ReactNode } from "react";

import { Button } from "@/components/ui/button";
import { Tooltip, TooltipContent, TooltipProvider, TooltipTrigger } from "@/components/ui/tooltip";
import { cn } from "@/lib/utils";
import { useChat } from "./ChatProvider";

export interface ShellHeaderConfig {
  /** Show/hide the entire header */
  showHeader?: boolean;
  /** Show/hide the icon badge on the left */
  showIcon?: boolean;
  /** Custom icon component (defaults to Sparkles) */
  icon?: LucideIcon;
  /** Show/hide the title section */
  showTitle?: boolean;
  /** Custom title label (defaults to "Banking copilot") */
  titleLabel?: string;
  /** Show/hide the active thread name */
  showActiveThread?: boolean;
  /** Custom fallback text when no thread is active */
  activeThreadFallback?: string;
  /** Show/hide the new thread button */
  showNewThreadButton?: boolean;
  /** Show/hide the history toggle button */
  showHistoryButton?: boolean;
  /** Custom header content (replaces entire header if provided) */
  customContent?: ReactNode;
}

interface ShellHeaderProps {
  config?: ShellHeaderConfig;
}

export function ShellHeader({ config }: ShellHeaderProps) {
  const { activeThread, createThread, historyOpen, toggleHistory } = useChat();

  const {
    showHeader = true,
    showIcon = true,
    icon: IconComponent = Sparkles,
    showTitle = true,
    titleLabel = "Banking copilot",
    showActiveThread = true,
    activeThreadFallback = "Untitled thread",
    showNewThreadButton = true,
    showHistoryButton = true,
    customContent,
  } = config ?? {};

  // If header is hidden, render nothing
  if (!showHeader) {
    return null;
  }

  // If custom content is provided, render it entirely
  if (customContent) {
    return <header className="flex items-center justify-between border-b border-border/80 px-4 py-3">{customContent}</header>;
  }

  return (
    <header className="flex items-center justify-between border-b border-border/80 px-4 py-3">
      <div className="flex items-center gap-3">
        {showIcon && (
          <span className="inline-flex h-10 w-10 items-center justify-center rounded-full bg-primary/10 text-primary">
            <IconComponent className="h-5 w-5" />
          </span>
        )}
        {(showTitle || showActiveThread) && (
          <div className="space-y-0.5">
            {showTitle && <p className="text-xs uppercase tracking-wide text-muted-foreground">{titleLabel}</p>}
            {showActiveThread && (
              <div className="flex items-center gap-2 text-sm font-medium text-foreground">
                {activeThread?.title ?? activeThreadFallback}
              </div>
            )}
          </div>
        )}
      </div>
      <TooltipProvider>
        <div className="flex items-center gap-2">
          {showNewThreadButton && (
            <Tooltip delayDuration={150}>
              <TooltipTrigger asChild>
                <Button variant="ghost" size="icon" className="hover:bg-primary/10" onClick={() => createThread()}>
                  <SquarePen className="h-4 w-4" />
                  <span className="sr-only">Start new thread</span>
                </Button>
              </TooltipTrigger>
              <TooltipContent sideOffset={6}>Start a fresh conversation</TooltipContent>
            </Tooltip>
          )}
          {showHistoryButton && (
            <Tooltip delayDuration={150}>
              <TooltipTrigger asChild>
                <Button
                  variant="ghost"
                  size="icon"
                  aria-pressed={historyOpen}
                  onClick={toggleHistory}
                  className={cn("hover:bg-primary/10", historyOpen && "bg-primary/10 text-primary")}
                >
                  <History className="h-4 w-4" />
                  <span className="sr-only">View history</span>
                </Button>
              </TooltipTrigger>
              <TooltipContent sideOffset={6}>{historyOpen ? "Close history" : "Show thread history"}</TooltipContent>
            </Tooltip>
          )}
        </div>
      </TooltipProvider>
    </header>
  );
}
