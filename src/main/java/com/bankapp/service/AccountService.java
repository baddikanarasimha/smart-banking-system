package com.bankapp.service;

import com.bankapp.model.Account;
import com.bankapp.model.Transaction;
import com.bankapp.model.User;
import com.bankapp.repository.AccountRepository;
import com.bankapp.repository.TransactionRepository;
import com.bankapp.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.List;

@Service
public class AccountService {
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    public AccountService(AccountRepository accountRepository, TransactionRepository transactionRepository, UserRepository userRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
    }

    public Account getAccountForUser(String username) {
        User user = userRepository.findByUsername(username).orElseThrow();
        return user.getAccount();
    }

    public List<Transaction> getTransactionsForAccount(String accountNumber) {
        return transactionRepository.findBySenderAccountOrReceiverAccountOrderByTimestampDesc(accountNumber, accountNumber);
    }

    @Transactional
    public void deposit(String accountNumber, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) throw new IllegalArgumentException("Amount must be positive");
        Account acc = accountRepository.findByAccountNumber(accountNumber).orElseThrow();
        acc.setBalance(acc.getBalance().add(amount));
        accountRepository.save(acc);

        Transaction t = new Transaction();
        t.setType("DEPOSIT");
        t.setReceiverAccount(accountNumber);
        t.setAmount(amount);
        transactionRepository.save(t);
    }

    @Transactional
    public void withdraw(String accountNumber, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) throw new IllegalArgumentException("Amount must be positive");
        Account acc = accountRepository.findByAccountNumber(accountNumber).orElseThrow();
        if (acc.getBalance().compareTo(amount) < 0) throw new IllegalArgumentException("Insufficient funds");
        acc.setBalance(acc.getBalance().subtract(amount));
        accountRepository.save(acc);

        Transaction t = new Transaction();
        t.setType("WITHDRAW");
        t.setSenderAccount(accountNumber);
        t.setAmount(amount);
        transactionRepository.save(t);
    }

    @Transactional
    public void transfer(String fromAccount, String toAccount, BigDecimal amount) {
        if (fromAccount.equals(toAccount)) throw new IllegalArgumentException("Cannot transfer to same account");
        if (amount.compareTo(BigDecimal.ZERO) <= 0) throw new IllegalArgumentException("Amount must be positive");
        Account sender = accountRepository.findByAccountNumber(fromAccount).orElseThrow();
        Account receiver = accountRepository.findByAccountNumber(toAccount).orElseThrow();
        if (sender.getBalance().compareTo(amount) < 0) throw new IllegalArgumentException("Insufficient funds");

        sender.setBalance(sender.getBalance().subtract(amount));
        receiver.setBalance(receiver.getBalance().add(amount));
        accountRepository.save(sender);
        accountRepository.save(receiver);

        Transaction t1 = new Transaction();
        t1.setType("TRANSFER");
        t1.setSenderAccount(fromAccount);
        t1.setReceiverAccount(toAccount);
        t1.setAmount(amount);
        transactionRepository.save(t1);
    }

    public String generateAccountNumber() {
        SecureRandom r = new SecureRandom();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 12; i++) sb.append(r.nextInt(10));
        return sb.toString();
    }
}
