package com.microsoft.openai.samples.assistant.common;

import com.azure.ai.openai.models.*;
import com.microsoft.semantickernel.services.chatcompletion.AuthorRole;
import com.microsoft.semantickernel.services.chatcompletion.ChatMessageContent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ChatGPTUtils {

    public static ChatCompletionsOptions buildDefaultChatCompletionsOptions(List<ChatRequestMessage> messages) {
        ChatCompletionsOptions completionsOptions = new ChatCompletionsOptions(messages);

        completionsOptions.setMaxTokens(1024);
        completionsOptions.setTemperature(0.1);
        completionsOptions.setTopP(1.0);
        //completionsOptions.setStop(new ArrayList<>(List.of("\n")));
        completionsOptions.setLogitBias(new HashMap<>());
        completionsOptions.setN(1);
        completionsOptions.setUser("search-openai-demo-java");
        completionsOptions.setPresencePenalty(0.0);
        completionsOptions.setFrequencyPenalty(0.0);

        return completionsOptions;
    }

    private static final String IM_START_USER = "<|im_start|>user";
    private static final String IM_START_ASSISTANT = "<|im_start|>assistant";
    private static final String IM_START_SYSTEM = "<|im_start|>system";

    public static String formatAsChatML(List<ChatRequestMessage> messages) {
        StringBuilder sb = new StringBuilder();
        messages.forEach(message -> {
            String content = null;
            if (message instanceof ChatRequestUserMessage) {
                sb.append(IM_START_USER).append("\n");
                content = ((ChatRequestUserMessage) message).getContent().toString();
            } else if (message instanceof ChatRequestSystemMessage) {
                sb.append(IM_START_SYSTEM).append("\n");
                content = ((ChatRequestSystemMessage) message).getContent().toString();
            } else if (message instanceof ChatRequestAssistantMessage) {
                sb.append(IM_START_ASSISTANT).append("\n");
                content = ((ChatRequestAssistantMessage) message).getContent().toString();
            }

            if (content != null) {
                sb.append(content).append("\n").append("|im_end|").append("\n");
            }
        });
        return sb.toString();
    }

    public static List<ChatMessageContent> parseChatML(String chatML) {
        List<ChatMessageContent> messages = new ArrayList<>();
        String[] messageTokens = chatML.split("\\|im_end\\|\\n");

        for (String messageToken : messageTokens) {
            String[] lines = messageToken.trim().split("\n");

            if (lines.length >= 2) {
                AuthorRole role = AuthorRole.SYSTEM;
                if (IM_START_USER.equals(lines[0])) {
                    role = AuthorRole.USER;
                } else if (IM_START_ASSISTANT.equals(lines[0])) {
                    role = AuthorRole.ASSISTANT;
                }

                StringBuilder content = new StringBuilder();
                for (int i = 1; i < lines.length; ++i) {
                    content.append(lines[i]);
                    if (i < lines.length - 1) {
                        content.append("\n");
                    }
                }

                messages.add(new ChatMessageContent<>(role, content.toString()));
            }
        }

        return messages;
    }

    public static String getLastUserQuestion(List<ChatGPTMessage> messages) {
        List<ChatGPTMessage> userMessages = messages
                .stream()
                .filter(message -> message.role() == ChatGPTMessage.ChatRole.USER)
                .toList();

        if (!userMessages.isEmpty()) {
            return userMessages.get(userMessages.size() - 1).content();
        } else {
            return "";
        }
    }
}
