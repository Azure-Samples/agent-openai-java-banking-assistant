// ============================================================================
// Action Configuration Types
// ============================================================================
export interface ActionConfig {
  type: string;
  payload?: Record<string, unknown>;
  handler?: "server" | "client";
  loadingBehavior?: "auto" | "manual";
}

// ============================================================================
// Widget Base Types
// ============================================================================
export interface WidgetComponentBase {
  key?: string;
  id?: string;
  type: string;
}

// ============================================================================
// Color & Styling Types
// ============================================================================
export interface ThemeColor {
  dark: string;
  light: string;
}

export interface Spacing {
  top?: number | string;
  right?: number | string;
  bottom?: number | string;
  left?: number | string;
  x?: number | string;
  y?: number | string;
}

export interface Border {
  size: number;
  color?: string | ThemeColor;
  style?: "solid" | "dashed" | "dotted" | "double" | "groove" | "ridge" | "inset" | "outset";
}

export interface Borders {
  top?: number | Border;
  right?: number | Border;
  bottom?: number | Border;
  left?: number | Border;
  x?: number | Border;
  y?: number | Border;
}

export type RadiusValue = "2xs" | "xs" | "sm" | "md" | "lg" | "xl" | "2xl" | "3xl" | "4xl" | "full" | "100%" | "none";
export type TextAlign = "start" | "center" | "end";
export type Alignment = "start" | "center" | "end" | "baseline" | "stretch";
export type Justification = "start" | "center" | "end" | "between" | "around" | "evenly" | "stretch";
export type ControlVariant = "solid" | "soft" | "outline" | "ghost";
export type ControlSize = "3xs" | "2xs" | "xs" | "sm" | "md" | "lg" | "xl" | "2xl" | "3xl";

// ============================================================================
// Text Components
// ============================================================================
export interface TextWidget extends WidgetComponentBase {
  type: "Text";
  value: string;
  streaming?: boolean;
  italic?: boolean;
  lineThrough?: boolean;
  color?: string | ThemeColor;
  weight?: "normal" | "medium" | "semibold" | "bold";
  width?: number | string;
  size?: "xs" | "sm" | "md" | "lg" | "xl";
  textAlign?: TextAlign;
  truncate?: boolean;
  minLines?: number;
  maxLines?: number;
}

export interface TitleWidget extends WidgetComponentBase {
  type: "Title";
  value: string;
  color?: string | ThemeColor;
  weight?: "normal" | "medium" | "semibold" | "bold";
  size?: "sm" | "md" | "lg" | "xl" | "2xl" | "3xl" | "4xl" | "5xl";
  textAlign?: TextAlign;
  truncate?: boolean;
  maxLines?: number;
}

export interface CaptionWidget extends WidgetComponentBase {
  type: "Caption";
  value: string;
  color?: string | ThemeColor;
  weight?: "normal" | "medium" | "semibold" | "bold";
  size?: "sm" | "md" | "lg";
  textAlign?: TextAlign;
  truncate?: boolean;
  maxLines?: number;
}

export interface MarkdownWidget extends WidgetComponentBase {
  type: "Markdown";
  value: string;
  streaming?: boolean;
}

// ============================================================================
// Layout Components
// ============================================================================
export interface BoxBase {
  children?: WidgetComponent[];
  direction?: "row" | "column";
  align?: Alignment;
  justify?: Justification;
  wrap?: "nowrap" | "wrap" | "wrap-reverse";
  flex?: number | string;
  height?: number | string;
  width?: number | string;
  minHeight?: number | string;
  minWidth?: number | string;
  maxHeight?: number | string;
  maxWidth?: number | string;
  size?: number | string;
  minSize?: number | string;
  maxSize?: number | string;
  gap?: number | string;
  padding?: number | string | Spacing;
  margin?: number | string | Spacing;
  border?: number | Border | Borders;
  radius?: RadiusValue;
  background?: string | ThemeColor;
  aspectRatio?: number | string;
}

export interface BoxWidget extends WidgetComponentBase, BoxBase {
  type: "Box";
}

export interface RowWidget extends WidgetComponentBase, BoxBase {
  type: "Row";
}

export interface ColWidget extends WidgetComponentBase, BoxBase {
  type: "Col";
}

export interface FormWidget extends WidgetComponentBase, BoxBase {
  type: "Form";
  onSubmitAction: ActionConfig;
}

// ============================================================================
// Container Components (Root)
// ============================================================================
export interface CardAction {
  label: string;
  action: ActionConfig;
}

export interface WidgetStatus {
  text: string;
  favicon?: string;
  icon?: string;
}

export interface CardWidget extends WidgetComponentBase {
  type: "Card";
  asForm?: boolean;
  children: WidgetComponent[];
  background?: string | ThemeColor;
  size?: "sm" | "md" | "lg" | "full";
  padding?: number | string | Spacing;
  status?: WidgetStatus;
  collapsed?: boolean;
  confirm?: CardAction;
  cancel?: CardAction;
  theme?: "light" | "dark";
}

export interface ListViewItem extends WidgetComponentBase {
  type: "ListViewItem";
  children: WidgetComponent[];
  onClickAction?: ActionConfig;
  gap?: number | string;
  align?: Alignment;
}

export interface ListViewWidget extends WidgetComponentBase {
  type: "ListView";
  children: ListViewItem[];
  limit?: number | "auto";
  status?: WidgetStatus;
  theme?: "light" | "dark";
}

// ============================================================================
// Interactive Components
// ============================================================================
export interface ButtonWidget extends WidgetComponentBase {
  type: "Button";
  submit?: boolean;
  style?: "primary" | "secondary";
  label: string;
  onClickAction?: ActionConfig;
  iconStart?: string;
  iconEnd?: string;
  color?: "primary" | "secondary" | "info" | "discovery" | "success" | "caution" | "warning" | "danger";
  variant?: ControlVariant;
  size?: ControlSize;
  pill?: boolean;
  block?: boolean;
  uniform?: boolean;
  iconSize?: "sm" | "md" | "lg" | "xl" | "2xl";
}

export interface SelectOption {
  label: string;
  value: string;
}

export interface SelectWidget extends WidgetComponentBase {
  type: "Select";
  options: SelectOption[];
  onChangeAction?: ActionConfig;
  name: string;
  placeholder?: string;
  defaultValue?: string;
  variant?: ControlVariant;
  size?: ControlSize;
  pill?: boolean;
  block?: boolean;
  clearable?: boolean;
  disabled?: boolean;
}

export interface DatePickerWidget extends WidgetComponentBase {
  type: "DatePicker";
  onChangeAction?: ActionConfig;
  name: string;
  min?: string;
  max?: string;
  side?: "top" | "bottom" | "left" | "right";
  align?: "start" | "center" | "end";
  placeholder?: string;
  defaultValue?: string;
  variant?: ControlVariant;
  size?: ControlSize;
  pill?: boolean;
  block?: boolean;
  clearable?: boolean;
  disabled?: boolean;
}

export interface CheckboxWidget extends WidgetComponentBase {
  type: "Checkbox";
  onChangeAction?: ActionConfig;
  name: string;
  label?: string;
  defaultChecked?: boolean;
  disabled?: boolean;
}

export interface InputWidget extends WidgetComponentBase {
  onChangeAction?: ActionConfig;
  name: string;
  placeholder?: string;
  defaultValue?: string;
  variant?: ControlVariant;
  size?: ControlSize;
  pill?: boolean;
  block?: boolean;
  disabled?: boolean;
  type: "text" | "email" | "password" | "number" | "tel" | "url";
}

export interface RadioOption {
  label: string;
  value: string;
}

export interface RadioGroupWidget extends WidgetComponentBase {
  type: "RadioGroup";
  options: RadioOption[];
  onChangeAction?: ActionConfig;
  name: string;
  defaultValue?: string;
  disabled?: boolean;
}

export interface TextareaWidget extends WidgetComponentBase {
  type: "Textarea";
  onChangeAction?: ActionConfig;
  name: string;
  placeholder?: string;
  defaultValue?: string;
  rows?: number;
  disabled?: boolean;
  variant?: ControlVariant;
  size?: ControlSize;
  block?: boolean;
}

export interface LabelWidget extends WidgetComponentBase {
  type: "Label";
  children: WidgetComponent[];
  htmlFor?: string;
  required?: boolean;
}

// ============================================================================
// Visual Components
// ============================================================================
export interface BadgeWidget extends WidgetComponentBase {
  type: "Badge";
  label: string;
  color?: "secondary" | "success" | "danger" | "warning" | "info" | "discovery";
  variant?: "solid" | "soft" | "outline";
  pill?: boolean;
  size?: "sm" | "md" | "lg";
}

export interface IconWidget extends WidgetComponentBase {
  type: "Icon";
  name: string;
  color?: string | ThemeColor;
  size?: "xs" | "sm" | "md" | "lg" | "xl" | "2xl" | "3xl";
}

export interface ImageWidget extends WidgetComponentBase {
  type: "Image";
  src: string;
  alt?: string;
  size?: number | string;
  height?: number | string;
  width?: number | string;
  minHeight?: number | string;
  minWidth?: number | string;
  maxHeight?: number | string;
  maxWidth?: number | string;
  minSize?: number | string;
  maxSize?: number | string;
  radius?: RadiusValue;
  background?: string | ThemeColor;
  margin?: number | string | Spacing;
  aspectRatio?: number | string;
  flex?: number | string;
  fit?: "none" | "cover" | "contain" | "fill" | "scale-down";
  position?: "center" | "top" | "bottom" | "left" | "right" | "top left" | "top right" | "bottom left" | "bottom right";
  frame?: boolean;
  flush?: boolean;
}

export interface DividerWidget extends WidgetComponentBase {
  type: "Divider";
  spacing?: number | string;
  color?: string | ThemeColor;
  size?: number | string;
  flush?: boolean;
}

export interface SpacerWidget extends WidgetComponentBase {
  type: "Spacer";
  minSize?: number | string;
}

export interface TransitionWidget extends WidgetComponentBase {
  type: "Transition";
  children?: WidgetComponent;
}

// ============================================================================
// Chart Components
// ============================================================================
export interface ChartWidget extends WidgetComponentBase {
  type: "Chart";
  title?: string;
  xAxis?: {
    dataKey: string;
    label?: string;
  };
  series: ChartSeries[];
  height?: number | string;
  width?: number | string;
}

export type ChartSeries = BarSeries | AreaSeries | LineSeries;

export interface BarSeries {
  type: "bar";
  dataKey: string;
  name?: string;
  color?: string;
  data: Record<string, unknown>[];
}

export interface AreaSeries {
  type: "area";
  dataKey: string;
  name?: string;
  color?: string;
  data: Record<string, unknown>[];
  curve?: string;
}

export interface LineSeries {
  type: "line";
  dataKey: string;
  name?: string;
  color?: string;
  data: Record<string, unknown>[];
  curve?: string;
}

// ============================================================================
// Union Types
// ============================================================================
export type WidgetRoot = CardWidget | ListViewWidget;

export type WidgetComponent =
  | TextWidget
  | TitleWidget
  | CaptionWidget
  | MarkdownWidget
  | BoxWidget
  | RowWidget
  | ColWidget
  | FormWidget
  | ButtonWidget
  | SelectWidget
  | DatePickerWidget
  | CheckboxWidget
  | InputWidget
  | RadioGroupWidget
  | TextareaWidget
  | LabelWidget
  | BadgeWidget
  | IconWidget
  | ImageWidget
  | DividerWidget
  | SpacerWidget
  | TransitionWidget
  | ChartWidget
  | ListViewItem;
