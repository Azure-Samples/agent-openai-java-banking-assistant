import type { ActionConfig, Spacing, Border, Borders, ThemeColor } from "./types";
import { cn } from "@/common/utils";

/**
 * Convert spacing object to Tailwind classes
 */
export function spacingToClasses(spacing: number | string | Spacing | undefined, type: "padding" | "margin"): string {
  if (!spacing) return "";
  
  const prefix = type === "padding" ? "p" : "m";
  
  if (typeof spacing === "number") {
    return `${prefix}-${spacing}`;
  }
  
  if (typeof spacing === "string") {
    return `${prefix}-[${spacing}]`;
  }
  
  // Handle spacing object
  const classes: string[] = [];
  
  if (spacing.x !== undefined) {
    if (typeof spacing.x === "number") {
      classes.push(`${prefix}x-${spacing.x}`);
    } else {
      classes.push(`${prefix}x-[${spacing.x}]`);
    }
  }
  
  if (spacing.y !== undefined) {
    if (typeof spacing.y === "number") {
      classes.push(`${prefix}y-${spacing.y}`);
    } else {
      classes.push(`${prefix}y-[${spacing.y}]`);
    }
  }
  
  if (spacing.top !== undefined) {
    if (typeof spacing.top === "number") {
      classes.push(`${prefix}t-${spacing.top}`);
    } else {
      classes.push(`${prefix}t-[${spacing.top}]`);
    }
  }
  
  if (spacing.right !== undefined) {
    if (typeof spacing.right === "number") {
      classes.push(`${prefix}r-${spacing.right}`);
    } else {
      classes.push(`${prefix}r-[${spacing.right}]`);
    }
  }
  
  if (spacing.bottom !== undefined) {
    if (typeof spacing.bottom === "number") {
      classes.push(`${prefix}b-${spacing.bottom}`);
    } else {
      classes.push(`${prefix}b-[${spacing.bottom}]`);
    }
  }
  
  if (spacing.left !== undefined) {
    if (typeof spacing.left === "number") {
      classes.push(`${prefix}l-${spacing.left}`);
    } else {
      classes.push(`${prefix}l-[${spacing.left}]`);
    }
  }
  
  return classes.join(" ");
}

/**
 * Convert border to CSS style object
 */
export function borderToStyle(border: number | Border | Borders | undefined): React.CSSProperties {
  if (!border) return {};
  
  if (typeof border === "number") {
    return { borderWidth: `${border}px` };
  }
  
  // Check if it's a single Border
  if ("size" in border) {
    const style: React.CSSProperties = { borderWidth: `${border.size}px` };
    if (border.color) {
      style.borderColor = typeof border.color === "string" ? border.color : border.color.light;
    }
    if (border.style) {
      style.borderStyle = border.style;
    }
    return style;
  }
  
  // Handle Borders (per-side)
  const style: React.CSSProperties = {};
  const borders = border as Borders;
  
  if (borders.top !== undefined) {
    if (typeof borders.top === "number") {
      style.borderTopWidth = `${borders.top}px`;
    } else {
      style.borderTopWidth = `${borders.top.size}px`;
      if (borders.top.color) {
        style.borderTopColor = typeof borders.top.color === "string" ? borders.top.color : borders.top.color.light;
      }
      if (borders.top.style) {
        style.borderTopStyle = borders.top.style;
      }
    }
  }
  
  if (borders.bottom !== undefined) {
    if (typeof borders.bottom === "number") {
      style.borderBottomWidth = `${borders.bottom}px`;
    } else {
      style.borderBottomWidth = `${borders.bottom.size}px`;
      if (borders.bottom.color) {
        style.borderBottomColor = typeof borders.bottom.color === "string" ? borders.bottom.color : borders.bottom.color.light;
      }
      if (borders.bottom.style) {
        style.borderBottomStyle = borders.bottom.style;
      }
    }
  }
  
  if (borders.left !== undefined) {
    if (typeof borders.left === "number") {
      style.borderLeftWidth = `${borders.left}px`;
    } else {
      style.borderLeftWidth = `${borders.left.size}px`;
      if (borders.left.color) {
        style.borderLeftColor = typeof borders.left.color === "string" ? borders.left.color : borders.left.color.light;
      }
      if (borders.left.style) {
        style.borderLeftStyle = borders.left.style;
      }
    }
  }
  
  if (borders.right !== undefined) {
    if (typeof borders.right === "number") {
      style.borderRightWidth = `${borders.right}px`;
    } else {
      style.borderRightWidth = `${borders.right.size}px`;
      if (borders.right.color) {
        style.borderRightColor = typeof borders.right.color === "string" ? borders.right.color : borders.right.color.light;
      }
      if (borders.right.style) {
        style.borderRightStyle = borders.right.style;
      }
    }
  }
  
  if (borders.x !== undefined) {
    if (typeof borders.x === "number") {
      style.borderLeftWidth = `${borders.x}px`;
      style.borderRightWidth = `${borders.x}px`;
    } else {
      style.borderLeftWidth = `${borders.x.size}px`;
      style.borderRightWidth = `${borders.x.size}px`;
      if (borders.x.color) {
        const color = typeof borders.x.color === "string" ? borders.x.color : borders.x.color.light;
        style.borderLeftColor = color;
        style.borderRightColor = color;
      }
      if (borders.x.style) {
        style.borderLeftStyle = borders.x.style;
        style.borderRightStyle = borders.x.style;
      }
    }
  }
  
  if (borders.y !== undefined) {
    if (typeof borders.y === "number") {
      style.borderTopWidth = `${borders.y}px`;
      style.borderBottomWidth = `${borders.y}px`;
    } else {
      style.borderTopWidth = `${borders.y.size}px`;
      style.borderBottomWidth = `${borders.y.size}px`;
      if (borders.y.color) {
        const color = typeof borders.y.color === "string" ? borders.y.color : borders.y.color.light;
        style.borderTopColor = color;
        style.borderBottomColor = color;
      }
      if (borders.y.style) {
        style.borderTopStyle = borders.y.style;
        style.borderBottomStyle = borders.y.style;
      }
    }
  }
  
  return style;
}

/**
 * Convert radius value to Tailwind class
 */
export function radiusToClass(radius: string | undefined): string {
  if (!radius) return "";
  
  const radiusMap: Record<string, string> = {
    "2xs": "rounded-sm",
    "xs": "rounded",
    "sm": "rounded-md",
    "md": "rounded-lg",
    "lg": "rounded-xl",
    "xl": "rounded-2xl",
    "2xl": "rounded-3xl",
    "3xl": "rounded-3xl",
    "4xl": "rounded-3xl",
    "full": "rounded-full",
    "100%": "rounded-full",
    "none": "rounded-none",
  };
  
  return radiusMap[radius] || `rounded-[${radius}]`;
}

/**
 * Convert color value to CSS or Tailwind class
 */
export function colorToStyle(color: string | ThemeColor | undefined): { style?: React.CSSProperties; className?: string } {
  if (!color) return {};
  
  if (typeof color === "object") {
    // Handle theme-aware colors (for now, use light mode)
    return { style: { color: color.light } };
  }
  
  // Check if it's a token or CSS value
  if (color.startsWith("#") || color.startsWith("rgb") || color.startsWith("hsl")) {
    return { style: { color } };
  }
  
  // It's a token, convert to Tailwind class
  return { className: `text-${color}` };
}

/**
 * Convert background value to CSS or Tailwind class
 */
export function backgroundToStyle(background: string | ThemeColor | undefined): { style?: React.CSSProperties; className?: string } {
  if (!background) return {};
  
  if (typeof background === "object") {
    // Handle theme-aware colors (for now, use light mode)
    return { style: { backgroundColor: background.light } };
  }
  
  // Check if it's a token or CSS value
  if (background.startsWith("#") || background.startsWith("rgb") || background.startsWith("hsl")) {
    return { style: { backgroundColor: background } };
  }
  
  // It's a token, convert to Tailwind class
  return { className: `bg-${background}` };
}

/**
 * Get size class for various components
 */
export function getSizeClass(size: string | undefined, type: "text" | "title" | "caption" | "control" | "icon"): string {
  if (!size) return "";
  
  const sizeMap: Record<string, Record<string, string>> = {
    text: {
      xs: "text-xs",
      sm: "text-sm",
      md: "text-base",
      lg: "text-lg",
      xl: "text-xl",
    },
    title: {
      sm: "text-lg",
      md: "text-xl",
      lg: "text-2xl",
      xl: "text-3xl",
      "2xl": "text-4xl",
      "3xl": "text-5xl",
      "4xl": "text-6xl",
      "5xl": "text-7xl",
    },
    caption: {
      sm: "text-xs",
      md: "text-sm",
      lg: "text-base",
    },
    control: {
      "3xs": "h-6 text-xs px-2",
      "2xs": "h-7 text-xs px-2",
      xs: "h-8 text-sm px-3",
      sm: "h-9 text-sm px-3",
      md: "h-10 text-base px-4",
      lg: "h-11 text-base px-4",
      xl: "h-12 text-lg px-5",
      "2xl": "h-14 text-lg px-6",
      "3xl": "h-16 text-xl px-7",
    },
    icon: {
      xs: "h-3 w-3",
      sm: "h-4 w-4",
      md: "h-5 w-5",
      lg: "h-6 w-6",
      xl: "h-8 w-8",
      "2xl": "h-10 w-10",
      "3xl": "h-12 w-12",
    },
  };
  
  return sizeMap[type]?.[size] || "";
}

/**
 * Get variant class for controls
 */
export function getVariantClass(variant: string | undefined): string {
  if (!variant) return "";
  
  const variantMap: Record<string, string> = {
    solid: "bg-primary text-primary-foreground hover:bg-primary/90",
    soft: "bg-primary/10 text-primary hover:bg-primary/20",
    outline: "border border-input bg-background hover:bg-accent hover:text-accent-foreground",
    ghost: "hover:bg-accent hover:text-accent-foreground",
  };
  
  return variantMap[variant] || "";
}

/**
 * Get alignment/justification classes
 */
export function getAlignmentClass(align: string | undefined): string {
  if (!align) return "";
  
  const alignMap: Record<string, string> = {
    start: "items-start",
    center: "items-center",
    end: "items-end",
    baseline: "items-baseline",
    stretch: "items-stretch",
  };
  
  return alignMap[align] || "";
}

export function getJustificationClass(justify: string | undefined): string {
  if (!justify) return "";
  
  const justifyMap: Record<string, string> = {
    start: "justify-start",
    center: "justify-center",
    end: "justify-end",
    between: "justify-between",
    around: "justify-around",
    evenly: "justify-evenly",
    stretch: "justify-stretch",
  };
  
  return justifyMap[justify] || "";
}

/**
 * Build common box/layout classes
 */
export function buildLayoutClasses(props: {
  direction?: "row" | "column";
  align?: string;
  justify?: string;
  wrap?: string;
  gap?: number | string;
  padding?: number | string | Spacing;
  margin?: number | string | Spacing;
  radius?: string;
  background?: string | ThemeColor;
  width?: number | string;
  height?: number | string;
  minWidth?: number | string;
  maxWidth?: number | string;
  minHeight?: number | string;
  maxHeight?: number | string;
  flex?: number | string;
}): { className: string; style: React.CSSProperties } {
  const classes: string[] = ["flex"];
  const style: React.CSSProperties = {};
  
  // Direction
  if (props.direction === "column") {
    classes.push("flex-col");
  } else {
    classes.push("flex-row");
  }
  
  // Alignment
  if (props.align) {
    classes.push(getAlignmentClass(props.align));
  }
  
  // Justification
  if (props.justify) {
    classes.push(getJustificationClass(props.justify));
  }
  
  // Wrap
  if (props.wrap === "wrap") {
    classes.push("flex-wrap");
  } else if (props.wrap === "wrap-reverse") {
    classes.push("flex-wrap-reverse");
  }
  
  // Gap
  if (props.gap !== undefined) {
    if (typeof props.gap === "number") {
      classes.push(`gap-${props.gap}`);
    } else {
      style.gap = props.gap;
    }
  }
  
  // Spacing
  const paddingClass = spacingToClasses(props.padding, "padding");
  if (paddingClass) classes.push(paddingClass);
  
  const marginClass = spacingToClasses(props.margin, "margin");
  if (marginClass) classes.push(marginClass);
  
  // Radius
  if (props.radius) {
    classes.push(radiusToClass(props.radius));
  }
  
  // Background
  if (props.background) {
    const bgResult = backgroundToStyle(props.background);
    if (bgResult.className) classes.push(bgResult.className);
    if (bgResult.style) Object.assign(style, bgResult.style);
  }
  
  // Dimensions
  if (props.width !== undefined) {
    style.width = typeof props.width === "number" ? `${props.width}px` : props.width;
  }
  if (props.height !== undefined) {
    style.height = typeof props.height === "number" ? `${props.height}px` : props.height;
  }
  if (props.minWidth !== undefined) {
    style.minWidth = typeof props.minWidth === "number" ? `${props.minWidth}px` : props.minWidth;
  }
  if (props.maxWidth !== undefined) {
    style.maxWidth = typeof props.maxWidth === "number" ? `${props.maxWidth}px` : props.maxWidth;
  }
  if (props.minHeight !== undefined) {
    style.minHeight = typeof props.minHeight === "number" ? `${props.minHeight}px` : props.minHeight;
  }
  if (props.maxHeight !== undefined) {
    style.maxHeight = typeof props.maxHeight === "number" ? `${props.maxHeight}px` : props.maxHeight;
  }
  if (props.flex !== undefined) {
    style.flex = props.flex;
  }
  
  return {
    className: cn(classes),
    style,
  };
}

/**
 * Create action handler for widgets
 */
export function createActionHandler(
  action: ActionConfig | undefined,
  onAction: (action: ActionConfig) => void,
): (() => void) | undefined {
  if (!action) return undefined;
  
  return () => {
    onAction(action);
  };
}
