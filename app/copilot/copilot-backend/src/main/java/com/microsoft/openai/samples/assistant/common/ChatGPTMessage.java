// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.openai.samples.assistant.common;

import com.azure.core.util.ExpandableStringEnum;

import java.util.Collection;

public record ChatGPTMessage(ChatRole role, String content) {

    public static final class ChatRole extends ExpandableStringEnum<ChatRole> {
        public static final ChatRole SYSTEM = fromString("system");

        public static final ChatRole ASSISTANT = fromString("assistant");

        public static final ChatRole USER = fromString("user");

        public static ChatRole fromString(String name) {
            return fromString(name, ChatRole.class);
        }

        public static Collection<ChatRole> values() {
            return values(ChatRole.class);
        }
    }
}
