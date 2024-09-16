package com.microsoft.openai.samples.assistant.security;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class LoggedUserService {

    public LoggedUser getLoggedUser(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        //this is always true in the PoC code
        if(authentication == null) {
           return getDefaultUser();
        }
        //this code is never executed in the PoC. It's a hook for future improvements requiring integration with authentication providers.
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            String currentUserName = authentication.getName();

            Object details = authentication.getDetails();
            //object should be cast to specific type based on the authentication provider
            return new LoggedUser(currentUserName, "changeme@contoso.com", "changeme", "changeme");
        }
        return getDefaultUser();
    }

    private LoggedUser getDefaultUser() {
        return new LoggedUser("bob.user@contoso.com", "bob.user@contoso.com", "generic", "Bob The User");
    }
}
