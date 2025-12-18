import { ShellHeader, type ShellHeaderConfig } from "./ShellHeader";
import { StreamViewport } from "./StreamViewport";
import { HistoryView } from "./HistoryView";
import { Composer } from "./Composer";
import { useChat } from "./ChatProvider";
import { cn } from "@/common/utils";

interface ChatShellProps {
  headerConfig?: ShellHeaderConfig;
}

export function ChatShell({ headerConfig }: ChatShellProps) {
  const { historyOpen, attachmentImageSize, maxVisibleAttachments, composerConfig, shellContainerConfig } = useChat();

  // Default container styling (standalone mode)
  const {
    showBorder = true,
    showRoundedCorners = true,
    showShadow = true,
    backgroundColor = "bg-background",
    borderClass = "border-border/60",
    roundedClass = "rounded-3xl",
    shadowClass = "shadow-2xl",
  } = shellContainerConfig ?? {};

  return (
    <div className={cn(
      "flex h-full w-full flex-col overflow-hidden",
      showBorder && `border ${borderClass}`,
      showRoundedCorners && roundedClass,
      showShadow && shadowClass,
      backgroundColor
    )}>
      <ShellHeader config={headerConfig} />
      <div className="min-h-0 flex-1">
        {historyOpen ? (
          <HistoryView />
        ) : (
          <div className="flex h-full flex-col">
            <div className="min-h-0 flex-1">
              <StreamViewport 
                attachmentImageSize={attachmentImageSize}
                maxVisibleAttachments={maxVisibleAttachments}
              />
            </div>
            <div className="border-t border-border/70 bg-background/95">
              <Composer {...composerConfig} />
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
