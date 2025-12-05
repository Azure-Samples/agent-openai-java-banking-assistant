/**
 * Built-in widgets initialization
 * Registers pre-built widgets with the widget registry
 */

import { widgetRegistry } from "./WidgetRegistry";
import { ToolApprovalRequest } from "./common/ToolApprovalRequest";

/**
 * Register all built-in widgets
 * This should be called once during application initialization
 */
export function registerBuiltInWidgets() {
  // Register the tool approval request widget
  // Support both hyphen and underscore naming conventions
  widgetRegistry.register("tool-approval-request", ToolApprovalRequest);
  widgetRegistry.register("tool_approval_request", ToolApprovalRequest);
}

// Auto-register on module load
registerBuiltInWidgets();
