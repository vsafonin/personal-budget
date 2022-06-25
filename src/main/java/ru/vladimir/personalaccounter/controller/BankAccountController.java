package ru.vladimir.personalaccounter.controller;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Currency;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

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

import ru.vladimir.personalaccounter.entity.AdjustmentTransaction;
import ru.vladimir.personalaccounter.entity.AppUser;
import ru.vladimir.personalaccounter.entity.BankAccount;
import ru.vladimir.personalaccounter.enums.TypeOfOperation;
import ru.vladimir.personalaccounter.exception.BankAccountNotFoundException;
import ru.vladimir.personalaccounter.exception.BankAccountNotFoundExp;
import ru.vladimir.personalaccounter.service.AdjustmentTransactionService;
import ru.vladimir.personalaccounter.service.BankAccountService;
import ru.vladimir.personalaccounter.service.UserService;
/**
 * The Controller for work with Bank Account Entity over web UI.
 * <p>
 * This class handles the CRUD operations for Bank Account entity via HTTP actions
 * as result return HTML based web pages using thymeleaf template.
 * </p>
 * @author Vladimir Afonin
 *
 */
@Controller
public class BankAccountController {

	@Autowired
	private BankAccountService bankAccountService;

	@Autowired
	private AdjustmentTransactionService adjustmentTransactionService;

	private final Set<Currency> currencySet = Currency.getAvailableCurrencies();
	
	@Autowired
	private UserService userService;

	//all model want currency set
	@ModelAttribute
	private void addNecessaryAttr(Model model) {
		model.addAttribute("currencySet", currencySet);
	}
	
	/**
	 * handle /bank-account/{id} requests, return page for edit BankAccount entity
	 * @param id - id of BankAccount entity, if id = 0 user want create new BankAccount
	 * @param model - Model for thymeleaf
	 * @return  - Bank Account edit html page. or throws BankAccountNotFoundException
	 */
	@GetMapping("/bank-account/{id}")
	public String getBankAccountCreatePage(@PathVariable("id") long id, Model model) {
		if (id == 0) { //this request want create new bank account
			AppUser theAppUser = userService.getCurrentAppUserFromContextOrCreateDemoUser();
			BankAccount theNewBankAccount = new BankAccount();
			theNewBankAccount.setAppUser(theAppUser);
			model.addAttribute("bankAccount", theNewBankAccount);
			return "bankaccount-edit-page";
		}
		try {
			BankAccount theBankAccount = bankAccountService.getBankAccountById(id);
			model.addAttribute("bankAccount", theBankAccount);
			model.addAttribute("transactions", adjustmentTransactionService.getTransactions(theBankAccount));
			return "bankaccount-edit-page";
		}
		catch (NoSuchElementException exp) {
			throw new BankAccountNotFoundException();
		}
	}

	/**
	 * handle post operations for save/create BankAccount entity. 
	 * @param id - id of BankAccount entity, if id = 0 user want create new BankAccount
	 * @param bankAccount - Model Attribute from thymeleaf, store Bank Account what we want to save/create
	 * @param bindingResult - validation errors for BankAccount fields
	 * @param model - Model for thymeleaf
	 * @return - if user create new BankAccount (id == 0 ) - redirect user to /, otherwise redirect back to edit 
	 * page with success save message. If old bankAccount not found throws Exception
	 */
	@PostMapping("/bank-account/{id}")
	public String saveNewBankAccount(@PathVariable("id") long id,
			@Valid @ModelAttribute("bankAccount") BankAccount bankAccount,
			BindingResult bindingResult, Model model) {
		if (bindingResult.hasErrors()) {
			return "bankaccount-edit-page";
		}
		AppUser currentUser = userService.getCurrentAppUserFromContextOrCreateDemoUser();
		if (!currentUser.equals(bankAccount.getAppUser())) {
			throw new IllegalAccessError("User trying save bank account, but not own it");
		}
		//if this is new (id = 0) check maybe name bank account is exist
		if (id == 0 && bankAccountService.bankAccountNameIsNotExist(bankAccount.getName())) {
			model.addAttribute("errorName",true);
			return "bankaccount-edit-page";
		}
		
		
		if (id != 0) { //this is old transaction, user trying save changes
			BankAccount oldBankAccountForCompare;
			try {
				oldBankAccountForCompare = bankAccountService.getBankAccountById(id);
			}
			catch (NoSuchElementException e) {
				throw new BankAccountNotFoundException();
			}
			if (oldBankAccountForCompare.getBalance().compareTo(bankAccount.getBalance()) != 0) {
				// to do create new adjustment transaction
				AdjustmentTransaction adjustment = new AdjustmentTransaction();
				adjustment.setCurrency(bankAccount.getCurrency());
				if (oldBankAccountForCompare.getBalance().compareTo(bankAccount.getBalance()) > 0 ) {
					adjustment.setTypeOfOperation(TypeOfOperation.DECREASE);
					BigDecimal oldBalance = oldBankAccountForCompare.getBalance();
					BigDecimal newBalance = bankAccount.getBalance(); 
					BigDecimal difference = oldBalance.subtract(newBalance);
					adjustment.setSumTransaction(difference);
				} else {
					adjustment.setTypeOfOperation(TypeOfOperation.INCREASE);
					BigDecimal oldBalance = oldBankAccountForCompare.getBalance();
					BigDecimal newBalance = bankAccount.getBalance();
					BigDecimal difference = newBalance.subtract(oldBalance);
					adjustment.setSumTransaction(difference);
				}
				
				adjustment.setBankAccount(bankAccount);
				adjustment.setAppUser(currentUser);
				Timestamp currentTime = new java.sql.Timestamp(System.currentTimeMillis());
				adjustment.setCreateTime(currentTime);
				bankAccount.addBankAccountAdjstmens(adjustment);
			}
			
		}
		bankAccountService.save(bankAccount);
		
		if (id == 0) {
			return "redirect:/";
		} else {
			return "redirect:/bank-account/" + id + "?success=true";
		}
	}

	/**
	 * handle /bank-account/delete/{id} request, which delete bank-acount with this id from DB.
	 * If BankAccount has active transactions, user must use force option.
	 * @param id - id of BankAccount entity, if id = 0 user want create new BankAccount.
	 * @param model - Model for thymeleaf/
	 * @param force - request parameter for force delete Bank Account with all transactions with this account.
	 * @return - if user not use force parameter and bankAccount has  transactions
	 * return template with variable hasTransaction, this variable handle by Thymeleaf and show link with "force" 
	 * request parameter, otherwise redirect to /
	 */
	@GetMapping("/bank-account/delete/{id}")
	public String deleteBankAccount(@PathVariable("id") Long id, Model model,
			@RequestParam(name = "force", required = false ) boolean force) {
		
		BankAccount bankAccountForDelete = bankAccountService.getBankAccountById(id);
		if (bankAccountForDelete == null) {
			throw new BankAccountNotFoundExp();
		}
		
		//check, if bank account has transaction and user don't set force parameter return false
		if (!force) {
			boolean hasTransaction = bankAccountService.bankAccountHasTransaction(bankAccountForDelete);
				if (hasTransaction) {
					model.addAttribute("bankAccount", bankAccountForDelete);
					List<AdjustmentTransaction> adjustmentTransactions = adjustmentTransactionService.getTransactions(bankAccountForDelete);  
					model.addAttribute("transactions", adjustmentTransactions);
					model.addAttribute("bankAccountHasTransaction", hasTransaction);
					return "bankaccount-edit-page";			
				}
				else {
					bankAccountService.delete(bankAccountForDelete);
				}
		}
		else {
			// delete it
			bankAccountService.delete(bankAccountForDelete,force);
		}
		return "redirect:/";
	}

}
