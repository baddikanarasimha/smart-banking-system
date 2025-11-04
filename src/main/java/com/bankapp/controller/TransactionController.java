package com.bankapp.controller;

import com.bankapp.model.Account;
import com.bankapp.service.AccountService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

@Controller
public class TransactionController {

    private final AccountService accountService;

    public TransactionController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping("/deposit")
    public String deposit(@AuthenticationPrincipal User user,
                          @RequestParam BigDecimal amount, Model model) {
        Account acc = accountService.getAccountForUser(user.getUsername());
        try {
            accountService.deposit(acc.getAccountNumber(), amount);
            return "redirect:/dashboard";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "dashboard";
        }
    }

    @PostMapping("/withdraw")
    public String withdraw(@AuthenticationPrincipal User user,
                           @RequestParam BigDecimal amount, Model model) {
        Account acc = accountService.getAccountForUser(user.getUsername());
        try {
            accountService.withdraw(acc.getAccountNumber(), amount);
            return "redirect:/dashboard";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "dashboard";
        }
    }

    @PostMapping("/transfer")
    public String transfer(@AuthenticationPrincipal User user,
                           @RequestParam String toAccount,
                           @RequestParam BigDecimal amount, Model model) {
        Account acc = accountService.getAccountForUser(user.getUsername());
        try {
            accountService.transfer(acc.getAccountNumber(), toAccount, amount);
            return "redirect:/dashboard";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "dashboard";
        }
    }
}
