package com.microsoft.openai.samples.assistant.business.service;

import com.microsoft.openai.samples.assistant.business.models.Account;
import org.springframework.stereotype.Service;

import java.util.*;


@Service
public class UserService {


    private Map<String, Account> accounts = new HashMap<>();

    public UserService() {
        accounts.put(
                "alice.user@microsoft.com",
                new Account(
                        "1000",
                        "alice.user@microsoft.com",
                        "Alice User",
                        "USD",
                        "2022-01-01",
                        "1000.00",
                       null
                )
        );
        accounts.put(
                "bob.user@microsoft.com",
                new Account(
                        "1010",
                        "bob.user@microsoft.com",
                        "Bob User",
                        "EUR",
                        "2022-01-01",
                        "2000,40",
                       null
                )
        );
        accounts.put(
                "charlie.user@microsoft.com",
                new Account(
                        "1020",
                        "charlie.user@microsoft.com",
                        "Charlie User",
                        "EUR",
                        "2022-01-01",
                        "3000,20",
                        null
                )
        );

     }
    public List<Account> getAccountsByUserName(String userName) {
        return Arrays.asList(accounts.get(userName));
    }


}
