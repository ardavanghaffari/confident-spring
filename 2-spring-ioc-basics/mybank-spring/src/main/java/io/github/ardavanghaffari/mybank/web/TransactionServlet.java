package io.github.ardavanghaffari.mybank.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.ardavanghaffari.mybank.model.Transaction;
import io.github.ardavanghaffari.mybank.service.TransactionService;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TransactionServlet extends HttpServlet {

    private final TransactionService transactionService;
    private final ObjectMapper objectMapper;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (req.getRequestURI().equalsIgnoreCase("/transactions")) {
            List<Transaction> transactions = transactionService.findAll();
            resp.setContentType("application/json; charset=UTF-8");
            resp.getWriter().println(objectMapper.writeValueAsString(transactions));
        } else {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (req.getRequestURI().equalsIgnoreCase("/transactions")) {

            BigDecimal amount = BigDecimal.valueOf(Double.parseDouble(req.getParameter("amount")));
            String reference = req.getParameter("reference");

            Transaction transaction = transactionService.create(amount, reference);

            resp.setContentType("application/json; charset=UTF-8");
            resp.getWriter().print(objectMapper.writeValueAsString(transaction));

        } else {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }

}
