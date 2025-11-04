package com.bankapp.service;

import com.bankapp.model.Account;
import com.bankapp.model.User;
import com.bankapp.repository.AccountRepository;
import com.bankapp.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final AccountService accountService;

    public AuthService(UserRepository userRepository, AccountRepository accountRepository, PasswordEncoder passwordEncoder, AccountService accountService) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
        this.accountService = accountService;
    }

    @Transactional
    public void register(String username, String rawPassword, String role, String email) {
        if (userRepository.existsByUsername(username)) throw new IllegalArgumentException("Username already exists");
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setRole(role == null ? "USER" : role);
        user.setEnabled(true);
        user.setEmail(email);
        userRepository.save(user);

        Account acc = new Account();
        acc.setUser(user);
        acc.setAccountNumber(accountService.generateAccountNumber());
        acc.setBalance(java.math.BigDecimal.ZERO);
        accountRepository.save(acc);
        user.setAccount(acc);
        userRepository.save(user);
    }
}
