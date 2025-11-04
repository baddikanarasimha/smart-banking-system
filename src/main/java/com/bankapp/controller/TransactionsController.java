package com.bankapp.controller;

import com.bankapp.model.Account;
import com.bankapp.service.AccountService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TransactionsController {

    private final AccountService accountService;

    public TransactionsController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/transactions")
    public String history(@AuthenticationPrincipal User user, Model model) {
        Account acc = accountService.getAccountForUser(user.getUsername());
        model.addAttribute("account", acc);
        model.addAttribute("transactions", accountService.getTransactionsForAccount(acc.getAccountNumber()));
        return "transactions";
    }
}
