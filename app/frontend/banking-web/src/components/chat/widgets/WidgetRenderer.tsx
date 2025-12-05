import React from "react";
import { cn } from "@/lib/utils";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Card } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Checkbox } from "@/components/ui/checkbox";
import { Label } from "@/components/ui/label";
import { RadioGroup, RadioGroupItem } from "@/components/ui/radio-group";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Textarea } from "@/components/ui/textarea";
import { Separator } from "@/components/ui/separator";
import { Markdown } from "@/components/chat/Markdown";
import {
  Calendar,
  Check,
  AlertCircle,
  Info,
  Sparkles,
  Star,
  User,
  Mail,
  Phone,
  MapPin,
  Globe,
  Briefcase,
  Loader2,
} from "lucide-react";

import type {
  ActionConfig,
  WidgetComponent,
  WidgetRoot,
  TextWidget,
  TitleWidget,
  CaptionWidget,
  MarkdownWidget,
  BoxWidget,
  RowWidget,
  ColWidget,
  FormWidget,
  ButtonWidget,
  BadgeWidget,
  IconWidget,
  ImageWidget,
  DividerWidget,
  SpacerWidget,
  CardWidget,
  ListViewWidget,
  ListViewItem,
  SelectWidget,
  CheckboxWidget,
  InputWidget,
  RadioGroupWidget,
  TextareaWidget,
  LabelWidget,
} from "./types";

import {
  buildLayoutClasses,
  colorToStyle,
  getSizeClass,
  getVariantClass,
  radiusToClass,
  borderToStyle,
  createActionHandler,
} from "./utils";

// ============================================================================
// Widget Renderer Context
// ============================================================================
interface WidgetContextValue {
  onAction: (action: ActionConfig, itemId: string) => void;
  itemId: string;
}

const WidgetContext = React.createContext<WidgetContextValue | undefined>(undefined);

function useWidgetContext() {
  const context = React.useContext(WidgetContext);
  if (!context) {
    throw new Error("Widget components must be used within WidgetRenderer");
  }
  return context;
}

// ============================================================================
// Icon Mapping
// ============================================================================
function getIconComponent(name: string, className?: string) {
  const icons: Record<string, React.ReactNode> = {
    check: <Check className={className} />,
    info: <Info className={className} />,
    alert: <AlertCircle className={className} />,
    sparkles: <Sparkles className={className} />,
    star: <Star className={className} />,
    user: <User className={className} />,
    mail: <Mail className={className} />,
    phone: <Phone className={className} />,
    "map-pin": <MapPin className={className} />,
    globe: <Globe className={className} />,
    briefcase: <Briefcase className={className} />,
    calendar: <Calendar className={className} />,
    loader: <Loader2 className={cn(className, "animate-spin")} />,
  };
  
  return icons[name] || <span className={className}>{name}</span>;
}

// ============================================================================
// Text Components
// ============================================================================
function TextComponent({ widget }: { widget: TextWidget }) {
  const colorResult = colorToStyle(widget.color);
  const sizeClass = getSizeClass(widget.size, "text");
  const weightClass = widget.weight ? `font-${widget.weight}` : "";
  const alignClass = widget.textAlign ? `text-${widget.textAlign}` : "";
  const italicClass = widget.italic ? "italic" : "";
  const lineThroughClass = widget.lineThrough ? "line-through" : "";
  const truncateClass = widget.truncate ? "truncate" : "";
  
  const style: React.CSSProperties = { ...colorResult.style };
  if (widget.width !== undefined) {
    style.width = typeof widget.width === "number" ? `${widget.width}px` : widget.width;
  }
  if (widget.maxLines) {
    style.display = "-webkit-box";
    style.WebkitLineClamp = widget.maxLines;
    style.WebkitBoxOrient = "vertical";
    style.overflow = "hidden";
  }
  
  return (
    <p
      className={cn(
        sizeClass,
        weightClass,
        alignClass,
        italicClass,
        lineThroughClass,
        truncateClass,
        colorResult.className,
      )}
      style={style}
    >
      {widget.value}
      {widget.streaming && <span className="ml-1 inline-block h-4 w-[2px] animate-pulse bg-primary" />}
    </p>
  );
}

function TitleComponent({ widget }: { widget: TitleWidget }) {
  const colorResult = colorToStyle(widget.color);
  const sizeClass = getSizeClass(widget.size, "title");
  const weightClass = widget.weight ? `font-${widget.weight}` : "font-semibold";
  const alignClass = widget.textAlign ? `text-${widget.textAlign}` : "";
  const truncateClass = widget.truncate ? "truncate" : "";
  
  const style: React.CSSProperties = { ...colorResult.style };
  if (widget.maxLines) {
    style.display = "-webkit-box";
    style.WebkitLineClamp = widget.maxLines;
    style.WebkitBoxOrient = "vertical";
    style.overflow = "hidden";
  }
  
  return (
    <h2
      className={cn(sizeClass, weightClass, alignClass, truncateClass, colorResult.className)}
      style={style}
    >
      {widget.value}
    </h2>
  );
}

function CaptionComponent({ widget }: { widget: CaptionWidget }) {
  const colorResult = colorToStyle(widget.color);
  const sizeClass = getSizeClass(widget.size, "caption");
  const weightClass = widget.weight ? `font-${widget.weight}` : "";
  const alignClass = widget.textAlign ? `text-${widget.textAlign}` : "";
  const truncateClass = widget.truncate ? "truncate" : "";
  
  const style: React.CSSProperties = { ...colorResult.style };
  if (widget.maxLines) {
    style.display = "-webkit-box";
    style.WebkitLineClamp = widget.maxLines;
    style.WebkitBoxOrient = "vertical";
    style.overflow = "hidden";
  }
  
  return (
    <p
      className={cn(
        "text-muted-foreground",
        sizeClass,
        weightClass,
        alignClass,
        truncateClass,
        colorResult.className,
      )}
      style={style}
    >
      {widget.value}
    </p>
  );
}

function MarkdownComponent({ widget }: { widget: MarkdownWidget }) {
  return <Markdown content={widget.value} />;
}

// ============================================================================
// Layout Components
// ============================================================================
function BoxComponent({ widget }: { widget: BoxWidget }) {
  const { className, style: layoutStyle } = buildLayoutClasses(widget);
  const borderStyle = borderToStyle(widget.border);
  const style = { ...layoutStyle, ...borderStyle };
  
  if (widget.aspectRatio) {
    style.aspectRatio = String(widget.aspectRatio);
  }
  
  return (
    <div className={className} style={style}>
      {widget.children?.map((child, index) => (
        <WidgetComponentRenderer key={child.key || index} widget={child} />
      ))}
    </div>
  );
}

function RowComponent({ widget }: { widget: RowWidget }) {
  const { className, style: layoutStyle } = buildLayoutClasses({
    ...widget,
    direction: "row",
    gap: widget.gap ?? 2, // Default gap of 2 (0.5rem/8px) if not specified
  });
  const borderStyle = borderToStyle(widget.border);
  const style = { ...layoutStyle, ...borderStyle };
  
  if (widget.aspectRatio) {
    style.aspectRatio = String(widget.aspectRatio);
  }
  
  return (
    <div className={className} style={style}>
      {widget.children?.map((child, index) => (
        <WidgetComponentRenderer key={child.key || index} widget={child} />
      ))}
    </div>
  );
}

function ColComponent({ widget }: { widget: ColWidget }) {
  const { className, style: layoutStyle } = buildLayoutClasses({
    ...widget,
    direction: "column",
  });
  const borderStyle = borderToStyle(widget.border);
  const style = { ...layoutStyle, ...borderStyle };
  
  if (widget.aspectRatio) {
    style.aspectRatio = String(widget.aspectRatio);
  }
  
  return (
    <div className={className} style={style}>
      {widget.children?.map((child, index) => (
        <WidgetComponentRenderer key={child.key || index} widget={child} />
      ))}
    </div>
  );
}

function FormComponent({ widget }: { widget: FormWidget }) {
  const { onAction, itemId } = useWidgetContext();
  const { className, style: layoutStyle } = buildLayoutClasses({
    ...widget,
    direction: widget.direction || "column",
  });
  const borderStyle = borderToStyle(widget.border);
  const style = { ...layoutStyle, ...borderStyle };
  
  const handleSubmit = (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    const formData = new FormData(e.currentTarget);
    const data = Object.fromEntries(formData.entries());
    
    onAction(
      {
        ...widget.onSubmitAction,
        payload: { ...widget.onSubmitAction.payload, formData: data },
      },
      itemId,
    );
  };
  
  return (
    <form className={className} style={style} onSubmit={handleSubmit}>
      {widget.children?.map((child, index) => (
        <WidgetComponentRenderer key={child.key || index} widget={child} />
      ))}
    </form>
  );
}

// ============================================================================
// Interactive Components
// ============================================================================
function ButtonComponent({ widget }: { widget: ButtonWidget }) {
  const { onAction, itemId } = useWidgetContext();
  const sizeClass = getSizeClass(widget.size, "control");
  const pillClass = widget.pill ? "rounded-full" : "";
  const blockClass = widget.block ? "w-full" : "";
  
  // Map widget variant to shadcn Button variant
  const variantMap: Record<string, "default" | "destructive" | "outline" | "secondary" | "ghost" | "link"> = {
    solid: "default",
    soft: "secondary",
    outline: "outline",
    ghost: "ghost",
  };
  const buttonVariant = widget.variant ? variantMap[widget.variant] || "default" : "default";
  
  const handleClick = widget.onClickAction
    ? () => onAction(widget.onClickAction!, itemId)
    : undefined;
  
  return (
    <Button
      type={widget.submit ? "submit" : "button"}
      onClick={handleClick}
      variant={buttonVariant}
      className={cn(sizeClass, pillClass, blockClass)}
    >
      {widget.iconStart && getIconComponent(widget.iconStart, "mr-2 h-4 w-4")}
      {widget.label}
      {widget.iconEnd && getIconComponent(widget.iconEnd, "ml-2 h-4 w-4")}
    </Button>
  );
}

function SelectComponent({ widget }: { widget: SelectWidget }) {
  const { onAction, itemId } = useWidgetContext();
  const sizeClass = getSizeClass(widget.size, "control");
  const blockClass = widget.block ? "w-full" : "";
  
  const handleChange = (value: string) => {
    if (widget.onChangeAction) {
      onAction(
        {
          ...widget.onChangeAction,
          payload: { ...widget.onChangeAction.payload, [widget.name]: value },
        },
        itemId,
      );
    }
  };
  
  return (
    <Select
      name={widget.name}
      defaultValue={widget.defaultValue}
      onValueChange={handleChange}
      disabled={widget.disabled}
    >
      <SelectTrigger className={cn(sizeClass, blockClass)}>
        <SelectValue placeholder={widget.placeholder} />
      </SelectTrigger>
      <SelectContent>
        {widget.options.map((option) => (
          <SelectItem key={option.value} value={option.value}>
            {option.label}
          </SelectItem>
        ))}
      </SelectContent>
    </Select>
  );
}

function CheckboxComponent({ widget }: { widget: CheckboxWidget }) {
  const { onAction, itemId } = useWidgetContext();
  
  const handleChange = (checked: boolean) => {
    if (widget.onChangeAction) {
      onAction(
        {
          ...widget.onChangeAction,
          payload: { ...widget.onChangeAction.payload, [widget.name]: checked },
        },
        itemId,
      );
    }
  };
  
  return (
    <div className="flex items-center space-x-2">
      <Checkbox
        id={widget.id || widget.name}
        name={widget.name}
        defaultChecked={widget.defaultChecked}
        disabled={widget.disabled}
        onCheckedChange={handleChange}
      />
      {widget.label && (
        <Label htmlFor={widget.id || widget.name} className="text-sm font-medium leading-none peer-disabled:cursor-not-allowed peer-disabled:opacity-70">
          {widget.label}
        </Label>
      )}
    </div>
  );
}

function InputComponent({ widget }: { widget: InputWidget }) {
  const { onAction, itemId } = useWidgetContext();
  const sizeClass = getSizeClass(widget.size, "control");
  const blockClass = widget.block ? "w-full" : "";
  
  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (widget.onChangeAction) {
      onAction(
        {
          ...widget.onChangeAction,
          payload: { ...widget.onChangeAction.payload, [widget.name]: e.target.value },
        },
        itemId,
      );
    }
  };
  
  return (
    <Input
      type={widget.type || "text"}
      name={widget.name}
      placeholder={widget.placeholder}
      defaultValue={widget.defaultValue}
      disabled={widget.disabled}
      onChange={handleChange}
      className={cn(sizeClass, blockClass)}
    />
  );
}

function RadioGroupComponent({ widget }: { widget: RadioGroupWidget }) {
  const { onAction, itemId } = useWidgetContext();
  
  const handleChange = (value: string) => {
    if (widget.onChangeAction) {
      onAction(
        {
          ...widget.onChangeAction,
          payload: { ...widget.onChangeAction.payload, [widget.name]: value },
        },
        itemId,
      );
    }
  };
  
  return (
    <RadioGroup
      name={widget.name}
      defaultValue={widget.defaultValue}
      onValueChange={handleChange}
      disabled={widget.disabled}
    >
      {widget.options.map((option) => (
        <div key={option.value} className="flex items-center space-x-2">
          <RadioGroupItem value={option.value} id={`${widget.name}-${option.value}`} />
          <Label htmlFor={`${widget.name}-${option.value}`}>{option.label}</Label>
        </div>
      ))}
    </RadioGroup>
  );
}

function TextareaComponent({ widget }: { widget: TextareaWidget }) {
  const { onAction, itemId } = useWidgetContext();
  const blockClass = widget.block ? "w-full" : "";
  
  const handleChange = (e: React.ChangeEvent<HTMLTextAreaElement>) => {
    if (widget.onChangeAction) {
      onAction(
        {
          ...widget.onChangeAction,
          payload: { ...widget.onChangeAction.payload, [widget.name]: e.target.value },
        },
        itemId,
      );
    }
  };
  
  return (
    <Textarea
      name={widget.name}
      placeholder={widget.placeholder}
      defaultValue={widget.defaultValue}
      rows={widget.rows}
      disabled={widget.disabled}
      onChange={handleChange}
      className={blockClass}
    />
  );
}

function LabelComponent({ widget }: { widget: LabelWidget }) {
  return (
    <Label htmlFor={widget.htmlFor}>
      {widget.children?.map((child, index) => (
        <WidgetComponentRenderer key={child.key || index} widget={child} />
      ))}
      {widget.required && <span className="ml-1 text-destructive">*</span>}
    </Label>
  );
}

// ============================================================================
// Visual Components
// ============================================================================
function BadgeComponent({ widget }: { widget: BadgeWidget }) {
  const colorVariants: Record<string, string> = {
    secondary: "secondary",
    success: "default",
    danger: "destructive",
    warning: "outline",
    info: "outline",
    discovery: "outline",
  };
  
  const variant = widget.color ? colorVariants[widget.color] : "default";
  
  return (
    <Badge variant={variant as any} className={widget.pill ? "rounded-full" : ""}>
      {widget.label}
    </Badge>
  );
}

function IconComponent({ widget }: { widget: IconWidget }) {
  const sizeClass = getSizeClass(widget.size, "icon");
  const colorResult = colorToStyle(widget.color);
  
  // Combine size and color classes for the icon
  const iconClassName = cn(sizeClass, colorResult.className);
  
  return getIconComponent(widget.name, iconClassName);
}

function ImageComponent({ widget }: { widget: ImageWidget }) {
  const radiusClass = widget.radius ? radiusToClass(widget.radius) : "";
  
  const style: React.CSSProperties = {};
  if (widget.width) style.width = typeof widget.width === "number" ? `${widget.width}px` : widget.width;
  if (widget.height) style.height = typeof widget.height === "number" ? `${widget.height}px` : widget.height;
  if (widget.aspectRatio) style.aspectRatio = String(widget.aspectRatio);
  if (widget.fit) style.objectFit = widget.fit;
  if (widget.position) style.objectPosition = widget.position;
  
  return (
    <img
      src={widget.src}
      alt={widget.alt || ""}
      className={radiusClass}
      style={style}
    />
  );
}

function DividerComponent({ widget }: { widget: DividerWidget }) {
  return <Separator className={widget.flush ? "my-0" : ""} />;
}

function SpacerComponent({ widget }: { widget: SpacerWidget }) {
  const style: React.CSSProperties = {};
  if (widget.minSize) {
    style.minHeight = typeof widget.minSize === "number" ? `${widget.minSize}px` : widget.minSize;
  }
  
  return <div className="flex-1" style={style} />;
}

// ============================================================================
// Container Components (Root)
// ============================================================================
function CardComponent({ widget }: { widget: CardWidget }) {
  const { onAction, itemId } = useWidgetContext();
  const paddingClass = widget.padding !== undefined ? `p-${widget.padding}` : "p-6";
  const radiusClass = radiusToClass("lg");
  
  const handleConfirm = widget.confirm
    ? () => onAction(widget.confirm!.action, itemId)
    : undefined;
  
  const handleCancel = widget.cancel
    ? () => onAction(widget.cancel!.action, itemId)
    : undefined;
  
  return (
    <Card className={cn(radiusClass, paddingClass, "border")}>
      {widget.status && (
        <div className="mb-4 flex items-center gap-2 text-sm text-muted-foreground">
          {widget.status.icon && getIconComponent(widget.status.icon, "h-4 w-4")}
          <span>{widget.status.text}</span>
        </div>
      )}
      
      {!widget.collapsed && (
        <div className="space-y-4">
          {widget.children.map((child, index) => (
            <WidgetComponentRenderer key={child.key || index} widget={child} />
          ))}
        </div>
      )}
      
      {(widget.confirm || widget.cancel) && (
        <div className="mt-6 flex gap-2">
          {widget.confirm && (
            <Button onClick={handleConfirm} className="flex-1">
              {widget.confirm.label}
            </Button>
          )}
          {widget.cancel && (
            <Button onClick={handleCancel} variant="outline" className="flex-1">
              {widget.cancel.label}
            </Button>
          )}
        </div>
      )}
    </Card>
  );
}

function ListViewItemComponent({ widget }: { widget: ListViewItem }) {
  const { onAction, itemId } = useWidgetContext();
  
  const handleClick = widget.onClickAction
    ? () => onAction(widget.onClickAction!, itemId)
    : undefined;
  
  const alignClass = widget.align ? `items-${widget.align}` : "";
  const gapStyle = widget.gap ? { gap: typeof widget.gap === "number" ? `${widget.gap * 0.25}rem` : widget.gap } : {};
  
  return (
    <div
      className={cn(
        "flex",
        alignClass,
        widget.onClickAction && "cursor-pointer hover:bg-accent transition-colors",
        "px-4 py-3 rounded-lg",
      )}
      style={gapStyle}
      onClick={handleClick}
    >
      {widget.children.map((child, index) => (
        <WidgetComponentRenderer key={child.key || index} widget={child} />
      ))}
    </div>
  );
}

function ListViewComponent({ widget }: { widget: ListViewWidget }) {
  const [showAll, setShowAll] = React.useState(false);
  
  const visibleItems = widget.limit && !showAll && widget.limit !== "auto"
    ? widget.children.slice(0, widget.limit)
    : widget.children;
  
  const hasMore = widget.limit && widget.limit !== "auto" && widget.children.length > widget.limit;
  
  return (
    <div className="space-y-2">
      {widget.status && (
        <div className="mb-4 flex items-center gap-2 text-sm text-muted-foreground">
          {widget.status.icon && getIconComponent(widget.status.icon, "h-4 w-4")}
          <span>{widget.status.text}</span>
        </div>
      )}
      
      <div className="space-y-1">
        {visibleItems.map((item, index) => (
          <ListViewItemComponent key={item.key || index} widget={item} />
        ))}
      </div>
      
      {hasMore && !showAll && (
        <Button
          variant="ghost"
          size="sm"
          onClick={() => setShowAll(true)}
          className="w-full"
        >
          Show more ({widget.children.length - visibleItems.length} remaining)
        </Button>
      )}
    </div>
  );
}

// ============================================================================
// Component Router
// ============================================================================
function WidgetComponentRenderer({ widget }: { widget: WidgetComponent }) {
  switch (widget.type) {
    case "Text":
      return <TextComponent widget={widget as TextWidget} />;
    case "Title":
      return <TitleComponent widget={widget as TitleWidget} />;
    case "Caption":
      return <CaptionComponent widget={widget as CaptionWidget} />;
    case "Markdown":
      return <MarkdownComponent widget={widget as MarkdownWidget} />;
    case "Box":
      return <BoxComponent widget={widget as BoxWidget} />;
    case "Row":
      return <RowComponent widget={widget as RowWidget} />;
    case "Col":
      return <ColComponent widget={widget as ColWidget} />;
    case "Form":
      return <FormComponent widget={widget as FormWidget} />;
    case "Button":
      return <ButtonComponent widget={widget as ButtonWidget} />;
    case "Select":
      return <SelectComponent widget={widget as SelectWidget} />;
    case "Checkbox":
      return <CheckboxComponent widget={widget as CheckboxWidget} />;
    case "Input":
      return <InputComponent widget={widget as InputWidget} />;
    case "RadioGroup":
      return <RadioGroupComponent widget={widget as RadioGroupWidget} />;
    case "Textarea":
      return <TextareaComponent widget={widget as TextareaWidget} />;
    case "Label":
      return <LabelComponent widget={widget as LabelWidget} />;
    case "Badge":
      return <BadgeComponent widget={widget as BadgeWidget} />;
    case "Icon":
      return <IconComponent widget={widget as IconWidget} />;
    case "Image":
      return <ImageComponent widget={widget as ImageWidget} />;
    case "Divider":
      return <DividerComponent widget={widget as DividerWidget} />;
    case "Spacer":
      return <SpacerComponent widget={widget as SpacerWidget} />;
    case "ListViewItem":
      return <ListViewItemComponent widget={widget as ListViewItem} />;
    default:
      console.warn("Unknown widget type:", (widget as any).type);
      return null;
  }
}

// ============================================================================
// Main Widget Renderer
// ============================================================================
interface WidgetRendererProps {
  widget: WidgetRoot;
  itemId: string;
  onAction: (action: ActionConfig, itemId: string) => void;
}

export function WidgetRenderer({ widget, itemId, onAction }: WidgetRendererProps) {
  const contextValue: WidgetContextValue = { onAction, itemId };
  
  return (
    <WidgetContext.Provider value={contextValue}>
      <div className="w-full animate-in fade-in-0 slide-in-from-bottom-2 duration-300">
        {widget.type === "Card" ? (
          <CardComponent widget={widget as CardWidget} />
        ) : widget.type === "ListView" ? (
          <ListViewComponent widget={widget as ListViewWidget} />
        ) : (
          <div>Unknown root widget type: {widget.type}</div>
        )}
      </div>
    </WidgetContext.Provider>
  );
}
