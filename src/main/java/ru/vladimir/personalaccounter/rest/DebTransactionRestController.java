package ru.vladimir.personalaccounter.rest;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ru.vladimir.personalaccounter.entity.AbstractTransaction;
import ru.vladimir.personalaccounter.entity.AppUser;
import ru.vladimir.personalaccounter.entity.BankAccount;
import ru.vladimir.personalaccounter.entity.DebtTransaction;
import ru.vladimir.personalaccounter.entity.Partner;
import ru.vladimir.personalaccounter.enums.TypeOfOperation;
import ru.vladimir.personalaccounter.repository.PartnerRepository;
import ru.vladimir.personalaccounter.service.BankAccountService;
import ru.vladimir.personalaccounter.service.DebtTransactionService;
import ru.vladimir.personalaccounter.service.UserService;

@RestController
@RequestMapping(value = "/api")
public class DebTransactionRestController {
	@Autowired
	private DebtTransactionService debtTransactionService;

	@Autowired
	private BankAccountService bankAccountService;

	@Autowired
	private PartnerRepository partnerRepository;

	@Autowired
	private UserService userService;
	
	@GetMapping("/debt")
	public List<AbstractTransaction> getDecreaseDebt(@RequestParam("type") TypeOfOperation typeOfOperation) {

		List<AbstractTransaction> result = new ArrayList<>();
		if (typeOfOperation == TypeOfOperation.INCREASE) {
			debtTransactionService.findAll().stream().filter(c -> c.isActive())
					.filter(c -> c.getTypeOfOperation() == TypeOfOperation.INCREASE).limit(20)
					.forEach(c -> result.add(c));
		} else if (typeOfOperation == TypeOfOperation.DECREASE) {
			debtTransactionService.findAll().stream().filter(c -> c.isActive())
					.filter(c -> c.getTypeOfOperation() == TypeOfOperation.DECREASE).limit(20)
					.forEach(c -> result.add(c));
		}
		return result;
	}

	@PostMapping("/debt")
	public DebtTransaction addNewdebtTransaction(@RequestBody DebtTransaction thedebtTransaction) {
		// set appUser
		AppUser theAppUser = userService.getCurrentAppUserFromContextOrCreateDemoUser();
		thedebtTransaction.setAppUser(theAppUser);

		// check typeOfOperation
		if (thedebtTransaction.getTypeOfOperation() == null) {
			throw new IllegalArgumentException("Type of operation not set");
		}

		// set bank
		BankAccount theBankAccount = thedebtTransaction.getBankAccount();
		if (theBankAccount == null) {
			theBankAccount = bankAccountService.getBankAccounts().stream().filter(ba -> ba.isDefaultAccount()).findAny()
					.orElse(null);
		} else {
			if (theBankAccount.getName() == null || theBankAccount.getName().isBlank()) {
				throw new IllegalArgumentException("Bank Account name is null or empty");
			}
			// find in db
			theBankAccount = bankAccountService.getBankAccounts().stream().filter((BankAccount ba) -> {
				return ba.getName().equals(thedebtTransaction.getBankAccount().getName());
			}).findFirst().orElse(null);

			if (theBankAccount == null) {
				throw new IllegalArgumentException("BankAccount not found");
			}

		}

		thedebtTransaction.setBankAccount(theBankAccount);
		thedebtTransaction.setCurrency(theBankAccount.getCurrency());
		thedebtTransaction.setCreateTime(new Timestamp(System.currentTimeMillis()));

		if (thedebtTransaction.getEndDate() == null) {
			throw new IllegalArgumentException("End date is null");
		}

		if (thedebtTransaction.getSumTransaction().compareTo(BigDecimal.ZERO) <= 0) {
			throw new IllegalArgumentException("Sum is 0 or less");
		}
		// set partner
		Partner thePartner = thedebtTransaction.getPartner();
		if (thePartner == null) {
			throw new IllegalArgumentException("Partner is null");
		}
		if (thePartner.getName() == null || thePartner.getName().isBlank()) {
			throw new IllegalArgumentException("Partner name is null or empty");
		}
		thePartner = partnerRepository.findByName(theAppUser, thePartner.getName());
		if (thePartner == null) {
			thePartner = partnerRepository.save(thedebtTransaction.getPartner());
		}
		thedebtTransaction.setPartner(thePartner);

		return debtTransactionService.save(thedebtTransaction);
	}

}
