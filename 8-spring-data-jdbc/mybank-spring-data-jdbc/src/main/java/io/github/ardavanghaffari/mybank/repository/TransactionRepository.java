package io.github.ardavanghaffari.mybank.repository;

import io.github.ardavanghaffari.mybank.model.Transaction;
import org.springframework.data.repository.CrudRepository;

public interface TransactionRepository extends CrudRepository<Transaction, String> {

    Iterable<Transaction> findByReceivingUser(String receivingUser);
}
