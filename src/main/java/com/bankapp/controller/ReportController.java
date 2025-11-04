package com.bankapp.controller;

import com.bankapp.model.Account;
import com.bankapp.model.Transaction;
import com.bankapp.service.AccountService;
import com.bankapp.service.StatementService;
import com.bankapp.repository.TransactionRepository;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class ReportController {

    private final AccountService accountService;
    private final TransactionRepository transactionRepository;
    private final StatementService statementService;

    public ReportController(AccountService accountService, TransactionRepository transactionRepository, StatementService statementService) {
        this.accountService = accountService;
        this.transactionRepository = transactionRepository;
        this.statementService = statementService;
    }

    @GetMapping(value = "/statement.pdf")
    public ResponseEntity<byte[]> statement(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        Account acc = accountService.getAccountForUser(user.getUsername());
        LocalDate fromDate = (from == null ? LocalDate.now().minusMonths(1) : from);
        LocalDate toDate = (to == null ? LocalDate.now() : to);
        LocalDateTime start = fromDate.atStartOfDay();
        LocalDateTime end = toDate.atTime(LocalTime.MAX);
        List<Transaction> tx = transactionRepository.findForAccountBetween(acc.getAccountNumber(), start, end);
        byte[] pdf = statementService.generatePdf(acc.getAccountNumber(), tx, fromDate, toDate);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=statement-" + acc.getAccountNumber() + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("/analytics/data")
    @ResponseBody
    public Map<String, Object> analytics(@AuthenticationPrincipal User user) {
        Account acc = accountService.getAccountForUser(user.getUsername());
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(29);
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = today.atTime(LocalTime.MAX);
        List<Transaction> list = transactionRepository.findForAccountBetween(acc.getAccountNumber(), start, end);

        double[] credits = new double[30];
        double[] debits = new double[30];
        int[] creditsCount = new int[30];
        int[] debitsCount = new int[30];
        String[] labels = new String[30];
        for (int i = 0; i < 30; i++) {
            LocalDate d = startDate.plusDays(i);
            labels[i] = d.toString();
        }
        double totalCredits = 0.0;
        double totalDebits = 0.0;
        int depositCount = 0;
        int withdrawCount = 0;
        int transferInCount = 0;
        int transferOutCount = 0;
        for (Transaction t : list) {
            LocalDate d = t.getTimestamp().toLocalDate();
            int idx = (int) (d.toEpochDay() - startDate.toEpochDay());
            if (idx < 0 || idx >= 30) continue;
            boolean credit = acc.getAccountNumber().equals(t.getReceiverAccount());
            boolean debit = acc.getAccountNumber().equals(t.getSenderAccount());
            if (credit) { credits[idx] += t.getAmount().doubleValue(); creditsCount[idx] += 1; totalCredits += t.getAmount().doubleValue(); }
            if (debit) { debits[idx] += t.getAmount().doubleValue(); debitsCount[idx] += 1; totalDebits += t.getAmount().doubleValue(); }

            // Type counts from perspective of current account
            String type = t.getType();
            if ("DEPOSIT".equalsIgnoreCase(type) && credit) depositCount++;
            else if ("WITHDRAW".equalsIgnoreCase(type) && debit) withdrawCount++;
            else if ("TRANSFER".equalsIgnoreCase(type)) {
                if (credit) transferInCount++;
                if (debit) transferOutCount++;
            }
        }
        Map<String, Object> res = new HashMap<>();
        res.put("labels", labels);
        res.put("credits", credits);
        res.put("debits", debits);
        res.put("creditsCount", creditsCount);
        res.put("debitsCount", debitsCount);
        Map<String, Object> totals = new HashMap<>();
        totals.put("totalCredits", totalCredits);
        totals.put("totalDebits", totalDebits);
        totals.put("net", totalCredits - totalDebits);
        res.put("totals", totals);
        Map<String, Integer> typeCounts = new HashMap<>();
        typeCounts.put("deposit", depositCount);
        typeCounts.put("withdraw", withdrawCount);
        typeCounts.put("transferIn", transferInCount);
        typeCounts.put("transferOut", transferOutCount);
        res.put("typeCounts", typeCounts);
        return res;
    }
}
