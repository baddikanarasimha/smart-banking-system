package com.bankapp.controller;

import com.bankapp.model.User;
import com.bankapp.repository.TransactionRepository;
import com.bankapp.repository.UserRepository;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

public class AdminController {

    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;

    public AdminController(UserRepository userRepository, TransactionRepository transactionRepository) {
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
    }

    @GetMapping("/admin")
    public String admin(Model model) {
        model.addAttribute("users", userRepository.findAll());
        model.addAttribute("transactions", transactionRepository.findAll());
        return "admin";
    }

    @PostMapping("/admin/toggle")
    public String toggle(@RequestParam Long userId) {
        User u = userRepository.findById(userId).orElseThrow();
        u.setEnabled(!u.isEnabled());
        userRepository.save(u);
        return "redirect:/admin";
    }
}
