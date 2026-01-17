package io.github.ardavanghaffari.myfancypdfinvoices.service;

import io.github.ardavanghaffari.myfancypdfinvoices.model.Invoice;
import io.github.ardavanghaffari.myfancypdfinvoices.repository.InvoiceRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final String cdnUrl;

    public InvoiceService(InvoiceRepository invoiceRepository, @Value("${cdn.url}") String cdnUrl) {
        this.invoiceRepository = invoiceRepository;
        this.cdnUrl = cdnUrl;
    }

    @Transactional
    public Iterable<Invoice> findAll() {
        return invoiceRepository.findAll();
    }

    public Iterable<Invoice> findByUserId(String userId) {
        return invoiceRepository.findByUserId(userId);
    }

    @Transactional
    public Invoice create(String userId, Integer amount) {
        String generatedPdfUrl = cdnUrl + "/pdf/sample.pdf";

        Invoice invoice = new Invoice();
        invoice.setPdfUrl(generatedPdfUrl);
        invoice.setAmount(amount);
        invoice.setUserId(userId);

        return invoiceRepository.save(invoice);
    }
}
