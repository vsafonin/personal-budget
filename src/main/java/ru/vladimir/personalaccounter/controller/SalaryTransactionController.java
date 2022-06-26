package ru.vladimir.personalaccounter.controller;

import java.sql.Timestamp;
import java.util.Currency;
import java.util.NoSuchElementException;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import ru.vladimir.personalaccounter.entity.AppUser;
import ru.vladimir.personalaccounter.entity.Partner;
import ru.vladimir.personalaccounter.entity.SalaryTransaction;
import ru.vladimir.personalaccounter.exception.SalaryTransactionNotFoundExcp;
import ru.vladimir.personalaccounter.repository.PartnerRepository;
import ru.vladimir.personalaccounter.service.SalaryTransactionService;
import ru.vladimir.personalaccounter.service.UserService;
/**
 * The Controller for work with Salary Transactions Entity over web UI.
 * <p>
 * This class handles the CRUD operations for Salary Transactions entity via HTTP actions
 * as result return HTML based web pages using thymeleaf template.
 * </p>
 * @author Vladimir Afonin
 *
 */
@Controller
public class SalaryTransactionController {
	
	@Autowired
	private SalaryTransactionService salaryTransactionService; 
	
	@Autowired
	private PartnerRepository pathnerRepository;
	
	@Autowired
	private UserService userService;
	
	
	/**
	 * returns HTML page for create new SalaryTransaction or if id != 0 for edit existing
	 * @param id - Salary Transaction id, if == 0 user want create new Salary Transaction
	 * @param model
	 * @return - return "transaction-salary" - template
	 */
	@GetMapping("/salary-transaction/{id}")
	public String getSalaryTransactionPage(@PathVariable("id") long id,Model model) {
		SalaryTransaction theSalaryTransaction;
		if (id == 0) {
			AppUser theAppUser = userService.getCurrentAppUserFromContextOrCreateDemoUser();
			theSalaryTransaction = new SalaryTransaction();
			theSalaryTransaction.setAppUser(theAppUser);
		}
		else {
			try {
				theSalaryTransaction = salaryTransactionService.findById(id);
			}
			catch (NoSuchElementException exp) {
				throw new SalaryTransactionNotFoundExcp();
			}
		}
		model.addAttribute("salaryTransaction",theSalaryTransaction);
		return "transaction-salary";
	}
	/**
	 * Used to delete the SalaryTransaction from DB vie web UI
	 * @param id - SalaryTransaction id which we want to delete
	 * @return redirects to list of SalaryTransaction page /salary
	 */
	@GetMapping("/salary-transaction/delete/{id}")
	public String deleteSalaryTransactionPage(@PathVariable("id") long id) {
		try {
			SalaryTransaction theSalaryTransaction = salaryTransactionService.findById(id);
			salaryTransactionService.delete(theSalaryTransaction);
			return "redirect:/salary";
		}
		catch (NoSuchElementException e) {
			throw new SalaryTransactionNotFoundExcp();
		}
		
	}
	/**
	 * Used to save new or changed SalaryTransaction entity to Database via WEB UI
	 * if the Object has error in fields, returns the edit page. 
	 * @param id - SalaryTransaction id, which we want to save/change
	 * @param model
	 * @param salaryTransaction - SalaryTransaction object for save into DB
	 * @param bindingResult
	 * @return - if everything is ok, redirects to list of SalaryTransaction page /salary, otherwise
	 * returns the edit page.
	 */
	@PostMapping("/salary-transaction/{id}")
	public String saveSalaryTransactionPage(@PathVariable("id") long id, Model model,
			@Valid @ModelAttribute("salaryTransaction") SalaryTransaction salaryTransaction,
			BindingResult bindingResult
			) {
		if (bindingResult.hasErrors()) {
			return "transaction-salary";
		}
		Partner partner = pathnerRepository.findByName(salaryTransaction.getAppUser(),
				salaryTransaction.getPartner().getName());
		if (partner == null) {
			salaryTransaction.setPartner(pathnerRepository.save(salaryTransaction.getPartner()));
		}
		else {
			salaryTransaction.setPartner(partner);
		}
		//set creation time 
		if(id == 0) {
			Timestamp currentTime = new Timestamp(System.currentTimeMillis());
			salaryTransaction.setCreateTime(currentTime);
		}
		Currency currentBankCurrency = salaryTransaction.getBankAccount().getCurrency(); 
		salaryTransaction.setCurrency(currentBankCurrency);
		
		salaryTransactionService.save(salaryTransaction);
		return "redirect:/salary";
	}
}
