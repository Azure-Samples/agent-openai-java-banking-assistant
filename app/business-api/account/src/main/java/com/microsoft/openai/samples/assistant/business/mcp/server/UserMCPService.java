package com.microsoft.openai.samples.assistant.business.mcp.server;

import com.microsoft.openai.samples.assistant.business.models.Account;
import com.microsoft.openai.samples.assistant.business.service.UserService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class UserMCPService {
    private final UserService userService;

    public UserMCPService(UserService userService) {
        this.userService = userService;
     }

     @Tool(description = "Get the list of all accounts for a specific user")
    public List<Account> getAccountsByUserName(@ToolParam( description ="userName once the user has logged" ) String userName) {
        return userService.getAccountsByUserName(userName);
    }


}
