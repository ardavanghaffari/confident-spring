package io.github.ardavanghaffari.mybank.service;

import io.github.ardavanghaffari.mybank.model.Transaction;
import io.github.ardavanghaffari.mybank.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final String bankSlogan;

    public TransactionService(TransactionRepository transactionRepository,
                              @Value("${bank.slogan}") String bankSlogan) {
        this.transactionRepository = transactionRepository;
        this.bankSlogan = bankSlogan;
    }

    @Transactional
    public Iterable<Transaction> findAll() {
        return transactionRepository.findAll();
    }

    @Transactional
    public Iterable<Transaction> findByReceivingUserId(String userId) {
        return transactionRepository.findByReceivingUser(userId);
    }

    @Transactional
    public Transaction create(BigDecimal amount, String reference, String receivingUser) {
        Instant timestamp = Instant.now();
        Transaction transaction = new Transaction(amount, timestamp, reference, bankSlogan, receivingUser);
        return transactionRepository.save(transaction);
    }

}
