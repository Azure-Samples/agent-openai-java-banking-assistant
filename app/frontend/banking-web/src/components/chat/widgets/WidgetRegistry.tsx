import React from "react";

/**
 * Props that all client-managed widgets receive
 */
export interface ClientWidgetProps {
  /** Arguments passed from the server */
  args: Record<string, unknown>;
  /** Widget item ID for action tracking */
  itemId: string;
}

/**
 * Type for a client-managed widget component
 */
export type ClientWidgetComponent = React.ComponentType<ClientWidgetProps>;

/**
 * Registry for client-managed widget components
 */
class WidgetRegistry {
  private widgets: Map<string, ClientWidgetComponent> = new Map();

  /**
   * Register a custom widget component
   * @param name - Unique name to identify the widget
   * @param component - React component to render
   */
  register(name: string, component: ClientWidgetComponent): void {
    this.widgets.set(name, component);
  }

  /**
   * Get a registered widget component by name
   * @param name - Name of the widget to retrieve
   * @returns The widget component or undefined if not found
   */
  get(name: string): ClientWidgetComponent | undefined {
    return this.widgets.get(name);
  }

  /**
   * Check if a widget is registered
   * @param name - Name of the widget to check
   * @returns True if the widget exists
   */
  has(name: string): boolean {
    return this.widgets.has(name);
  }

  /**
   * Get all registered widget names
   * @returns Array of widget names
   */
  getRegisteredNames(): string[] {
    return Array.from(this.widgets.keys());
  }

  /**
   * Unregister a widget component
   * @param name - Name of the widget to remove
   */
  unregister(name: string): void {
    this.widgets.delete(name);
  }

  /**
   * Clear all registered widgets
   */
  clear(): void {
    this.widgets.clear();
  }
}

// Global singleton instance
export const widgetRegistry = new WidgetRegistry();

/**
 * Hook to access the widget registry
 */
export function useWidgetRegistry() {
  return widgetRegistry;
}
