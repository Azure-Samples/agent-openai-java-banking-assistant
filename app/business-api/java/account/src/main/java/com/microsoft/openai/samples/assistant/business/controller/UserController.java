package com.microsoft.openai.samples.assistant.business.controller;

import com.microsoft.openai.samples.assistant.business.models.Account;
import com.microsoft.openai.samples.assistant.business.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);


    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }
    @GetMapping("/{userName}/accounts")
    public List<Account> getAccountsByUserName(@PathVariable String userName) {
        // Implement the logic to get the list of all accounts for a specific user
        logger.info("Received request to get accounts for user: {}", userName);
        return userService.getAccountsByUserName(userName);
    }
}
