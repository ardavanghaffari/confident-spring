package io.github.ardavanghaffari.myfancypdfinvoices.web;

import io.github.ardavanghaffari.myfancypdfinvoices.dto.InvoiceDto;
import io.github.ardavanghaffari.myfancypdfinvoices.model.Invoice;
import io.github.ardavanghaffari.myfancypdfinvoices.service.InvoiceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Validated
public class InvoicesController {

    private final InvoiceService invoiceService;

    @GetMapping("/invoices")
    // Equivalent to @RequestMapping(value = "/invoices", method = RequestMethod.GET)
    public List<Invoice> invoices() {
        return invoiceService.findAll();
    }

    @PostMapping("/invoices")
    public Invoice createInvoice(@RequestBody @Valid InvoiceDto invoiceDto) {

        return invoiceService.create(invoiceDto.getUserId(), invoiceDto.getAmount());
    }

//    @PostMapping("/invoices")
//    public Invoice createInvoice(@RequestParam("user_id") @NotBlank String userId,
//                                 @RequestParam @Min(10) @Max(50) Integer amount) {
//
//        return invoiceService.create(userId, amount);
//    }

//    @PathVariable example
//    @PostMapping("/invoices/{userId}/{amount}")
//    public Invoice createInvoice(@PathVariable String userId, @PathVariable Integer amount) {
//        return invoiceService.create(userId, amount);
//    }

}
