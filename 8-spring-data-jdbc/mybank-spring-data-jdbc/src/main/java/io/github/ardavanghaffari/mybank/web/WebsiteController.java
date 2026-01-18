package io.github.ardavanghaffari.mybank.web;

import io.github.ardavanghaffari.mybank.service.TransactionService;
import io.github.ardavanghaffari.mybank.web.forms.TransactionForm;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class WebsiteController {

    private final TransactionService transactionService;
    private final String bankSlogan;

    public WebsiteController(TransactionService transactionService, @Value("${bank.slogan}") String bankSlogan) {
        this.transactionService = transactionService;
        this.bankSlogan = bankSlogan;
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("slogan", bankSlogan);
        return "index.html";
    }

    @GetMapping("/account/{userId}")
    public String transactionList(Model model, @PathVariable("userId") String userId) {
        buildAccountPageModel(model, userId);
        model.addAttribute("txForm", new TransactionForm());
        return "account.html";
    }

    @PostMapping("/account/{userId}")
    public String newTransaction(@ModelAttribute("txForm") @Valid TransactionForm txForm,
                                 BindingResult bindingResult,
                                 @PathVariable("userId") String userId,
                                 Model model) {

        buildAccountPageModel(model, userId);

        if (bindingResult.hasErrors()) {
            model.addAttribute("txError", true);
            return "account.html";
        }

        createTransaction(txForm);
        return "redirect:/account/" + userId;
    }

    private void createTransaction(TransactionForm txForm) {
        transactionService.create(txForm.getAmount(), txForm.getReference(), txForm.getReceivingUser());
    }

    private void buildAccountPageModel(Model model, String userId) {
        model.addAttribute("userId", userId);
        model.addAttribute("transactions", transactionService.findByReceivingUserId(userId));
    }
}
