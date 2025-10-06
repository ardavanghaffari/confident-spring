package io.github.ardavanghaffari.myfancypdfinvoices.service;

import io.github.ardavanghaffari.myfancypdfinvoices.model.Invoice;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

@Component
public class InvoiceService {

    private final JdbcTemplate jdbcTemplate;
    private final UserService userService;
    private final String cdnUrl;

    public InvoiceService(JdbcTemplate jdbcTemplate,
                          UserService userService,
                          @Value("${cdn.url}") String cdnUrl) {
        this.jdbcTemplate = jdbcTemplate;
        this.userService = userService;
        this.cdnUrl = cdnUrl;
    }

    @Transactional
    public List<Invoice> findAll() {
        System.out.println("Is a database transaction open? = " +
                TransactionSynchronizationManager.isActualTransactionActive());

        return jdbcTemplate.query("select id, user_id, pdf_url, amount from invoices",
                (resultSet, rowNum) -> {
                    Invoice invoice = new Invoice();
                    invoice.setId(resultSet.getObject("id").toString());
                    invoice.setPdfUrl(resultSet.getString("pdf_url"));
                    invoice.setUserId(resultSet.getString("user_id"));
                    invoice.setAmount(resultSet.getInt("amount"));
                    return invoice;
                });
    }

    /*
     * Invoice's id is an auto-generated value and we want to retrieve that value from the database,
     * automatically, right after inserting an invoice.
     *
     * Returning generated primary keys from the database is a bit of a pain. You need to create a
     * preparedStatement, with a magic variable Statement.RETURN_GENERATED_KEY set to true. Only
     * then, your JDBC driver will make sure to make the generated id available to you via a
     * KeyHolder object, that you need to pass into the JDBCTemplate, while executing your insert
     * statement.
     *
     * Setting parameters on the PreparedStatment itself is plain JDBC code. These parameters will
     * replace the ? question marks in your SQL with the proper values and automatically safeguard
     * against SQL-injections.
     *
     * */
    @Transactional
    public Invoice create(String userId, Integer amount) {
        System.out.println("Is a database transaction open? = " +
                TransactionSynchronizationManager.isActualTransactionActive());

        String generatedPdfUrl = cdnUrl + "/pdf/sample.pdf";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        String statement = "insert into invoices (user_id, pdf_url, amount) values (?, ?, ?)";

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection
                    .prepareStatement(statement, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, userId);
            ps.setString(2, generatedPdfUrl);
            ps.setInt(3, amount);
            return ps;
        }, keyHolder);

        String uuid = !keyHolder.getKeys().isEmpty() ?
                keyHolder.getKeys().values().iterator().next().toString() : null;

        Invoice invoice = new Invoice();
        invoice.setId(uuid);
        invoice.setPdfUrl(generatedPdfUrl);
        invoice.setAmount(amount);
        invoice.setUserId(userId);
        return invoice;
    }
}
