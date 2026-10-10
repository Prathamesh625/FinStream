package com.finstream.account.service;

import com.finstream.account.entity.Account;
import com.finstream.account.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class AccountService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired(required = false)
    private StringRedisTemplate redisTemplate;

    public Account createAccount(String accountId, String ownerName, BigDecimal initialBalance, String currency) {
        Account account = new Account(accountId, ownerName, initialBalance, currency);
        Account saved = accountRepository.save(account);
        cacheBalance(saved.getAccountId(), saved.getBalance());
        return saved;
    }

    public Optional<Account> getAccount(String accountId) {
        if (redisTemplate != null) {
            String cached = redisTemplate.opsForValue().get("account:balance:" + accountId);
            if (cached != null) {
                Optional<Account> dbAcc = accountRepository.findById(accountId);
                dbAcc.ifPresent(acc -> acc.setBalance(new BigDecimal(cached)));
                return dbAcc;
            }
        }
        Optional<Account> acc = accountRepository.findById(accountId);
        acc.ifPresent(a -> cacheBalance(a.getAccountId(), a.getBalance()));
        return acc;
    }

    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }

    @Transactional
    public boolean updateBalances(String srcAccountId, String destAccountId, BigDecimal amount) {
        Optional<Account> srcOpt = accountRepository.findById(srcAccountId);
        Optional<Account> destOpt = accountRepository.findById(destAccountId);

        if (srcOpt.isEmpty() || destOpt.isEmpty()) {
            return false;
        }

        Account src = srcOpt.get();
        Account dest = destOpt.get();

        if (src.getBalance().compareTo(amount) < 0) {
            return false;
        }

        src.setBalance(src.getBalance().subtract(amount));
        src.setUpdatedAt(Instant.now());

        dest.setBalance(dest.getBalance().add(amount));
        dest.setUpdatedAt(Instant.now());

        accountRepository.save(src);
        accountRepository.save(dest);

        cacheBalance(src.getAccountId(), src.getBalance());
        cacheBalance(dest.getAccountId(), dest.getBalance());

        return true;
    }

    private void cacheBalance(String accountId, BigDecimal balance) {
        if (redisTemplate != null) {
            try {
                redisTemplate.opsForValue().set("account:balance:" + accountId, balance.toString(), Duration.ofMinutes(10));
            } catch (Exception ignored) {}
        }
    }
}
