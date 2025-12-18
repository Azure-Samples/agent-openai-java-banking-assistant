import { StartScreenPrompt } from "@openai/chatkit";

export const CHATKIT_API_URL =
  import.meta.env.VITE_CHATKIT_API_URL ?? "/chatkit";

/**
 * ChatKit still expects a domain key at runtime. Use any placeholder locally,
 * but register your production domain at
 * https://platform.openai.com/settings/organization/security/domain-allowlist
 * and deploy the real key.
 */
export const CHATKIT_API_DOMAIN_KEY = 
  import.meta.env.VITE_CHATKIT_API_DOMAIN_KEY ?? "domain_pk_localhost_dev";


export const THEME_STORAGE_KEY = "chatkit-boilerplate-theme";

export const GREETING = "Hi";

export const STARTER_PROMPTS: StartScreenPrompt[] = [
  {
    label: "What is the limit on my Visa",
    prompt: "What is the limit on my Visa",
    icon: "book-open",
  },
  {
    label: "When was last time I paid contoso",
    prompt: "When was last time I paid contoso",
    icon: "search",
  },
  {
    label: "Can you help me paying a bill",
    prompt: "Can you help me paying a bill",
    icon: "sparkle",
  },
];

export const PLACEHOLDER_INPUT = "Describe your issue or question...";
