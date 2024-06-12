package com.microsoft.openai.samples.assistant.plugin;

import com.microsoft.openai.samples.assistant.security.LoggedUser;
import com.microsoft.openai.samples.assistant.security.LoggedUserService;
import com.microsoft.semantickernel.semanticfunctions.annotations.DefineKernelFunction;

public class LoggedUserPlugin {

    private final LoggedUserService loggedUserService;
    public LoggedUserPlugin(LoggedUserService loggedUserService)
    {
        this.loggedUserService = loggedUserService;
    }
    @DefineKernelFunction(name = "UserContext", description = "Gets the user details after login")
    public String getUserContext() {
        LoggedUser loggedUser = loggedUserService.getLoggedUser();
        return loggedUser.toString();


    }

}

