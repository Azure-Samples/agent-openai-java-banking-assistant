// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.openai.samples.assistant.controller;


import com.google.adk.agents.LlmAgent;
import com.google.adk.events.Event;
import com.google.adk.runner.InMemoryRunner;
import com.google.adk.runner.Runner;
import com.google.adk.sessions.BaseSessionService;
import com.google.adk.sessions.GetSessionConfig;
import com.google.adk.sessions.InMemorySessionService;
import com.google.adk.sessions.Session;
import com.google.genai.types.Content;
import com.google.genai.types.Part;
import com.microsoft.openai.samples.assistant.config.agent.SupervisorAgent;
import com.microsoft.openai.samples.assistant.security.LoggedUser;
import com.microsoft.openai.samples.assistant.security.LoggedUserService;
import io.reactivex.rxjava3.core.Flowable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;

@RestController
public class ADKChatController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ADKChatController.class);
    private static final String APPNAME = "Copilot";
    private final LlmAgent supervisorAgent;
    private final InMemoryRunner runner;
    private final LoggedUserService loggedUserService;

    public ADKChatController(@Qualifier("adkSupervisorAgent") LlmAgent supervisorAgent, LoggedUserService loggedUserService){
        this.supervisorAgent = supervisorAgent;
        this.loggedUserService = loggedUserService;
        this.runner = new InMemoryRunner(supervisorAgent,APPNAME);
    }


    @PostMapping(value = "/api/chat", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ChatResponse> openAIAsk(@RequestBody ChatAppRequest chatRequest) {
        if (chatRequest.stream()) {
            LOGGER.warn(
                    "Requested a content-type of application/json however also requested streaming."
                            + " Please use a content-type of application/ndjson");
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Requested a content-type of application/json however also requested streaming."
                            + " Please use a content-type of application/ndjson");
        }

        if (chatRequest.messages() == null || chatRequest.messages().isEmpty()) {
            LOGGER.warn("history cannot be null in Chat request");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }

        ResponseMessage userMessage = chatRequest.messages().get(chatRequest.messages().size()-1);
        LOGGER.debug("Processing user message..", userMessage );

        String inputText = userMessage.content();
        if(userMessage.attachments()!= null && !userMessage.attachments().isEmpty()) {
            inputText += " Attachments: " + userMessage.attachments();
        }
        String threadId = chatRequest.threadId();

       //attach the session to the runner or create a new one
        Session session = buildSession(threadId,loggedUserService.getLoggedUser(),runner);

        //Run the agent flow
        Content userMsg = Content.fromParts(Part.fromText(inputText));
        Flowable<Event> events = runner.runAsync(loggedUserService.getLoggedUser().username(), session.id(), userMsg);

        AtomicReference<String> agentResponse = new AtomicReference<>("");

        events.blockingForEach(event ->{
         LOGGER.info(" {} > {} ",event.author()+(event.finalResponse()?"[Final]" :""),event.stringifyContent());
            if(event.finalResponse())
                agentResponse.set(event.stringifyContent());

          }
        );
        return ResponseEntity.ok(
                ChatResponse.buildChatResponse(agentResponse.get(),session.id()));
    }

    private Session buildSession(String threadId, LoggedUser loggedUser, Runner runner) {

        if (threadId != null && !threadId.isEmpty()) {
            LOGGER.debug("Using existing threadId: {}", threadId);
            return runner.sessionService().getSession(APPNAME,loggedUser.username(), threadId, Optional.of(GetSessionConfig.builder().build())).blockingGet();
        } else {
            LOGGER.debug("Creating new threadId: {}", threadId);

            ConcurrentMap<String, Object> initialState = new ConcurrentHashMap<>();
            initialState.put("loggedUserName", loggedUser.username());
            var datetimeIso8601 = java.time.ZonedDateTime.now(java.time.ZoneId.of("UTC")).toInstant().toString();
            initialState.put("timestamp", datetimeIso8601);

          return runner.sessionService()
                    .createSession(APPNAME, loggedUser.username(),initialState,null)
                    .blockingGet();
        }

    }


}
