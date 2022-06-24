package ru.vladimir.personalaccounter.controller;

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
import ru.vladimir.personalaccounter.entity.DebtPayment;
import ru.vladimir.personalaccounter.entity.DebtTransaction;
import ru.vladimir.personalaccounter.service.DebtPaymentService;
import ru.vladimir.personalaccounter.service.DebtTransactionService;
/**
 * The controller for work with DebtTransactionsPayment entity.
 * <p>
 * This class handles the CRUD operations for Debt transaction payment entity via HTTP actions
 * as result return HTML based web pages using thymeleaf template.
 * </p>
 * @author Vladimir Afonin
 *
 */
@Controller
public class DebtPaymentController {

	@Autowired
	private DebtPaymentService debtPaymentService;
	
	@Autowired
	private DebtTransactionService debtTransactionService;
	
	/**
	 * Handle /debt-transaction/payment/{id} requests, return page for edit Debt payment transactions
	 * @param id - id of DebtTransactionPayment entity, if id = 0 user want create new DebtTransactionPayment
	 * @param dtid - DebtTransaction id.
	 * @param model - Model for thymeleaf
	 * @return - Debt payment edit html page
	 */
	@GetMapping("/debt-transaction/payment/{id}")
	public String getDebtPaymentEditPage(@PathVariable("id") long id,
			@RequestParam("dtid") long dtid,
			Model model
			) {
		DebtPayment theDebtPayment;
		DebtTransaction thedebtTransaction = debtTransactionService.findById(dtid);
		if (id == 0) {
			theDebtPayment = new DebtPayment();
			theDebtPayment.setDebtTransaction(thedebtTransaction);
		}
		else {
			theDebtPayment = debtPaymentService.findById(id);
		}
		model.addAttribute("debtPayment",theDebtPayment);
		return "debt-payment";
	}
	/**
	 * Handle /debt-transaction/payment/delete/{id} requests, this requests delete entity from DB 
	 * and if ok redirect user to DebtTransaction Edit page. 
	 * @param id - DebtTransactionPayment id
	 * @return - redirect user to DebtTransaction Edit page.
	 */
	@GetMapping("/debt-transaction/payment/delete/{id}")
	public String deletePyament(@PathVariable("id") long id) {
		DebtPayment thedebtPayment = debtPaymentService.findById(id);
		debtPaymentService.delete(thedebtPayment);
		return "redirect:/debt-transaction/" + thedebtPayment.getDebtTransaction().getId() + "?type=" + 
			thedebtPayment.getDebtTransaction().getTypeOfOperation().toString();
	}

	/**
	 * Handle post request to /debt-transaction/payment/{id}, if ok 
	 * redirect user to DebtTransaction Edit page. 
	 * @param id - DebtTransactionPayment id
	 * @param thedebtPayment - Object DebtTransactionPayment which we want to save/create
	 * @param bindingResult - result validation object DebtTransactionPayment.
	 * @param model -  Model for thymeleaf.
	 * @return - redirect user to DebtTransaction Edit page.
	 */
	@PostMapping("/debt-transaction/payment/{id}")
	public String savedebtPayment(@PathVariable("id") long id,
			@Valid @ModelAttribute("debtPayment") DebtPayment thedebtPayment,
			BindingResult bindingResult,
			Model model) {
		if (bindingResult.hasErrors()) {
			return "debt-payment";
		}
		debtPaymentService.save(thedebtPayment);
		return "redirect:/debt-transaction/" + thedebtPayment.getDebtTransaction().getId() + "?type=" + 
				thedebtPayment.getDebtTransaction().getTypeOfOperation().toString();
	}
}
