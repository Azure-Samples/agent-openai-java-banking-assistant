import ReactMarkdown from "react-markdown";
import remarkGfm from "remark-gfm";
import { cn } from "@/common/utils";

interface MarkdownProps {
  content: string;
  className?: string;
}

export function Markdown({ content, className }: MarkdownProps) {
  return (
    <div
      className={cn(
        "prose prose-sm dark:prose-invert max-w-none",
        "prose-p:leading-relaxed prose-p:my-2 prose-p:first:mt-0 prose-p:last:mb-0",
        "prose-pre:bg-muted prose-pre:text-foreground prose-pre:border prose-pre:border-border",
        "prose-code:bg-muted prose-code:text-foreground prose-code:px-1 prose-code:py-0.5 prose-code:rounded prose-code:before:content-none prose-code:after:content-none",
        "prose-strong:text-foreground prose-strong:font-semibold",
        "prose-a:text-primary prose-a:no-underline hover:prose-a:underline",
        "prose-blockquote:border-l-primary prose-blockquote:text-muted-foreground prose-blockquote:italic",
        "prose-h1:text-foreground prose-h1:font-bold prose-h1:text-2xl prose-h1:mt-4 prose-h1:mb-2",
        "prose-h2:text-foreground prose-h2:font-bold prose-h2:text-xl prose-h2:mt-4 prose-h2:mb-2",
        "prose-h3:text-foreground prose-h3:font-semibold prose-h3:text-lg prose-h3:mt-3 prose-h3:mb-1",
        "prose-ul:my-2 prose-ul:list-disc prose-ul:pl-6",
        "prose-ol:my-2 prose-ol:list-decimal prose-ol:pl-6",
        "prose-li:my-1",
        "prose-table:border-collapse prose-table:w-full prose-table:my-4",
        "prose-th:border prose-th:border-border prose-th:bg-muted prose-th:px-3 prose-th:py-2 prose-th:text-left prose-th:font-semibold",
        "prose-td:border prose-td:border-border prose-td:px-3 prose-td:py-2",
        "prose-img:rounded-lg prose-img:my-4",
        "prose-hr:border-border prose-hr:my-4",
        className,
      )}
    >
      <ReactMarkdown
        remarkPlugins={[remarkGfm]}
        components={{
          // Customize rendering for specific elements if needed
          a: ({ node, ...props }) => (
            <a {...props} target="_blank" rel="noopener noreferrer" />
          ),
          code: ({ node, inline, className, children, ...props }) => {
            if (inline) {
              return (
                <code className={className} {...props}>
                  {children}
                </code>
              );
            }
            return (
              <code className={cn(className, "block")} {...props}>
                {children}
              </code>
            );
          },
        }}
      >
        {content}
      </ReactMarkdown>
    </div>
  );
}
