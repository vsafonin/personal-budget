package ru.vladimir.personalaccounter.controller;

import java.util.Currency;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import ru.vladimir.personalaccounter.entity.AdjustmentTransaction;
import ru.vladimir.personalaccounter.entity.AppUser;
import ru.vladimir.personalaccounter.exception.UserGetDataSecurityExp;
import ru.vladimir.personalaccounter.exception.UserNotFoundException;
import ru.vladimir.personalaccounter.exception.UsrTransactionNotFoundExp;
import ru.vladimir.personalaccounter.service.AdjustmentTransactionService;
import ru.vladimir.personalaccounter.service.UserService;
/**
 * The Controller for work with Adjustment Transaction Entity over web UI.
 * <p>
 * This class handles the CRUD operations for Adjustment Transaction entity via HTTP actions
 * as result return HTML based web pages using thymeleaf template.
 * </p>
 * @author Vladimir Afonin
 *
 */
@Controller
public class AdjustmentTransactionController {
	@Autowired
	private AdjustmentTransactionService transactionService;
	
	
	@Autowired
	private UserService userService;
	
	/**
	 * Handles "/adjustment-transaction/{id}" and return HTML page with new or exist Adjustment transaction entity 
	 * @param id - Adjustment Transaction id, user can't create Adjustment transaction, 
	 * this transaction creates automatically
	 * when a user changes their bank account balance
	 * @param model
	 * @return -  Adjustment Transaction Account edit HTML page.
	 */
	@GetMapping("/adjustment-transaction/{id}")
	public String getTransaction(@PathVariable("id") Long id, Model model) {
		//get transaction by id

		AdjustmentTransaction adjustmentTransaction = transactionService.getTransactioById(id);
		if (adjustmentTransaction == null) {
			throw new UsrTransactionNotFoundExp();
		}
		
		model.addAttribute("transaction", adjustmentTransaction);
		//add currency SET
		model.addAttribute("currencySet", Currency.getAvailableCurrencies());
		
		return "transaction-adjustment-edit-page";
	}
	/**
	 * Handles post requests to "/adjustment-transaction/{id}" and transfers object to service layer for save changes.
	 * If Adjustment Transaction change TypeOperation or BankAccount or Sum of transaction, sends to service layer
	 * request for deleting old transaction and create new.
	 * @param id - Adjustment Transaction id
	 * @param adjustmentTransacion - Adjustment Transaction object
	 * @param model
	 * @return - if no errors, redirects to /bank-account/{id} where id is bankAccount 
	 * that owner of this transaction
	 */
	@PostMapping("/adjustment-transaction/{id}")
	public String saveTransaction(@PathVariable("id") Long id, 
			@ModelAttribute("transaction") AdjustmentTransaction  adjustmentTransacion,
			Model model) {
		if (adjustmentTransacion == null) {
			throw new UsrTransactionNotFoundExp();
		}
		AdjustmentTransaction oldTransaction = transactionService.getTransactioById(id);
		if (oldTransaction == null) {
			throw new UsrTransactionNotFoundExp("old transaction doesn't exist");
		}

		if (oldTransaction.getTypeOfOperation() != adjustmentTransacion.getTypeOfOperation() || 
				oldTransaction.getBankAccount().equals(adjustmentTransacion.getBankAccount())
				|| oldTransaction.getSumTransaction()!= adjustmentTransacion.getSumTransaction()
				) {
			//easy way delete old transaction, and create new with new parameters. 
			transactionService.delete(oldTransaction);			
		}
		transactionService.save(adjustmentTransacion);		
		
		return "redirect:/bank-account/" + adjustmentTransacion.getBankAccount().getId();
	}
	/**
	 * Handles request for "/adjustment-transaction/delete/{id}" and use for deleting Adjustment Transaction via WEB UI
	 * @param id - Adjustment Transaction id which we want to delete
	 * @return - if no errors, redirects to /bank-account/{id} where id is bankAccount 
	 * that was owner of this transaction
	 */
	@GetMapping("/transaction/delete/{id}")
	public String deleteTransaction(@PathVariable("id") Long id	) {
		//check user right
		AppUser appUser = userService.getCurrentAppUserFromContextOrCreateDemoUser();
		if (appUser == null) {
			throw new UserNotFoundException("user must be being");
		}
		//get transaction
		AdjustmentTransaction adjustmentTransaction = transactionService.getTransactioById(id);
		if (adjustmentTransaction == null) {
			throw new UsrTransactionNotFoundExp();
		}
		if (!adjustmentTransaction.getAppUser().equals(appUser)) {
			throw new UserGetDataSecurityExp("someone want to get transaction, but he isn't owner ");
		}
		//delete it
		transactionService.delete(adjustmentTransaction);
                return "redirect:/bank-account/" + adjustmentTransaction.getBankAccount().getId();
	}
	

}