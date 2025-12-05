import React, { useState } from "react";
import { Info } from "lucide-react";
import { Card } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Separator } from "@/components/ui/separator";
import { ClientWidgetProps } from "../WidgetRegistry";
import { useSendWidgetAction, formatAsPython, createPythonCodeBlock } from "../widgetUtils";
import ReactMarkdown from "react-markdown";

/**
 * Arguments expected by the ToolApprovalRequest widget
 */
interface ToolApprovalArgs {
  tool_name: string;
  tool_args: Record<string, unknown>;
  call_id: string;
  request_id: string;
  title?: string;
  description?: string;
}

/**
 * Pre-built widget for tool approval requests
 * This component displays a tool call approval UI with approve/reject buttons
 */
export function ToolApprovalRequest({ args, itemId }: ClientWidgetProps) {
  const {
    tool_name,
    tool_args,
    call_id,
    request_id,
    title = "Approval Required",
    description = "This action requires your approval before proceeding.",
  } = args as ToolApprovalArgs;

  // State to track which button is loading (null = none, 'approve' or 'reject')
  const [loadingButton, setLoadingButton] = useState<'approve' | 'reject' | null>(null);
  const [isDisabled, setIsDisabled] = useState(false);

  // Get action sender from hook with callbacks
  const sendWidgetAction = useSendWidgetAction({
    onThreadStarted: () => {
      // Loading state is already set when button is clicked
      console.log('Widget action thread started');
    },
    onThreadEnded: () => {
      // Disable buttons to prevent double submit
      setIsDisabled(true);
      setLoadingButton(null);
    },
    onError: (error) => {
      // Re-enable buttons on error so user can retry
      console.error('Widget action error:', error);
      setIsDisabled(false);
      setLoadingButton(null);
    }
  });

  // Format tool arguments as Python code
  const argsStr = formatAsPython(tool_args);
  const codeBlock = createPythonCodeBlock(argsStr);

  const handleApprove = () => {
    setLoadingButton('approve');
    sendWidgetAction(itemId, {
      type: "approval",
      payload: {
        tool_name,
        tool_args,
        approved: true,
        call_id,
        request_id,
      },
    });
  };

  const handleReject = () => {
    setLoadingButton('reject');
    sendWidgetAction(itemId, {
      type: "approval",
      payload: {
        tool_name,
        tool_args,
        approved: false,
        call_id,
        request_id,
      },
    });
  };

  return (
    <Card className="border p-0">
      {/* Header with icon and title */}
      <div className="flex flex-col items-center gap-4 p-4">
        <div className="flex items-center justify-center rounded-full bg-yellow-400 p-3">
          <Info className="h-12 w-12 text-white" />
        </div>
        <div className="flex flex-col items-center gap-1">
          <h3 className="text-xl font-semibold">{title}</h3>
          <p className="text-sm text-muted-foreground">{description}</p>
          <div className="mt-1 prose prose-sm dark:prose-invert">
            <ReactMarkdown>
              {`**${tool_name}**`}
            </ReactMarkdown>
          </div>
        </div>
      </div>

      {/* Tool arguments */}
      <div className="px-4 prose prose-sm dark:prose-invert max-w-none">
        <ReactMarkdown>
          {codeBlock}
        </ReactMarkdown>
      </div>

      {/* Divider */}
      <div className="px-4 py-2">
        <Separator />
      </div>

      {/* Action buttons */}
      <div className="flex gap-2 p-4 pt-0">
        <Button 
          onClick={handleApprove} 
          className="flex-1" 
          disabled={isDisabled || loadingButton !== null}
          loading={loadingButton === 'approve'}
        >
          Approve
        </Button>
        <Button 
          onClick={handleReject} 
          variant="outline" 
          className="flex-1" 
          disabled={isDisabled || loadingButton !== null}
          loading={loadingButton === 'reject'}
        >
          No
        </Button>
      </div>
    </Card>
  );
}
