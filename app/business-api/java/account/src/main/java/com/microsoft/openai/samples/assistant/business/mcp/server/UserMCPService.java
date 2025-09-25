package com.microsoft.openai.samples.assistant.business.mcp.server;

import com.microsoft.openai.samples.assistant.business.models.Account;
import com.microsoft.openai.samples.assistant.business.service.UserService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
public class UserMCPService {
    private static final Logger logger = LoggerFactory.getLogger(UserMCPService.class);
    private final UserService userService;

    public UserMCPService(UserService userService) {
        this.userService = userService;
     }

     @Tool(description = "Get the list of all accounts for a specific user")
    public List<Account> getAccountsByUserName(@ToolParam( description ="userName once the user has logged" ) String userName) {
        logger.info("getAccountsByUserName called with userName={}", userName);
        return userService.getAccountsByUserName(userName);
    }


}
