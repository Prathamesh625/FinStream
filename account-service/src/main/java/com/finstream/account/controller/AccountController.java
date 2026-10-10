package com.finstream.account.controller;

import com.finstream.account.entity.Account;
import com.finstream.account.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/accounts")
@CrossOrigin(origins = "*")
public class AccountController {

    @Autowired
    private AccountService accountService;

    @PostMapping
    public ResponseEntity<Account> createAccount(@RequestBody Map<String, Object> req) {
        String accountId = (String) req.getOrDefault("accountId", "ACC-" + System.currentTimeMillis());
        String ownerName = (String) req.getOrDefault("ownerName", "Standard User");
        BigDecimal balance = new BigDecimal(req.getOrDefault("balance", 10000.0).toString());
        String currency = (String) req.getOrDefault("currency", "USD");

        Account acc = accountService.createAccount(accountId, ownerName, balance, currency);
        return ResponseEntity.ok(acc);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Account> getAccount(@PathVariable String id) {
        return accountService.getAccount(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<Account>> getAllAccounts() {
        return ResponseEntity.ok(accountService.getAllAccounts());
    }
}
