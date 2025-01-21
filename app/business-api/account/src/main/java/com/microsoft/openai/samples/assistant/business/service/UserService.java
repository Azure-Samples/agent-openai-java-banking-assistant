package com.microsoft.openai.samples.assistant.business.service;

import com.microsoft.openai.samples.assistant.business.models.Account;
import org.springframework.stereotype.Service;

import java.util.*;


@Service
public class UserService {


    private Map<String, Account> accounts = new HashMap<>();

    public UserService() {
        accounts.put(
                "alice.user@contoso.com",
                new Account(
                        "1000",
                        "alice.user@contoso.com",
                        "Alice User",
                        "USD",
                        "2022-01-01",
                        "5000",
                       null
                )
        );
        accounts.put(
                "bob.user@contoso.com",
                new Account(
                        "1010",
                        "bob.user@contoso.com",
                        "Bob User",
                        "EUR",
                        "2022-01-01",
                        "10000",
                       null
                )
        );
        accounts.put(
                "charlie.user@contoso.com",
                new Account(
                        "1020",
                        "charlie.user@contoso.com",
                        "Charlie User",
                        "EUR",
                        "2022-01-01",
                        "3000",
                        null
                )
        );

     }
    public List<Account> getAccountsByUserName(String userName) {
        return Arrays.asList(accounts.get(userName));
    }


}
