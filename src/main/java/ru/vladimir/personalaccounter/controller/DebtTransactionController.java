package ru.vladimir.personalaccounter.controller;

import java.sql.Timestamp;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import ru.vladimir.personalaccounter.entity.AppUser;
import ru.vladimir.personalaccounter.entity.DebtTransaction;
import ru.vladimir.personalaccounter.enums.TypeOfOperation;
import ru.vladimir.personalaccounter.exception.DebtTransactionNotFoundExp;
import ru.vladimir.personalaccounter.service.DebtTransactionService;
import ru.vladimir.personalaccounter.service.UserService;
/**
 * The Controller for work with Debt Transactions Entity over web UI.
 * <p>
 * This class handles the CRUD operations for Debt Transactions entity via HTTP actions
 * as result return HTML based web pages using thymeleaf template.
 * </p>
 * @author Vladimir Afonin
 *
 */
@Controller
public class DebtTransactionController {

	@Autowired
	private DebtTransactionService debtTransactionService;
	
	@Autowired
	private UserService userService;
	
	/**
	 * Handle /debt-transaction/{id} requests, return page for edit Debt transactions
	 * @param id - id of DebtTransaction entity, if id = 0 user want create new DebtTransaction
	 * @param model - Model for thymeleaf
	 * @param type - Type of Debt Transactions (INCREASE or DECREASE) user want to save.
	 * @return - Debt payment edit HTML page
	 */
	@GetMapping("/debt-transaction/{id}")
	public String getdebtTransactionCreateIncreaseNewPage(@PathVariable("id") long id,
			Model model,
			@RequestParam(name = "type", required = false) String typeOfOperation) {
		
		DebtTransaction theDebtTransaction;
		if (id == 0) {
			theDebtTransaction = new DebtTransaction();
			if (typeOfOperation.toUpperCase().equals(TypeOfOperation.INCREASE.toString())) {
				theDebtTransaction.setTypeOfOperation(TypeOfOperation.INCREASE);
			}
			else if (typeOfOperation.toUpperCase().equals(TypeOfOperation.DECREASE.toString())){
				theDebtTransaction.setTypeOfOperation(TypeOfOperation.DECREASE);
				
			}
			AppUser theAppUser = userService.getCurrentAppUserFromContextOrCreateDemoUser();
			theDebtTransaction.setAppUser(theAppUser);
			
		}
		else {
			theDebtTransaction= debtTransactionService.findById(id);
			if (theDebtTransaction == null) {
				throw new DebtTransactionNotFoundExp();
			}
		}
		model.addAttribute("debtTransaction",theDebtTransaction);
		model.addAttribute("listOfPayments", theDebtTransaction.getDebtPayment());
		return "debt-edit";
	}
	
	/**
	 * Handle /debt-transaction/delete/{id} requests, this requests delete entity from DB 
	 * and if OK redirect user to DebtTransactions List page. 
	 * @param id - DebtTransaction id
	 * @param model -  Model for thymeleaf.
	 * @return - redirect to list of DebtTransactions page.
	 */
	@GetMapping("/debt-transaction/delete/{id}")
	public String deleteDebtTransaction(@PathVariable("id")long id, Model model) {
		DebtTransaction thedebtTransaction = debtTransactionService.findById(id);
		if (thedebtTransaction == null) {
			throw new DebtTransactionNotFoundExp();
		}
		debtTransactionService.delete(thedebtTransaction);
		return "redirect:/debt";
	}
	/**
	 * Handle post request to /debt-transaction/{id}, save Entity to DB. If id == 0, create new, otherwise
	 * save changes.
	 * @param id - DebtTransaction id
	 * @param model -  Model for thymeleaf.
	 * @param debtTransaction - DebtTransaction which user want to save.
	 * @param bindingResult - result validation object DebtTransaction. We are check field partner, sum, end date. 
	 * @return if has error, return edit page  again, otherwise redirect to list of Debt Transactions page.
	 */
	@PostMapping("/debt-transaction/{id}")
	public String saveDebtTransactionDecrease(@PathVariable("id") long id, Model model,
			@Valid @ModelAttribute("debtTransaction") DebtTransaction debtTransaction,
			BindingResult bindingResult
			) {
		if (bindingResult.hasErrors()) {
			return "debt-edit";
		}
		debtTransaction.setCreateTime(new Timestamp(System.currentTimeMillis()));
		debtTransactionService.save(debtTransaction);
		return "redirect:/debt";
	}
	
}















