package io.github.ardavanghaffari.mybank.service;

import io.github.ardavanghaffari.mybank.model.Transaction;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

@Service
public class TransactionService {

    private final JdbcTemplate jdbcTemplate;
    private final String bankSlogan;

    public TransactionService(JdbcTemplate jdbcTemplate,
                              @Value("${bank.slogan}") String bankSlogan) {
        this.jdbcTemplate = jdbcTemplate;
        this.bankSlogan = bankSlogan;
    }

    @Transactional
    public List<Transaction> findAll() {
        System.out.println("Is a database transaction open? = " +
                TransactionSynchronizationManager.isActualTransactionActive());

        return jdbcTemplate.query("SELECT id, amount, timestamp, reference, bank_slogan, receiving_user FROM TRANSACTIONS",
                TransactionService::mapRow);
    }

    @Transactional
    public List<Transaction> findByReceivingUserId(String userId) {
        System.out.println("Is a database transaction open? = " +
                TransactionSynchronizationManager.isActualTransactionActive());

        return jdbcTemplate.query(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "SELECT tx.id, tx.amount, tx.timestamp, tx.reference, tx.bank_slogan, tx.receiving_user FROM TRANSACTIONS tx WHERE tx.receiving_user = ?");
            ps.setString(1, userId);
            return ps;
        }, TransactionService::mapRow);
    }

    @Transactional
    public Transaction create(BigDecimal amount, String reference, String receivingUser) {
        System.out.println("Is a database transaction open? = " +
                TransactionSynchronizationManager.isActualTransactionActive());

        Instant timestamp = Instant.now();
        Transaction transaction = new Transaction(amount, timestamp, reference, bankSlogan, receivingUser);

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO TRANSACTIONS (amount, timestamp, reference, bank_slogan, receiving_user) VALUES (?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setBigDecimal(1, transaction.getAmount());
            ps.setTimestamp(2, Timestamp.from(transaction.getTimestamp()));
            ps.setString(3, transaction.getReference());
            ps.setString(4, transaction.getBankSlogan());
            ps.setString(5, transaction.getReceivingUser());
            return ps;
        }, keyHolder);

        String uuid = !keyHolder.getKeys().isEmpty() ? keyHolder.getKeys().values().iterator().next().toString() : null;
        transaction.setId(uuid);
        return transaction;
    }

    private static Transaction mapRow(ResultSet rs, int rowNum) throws SQLException {
        Transaction tx = new Transaction();
        tx.setId(rs.getObject("id").toString());
        tx.setAmount(rs.getBigDecimal("amount"));
        tx.setTimestamp(rs.getTimestamp("timestamp").toInstant());
        tx.setReference(rs.getString("reference"));
        tx.setBankSlogan(rs.getString("bank_slogan"));
        tx.setReceivingUser(rs.getString("receiving_user"));
        return tx;
    }

}
