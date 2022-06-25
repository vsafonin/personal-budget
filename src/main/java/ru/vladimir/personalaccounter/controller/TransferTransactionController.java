package ru.vladimir.personalaccounter.controller;

import java.sql.Timestamp;
import java.util.Locale;
import java.util.NoSuchElementException;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import lombok.extern.slf4j.Slf4j;
import ru.vladimir.personalaccounter.client.CurrencyParseExcp;
import ru.vladimir.personalaccounter.entity.AppUser;
import ru.vladimir.personalaccounter.entity.TransferTransaction;
import ru.vladimir.personalaccounter.exception.TransferTransactionNotFoundExcp;
import ru.vladimir.personalaccounter.service.TransferTransactionService;
import ru.vladimir.personalaccounter.service.UserService;
/**
 * The Controller for work with Transfer Transaction Entity over web UI.
 * <p>
 * This class handles the CRUD operations for Transfer Transaction entity via HTTP actions
 * as result returns HTML based web pages using thymeleaf template.
 * </p>
 * @author Vladimir Afonin
 *
 */
@Controller
@Slf4j
public class TransferTransactionController {
	
	@Autowired
	private TransferTransactionService transferTransactionService;
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private MessageSource messageSource;
	
	/**
	 * Handles "/transfer/{id}" requests and return HTML edit page for create or save TransferTransaction entity. 
	 * @param id - TransferTransaction id, if id == 0 this request for creating new Transfer Transaction
	 * @param model
	 * @return - returns HTML edit page for edit/create TransferTransaction
	 */
	@GetMapping("/transfer/{id}")
	public String getTransfer(@PathVariable("id") long id, Model model) {
		TransferTransaction theTransaction;
		if (id == 0) {
		AppUser appUser = userService.getCurrentAppUserFromContextOrCreateDemoUser();
		theTransaction = new TransferTransaction();
		theTransaction.setAppUser(appUser);
		theTransaction.setCreateTime(new Timestamp(System.currentTimeMillis()));
		}
		else {
			try {
				theTransaction = transferTransactionService.getTransferTransactionById(id);
			}
			catch (NoSuchElementException exp) {
				throw new TransferTransactionNotFoundExcp();
			}
		}
		model.addAttribute("transferTransaction",theTransaction);
		return "transfer-betwen-bank";
	}
	
	/**
	 * Handles POST requests to "/transfer" check valid of filling 
	 * in the fields and transfers object to service layer for save changes
	 * @param model
	 * @param theTransferTransaction - TransferTransaction object for save in DB. 
	 * @param bindingResult
	 * @param locale - User locale in their browser.
	 * @return - if everything is OK redirects to list of Transfer Transactions page, otherwise returns edit page again
	 */	
	@PostMapping("/transfer")
	public String saveTransfer(Model model, @Valid @ModelAttribute("transferTransaction") TransferTransaction theTransferTransaction,
			BindingResult bindingResult, Locale locale) {
		if(bindingResult.hasErrors()) {
			return "transfer-betwen-bank";
		}
		if (theTransferTransaction.getFromBankAccount().equals(theTransferTransaction.getToBankAccount())) {
			String localizedMessage = messageSource.getMessage("transaction.trnsferTransaction.error.bankEquals", null,locale);
			model.addAttribute("error", localizedMessage);
			return "transfer-betwen-bank";
		}
		try {
			transferTransactionService.saveTransferTransaction(theTransferTransaction);
		}
		catch (CurrencyParseExcp exp) {
			log.error(exp.getMessage());
			model.addAttribute("error",exp.getMessage());
			return "transfer-betwen-bank";
		}
		catch (TransferTransactionNotFoundExcp exp) {
			log.error(exp.getMessage());
		}
		return "redirect:/transfer";
	}
	
	/**
	 * Handles request to "/transfer/delete/{id}" and serves for delete TransferTransaction entity from WEB UI
	 * @param id - Transfer Transaction id which we want to delete. 
	 * @return - redirect to list of Transfer Transactions page, or trhows TransferTransactionNotFoundExcp
	 */
	@GetMapping("/transfer/delete/{id}")
	public String deleteTransfer(@PathVariable("id") long id) {
		try {
		TransferTransaction theTransferTransaction = transferTransactionService.getTransferTransactionById(id);
		transferTransactionService.deleteTransferTransaction(theTransferTransaction);
		return "redirect:/transfer";
		}
		catch (NoSuchElementException exp) {
			throw new TransferTransactionNotFoundExcp();
		}
		
	}
}
