export { WidgetRenderer } from "./WidgetRenderer";
export * from "./types";
export * from "./utils";
export * from "./WidgetRegistry";
export * from "./widgetUtils";
export { ToolApprovalRequest } from "./common/ToolApprovalRequest";

// Auto-register built-in widgets
import "./builtInWidgets";

// Re-export for convenience
export { widgetRegistry } from "./WidgetRegistry";
