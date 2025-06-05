package ru.terentyev.pionerpixel_testtask.schedulers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.terentyev.pionerpixel_testtask.models.Account;
import ru.terentyev.pionerpixel_testtask.repositories.AccountRepository;

import java.math.BigDecimal;
import java.util.List;

@Component
public class BalanceIncreaseScheduler {
    private static final Logger logger = LoggerFactory.getLogger(BalanceIncreaseScheduler.class);

    private final AccountRepository accountRepository;
    private static final BigDecimal MAX_INCREASE_PERCENT = new BigDecimal("2.07");
    private static final BigDecimal INCREASE_PERCENT_PER_RUN = new BigDecimal("0.10");

    @Autowired
    public BalanceIncreaseScheduler(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Scheduled(fixedRate = 30000) // Раз в 30 секунд
    @Transactional
    public void increaseAccountBalances() {
        logger.info("Starting scheduled balance increase.");
        List<Account> accounts = accountRepository.findAll();
        for (Account account : accounts) {
            if (account.getInitialDeposit() != null) {
                BigDecimal maxAllowedBalance = account.getInitialDeposit().multiply(BigDecimal.ONE.add(MAX_INCREASE_PERCENT));
                if (account.getBalance().compareTo(maxAllowedBalance) < 0) {
                    BigDecimal increaseAmount = account.getBalance().multiply(INCREASE_PERCENT_PER_RUN);
                    BigDecimal newBalance = account.getBalance().add(increaseAmount);
                    if (newBalance.compareTo(maxAllowedBalance) > 0) {
                        newBalance = maxAllowedBalance;
                    }
                    account.setBalance(newBalance);
                    accountRepository.save(account);
                    logger.debug("Increased balance for account ID {}. New balance: {}", account.getId(), newBalance);
                } else {
                    logger.debug("Account ID {} has reached its maximum balance increase limit.", account.getId());
                }
            } else {
                logger.warn("Account ID {} has no initial deposit set. Skipping balance increase.", account.getId());
            }
        }
        logger.info("Scheduled balance increase finished.");
    }
}
