package com.bankapp.controller;

import com.bankapp.model.Account;
import com.bankapp.service.AccountService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    private final AccountService accountService;

    public DashboardController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping({"/", "/dashboard"})
    public String dashboard(@AuthenticationPrincipal User user, Model model) {
        Account acc = accountService.getAccountForUser(user.getUsername());
        model.addAttribute("account", acc);
        model.addAttribute("transactions", accountService.getTransactionsForAccount(acc.getAccountNumber()));
        return "dashboard";
    }
}
