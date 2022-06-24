package ru.vladimir.personalaccounter.rest;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ru.vladimir.personalaccounter.entity.AbstractTransaction;
import ru.vladimir.personalaccounter.entity.AppUser;
import ru.vladimir.personalaccounter.entity.BankAccount;
import ru.vladimir.personalaccounter.entity.Partner;
import ru.vladimir.personalaccounter.entity.SalaryTransaction;
import ru.vladimir.personalaccounter.enums.TypeOfOperation;
import ru.vladimir.personalaccounter.repository.PartnerRepository;
import ru.vladimir.personalaccounter.service.AdjustmentTransactionService;
import ru.vladimir.personalaccounter.service.BankAccountService;
import ru.vladimir.personalaccounter.service.DebtTransactionService;
import ru.vladimir.personalaccounter.service.SalaryTransactionService;
import ru.vladimir.personalaccounter.service.UserService;

@RestController
@RequestMapping(value = "/api")
public class SalaryTransactionRestController {

	@Autowired
	private SalaryTransactionService salaryTransactionService;
	
	@Autowired
	private BankAccountService bankAccountService;
	
	@Autowired
	private PartnerRepository partnerRepository;
	
	@Autowired
	private AdjustmentTransactionService adjustmentTransactionService;
	
	@Autowired
	private DebtTransactionService debtTransactionService;
	
	@Autowired
	private UserService userService;
	
	/**
	 * type of income: 
	 * 1. salary
	 * 2. Adjustment with type Increase
	 * 3. Debt with type Increase
	 * collect it in List<AbstractTransaction> and return to user
	 * @return
	 */
	@GetMapping("/income")
	public List<AbstractTransaction> getIncomeList(){
		List<AbstractTransaction> result = new ArrayList<>();
		//get 100 items salary transaction
		salaryTransactionService.getAllSalaryTransactions().stream()
			.limit(100)
			.forEach(s -> result.add(s));
		//get 100 items debt transaction
		bankAccountService.getBankAccounts().stream().forEach( b ->
				adjustmentTransactionService.getTransactions(b).stream()
					.limit(100)
					.filter(a -> a.getTypeOfOperation() == TypeOfOperation.INCREASE)
					.forEach(a -> result.add(a))
				);
		
		debtTransactionService.findAll().stream()
			.limit(100)
			.filter(c -> c.getTypeOfOperation() == TypeOfOperation.INCREASE)
			.forEach(c -> result.add(c));
		
		//sort received data
		return result.stream()
			.sorted(Comparator.comparingLong(c -> c.getCreateTime().getTime()))
			.limit(20)
			.collect(Collectors.toList());		
	}
	
	
	@PostMapping("/salary")
	public SalaryTransaction addSalaryTransaction(@RequestBody SalaryTransaction salaryTransaction) {
		AppUser theAppUser = userService.getCurrentAppUserFromContextOrCreateDemoUser();
		salaryTransaction.setAppUser(theAppUser);
		
		salaryTransaction.setCreateTime(new Timestamp(System.currentTimeMillis()));
		salaryTransaction.setTypeOfOperation(TypeOfOperation.INCREASE);

		//check bankAccount
		BankAccount theBankAccount = null;
		if (salaryTransaction.getBankAccount() == null) {
			theBankAccount = bankAccountService.getBankAccounts().stream().filter(ba -> ba.isDefaultAccount()).findAny().orElse(null);
		}
		else {
			theBankAccount = bankAccountService.getBankAccounts().stream()
					.filter(ba -> ba.getName().equals(salaryTransaction.getBankAccount().getName()))
							.findFirst().orElse(null);
			if (theBankAccount == null) {
				throw new IllegalArgumentException("Bank account is null");
			}
		}
		salaryTransaction.setBankAccount(theBankAccount);
		salaryTransaction.setCurrency(theBankAccount.getCurrency());
		
		Partner thePartner = null;
		if (salaryTransaction.getPartner() == null) {
			throw new IllegalArgumentException();
		}
		thePartner = partnerRepository.findByName(theAppUser, salaryTransaction.getPartner().getName());
		if (thePartner == null) {
			thePartner = partnerRepository.save(salaryTransaction.getPartner());
		}
		salaryTransaction.setPartner(thePartner);		
		
		return salaryTransactionService.save(salaryTransaction);
	}
}
