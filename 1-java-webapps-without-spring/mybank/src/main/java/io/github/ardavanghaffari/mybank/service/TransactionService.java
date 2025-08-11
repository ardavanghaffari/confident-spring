package io.github.ardavanghaffari.mybank.service;

import io.github.ardavanghaffari.mybank.model.Transaction;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class TransactionService {

    private final List<Transaction> transactions = new CopyOnWriteArrayList<>();

    public List<Transaction> findAll() {
        return transactions;
    }

    public Transaction create(BigDecimal amount, String reference) {
        Transaction tx = new Transaction(amount, Instant.now(), reference);
        transactions.add(tx);
        return tx;
    }

}
