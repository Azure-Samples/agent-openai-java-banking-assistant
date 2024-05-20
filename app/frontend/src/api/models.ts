export const enum Approaches {
    JAVA_OPENAI_SDK = "jos",
    JAVA_SEMANTIC_KERNEL = "jsk",
    JAVA_SEMANTIC_KERNEL_PLANNER = "jskp"
}

export const enum RetrievalMode {
    Hybrid = "hybrid",
    Vectors = "vectors",
    Text = "text"
}

export const enum SKMode {
    Chains = "chains",
    Planner = "planner"
}

export type ChatAppRequestOverrides = {
    retrieval_mode?: RetrievalMode;
    semantic_ranker?: boolean;
    semantic_captions?: boolean;
    exclude_category?: string;
    top?: number;
    temperature?: number;
    prompt_template?: string;
    prompt_template_prefix?: string;
    prompt_template_suffix?: string;
    suggest_followup_questions?: boolean;
    use_oid_security_filter?: boolean;
    use_groups_security_filter?: boolean;
    semantic_kernel_mode?: SKMode;
};

export type ResponseMessage = {
    content: string;
    role: string;
    attachments?: string[];
};

export type ResponseContext = {
    thoughts: string | null;
    data_points: string[];
};

export type ResponseChoice = {
    index: number;
    message: ResponseMessage;
    context: ResponseContext;
    session_state: any;
};

export type ChatAppResponseOrError = {
    choices?: ResponseChoice[];
    error?: string;
};

export type ChatAppResponse = {
    choices: ResponseChoice[];
};

export type ChatAppRequestContext = {
    overrides?: ChatAppRequestOverrides;
};

export type ChatAppRequest = {
    messages: ResponseMessage[];
    approach: Approaches;
    context?: ChatAppRequestContext;
    stream?: boolean;
    session_state: any;
};
