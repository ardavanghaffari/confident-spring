package io.github.ardavanghaffari.mybank.service;

import io.github.ardavanghaffari.mybank.model.Transaction;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Service
public class TransactionService {

    private final List<Transaction> transactions = new CopyOnWriteArrayList<>();
    private final String bankSlogan;

    public TransactionService(@Value("${bank.slogan}") String bankSlogan) {
        this.bankSlogan = bankSlogan;
    }

    public List<Transaction> findAll() {
        return transactions;
    }

    public List<Transaction> findByReceivingUserId(String userId) {
        return transactions.stream()
                .filter(tx -> userId.equalsIgnoreCase(tx.getReceivingUser()))
                .toList();
    }

    public Transaction create(BigDecimal amount, String reference, String receivingUser) {
        Transaction tx = new Transaction(amount, Instant.now(), reference, bankSlogan, receivingUser);
        transactions.add(tx);
        return tx;
    }

}
