package ru.vladimir.personalaccounter.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ru.vladimir.personalaccounter.entity.AppUser;
import ru.vladimir.personalaccounter.entity.BankAccount;
import ru.vladimir.personalaccounter.entity.SalaryTransaction;
import ru.vladimir.personalaccounter.exception.SalaryTransactionNotFoundExcp;
import ru.vladimir.personalaccounter.exception.UserGetDataSecurityExp;
import ru.vladimir.personalaccounter.repository.SalaryTransactionRepository;

@Service
public class SalaryTransactionServiceImpl implements SalaryTransactionService {
	
	@Autowired
	private SalaryTransactionRepository salaryTransactionRepository;
	
	@Autowired
	private BankAccountService bankAccountService;
	
	@Autowired
	private UserService userService;
	
	@Override
	public SalaryTransaction save(SalaryTransaction salaryTransaction) {
		checkRights(salaryTransaction);
		
		if (salaryTransaction.getId() == 0) {
			bankAccountService.changeBalanceSaveTransaction(salaryTransaction);
		}
		else {
			//edit current
			Optional<SalaryTransaction> oldSalaryTransactionOptional = salaryTransactionRepository.findById(salaryTransaction.getId());
			if (oldSalaryTransactionOptional.isPresent()) {
				bankAccountService.changeBalanceSaveTransaction(oldSalaryTransactionOptional.get(), salaryTransaction);
			}
		}
		return salaryTransactionRepository.save(salaryTransaction);
	}

	@Override
	public List<SalaryTransaction> getAllSalaryTransactions() {
		return salaryTransactionRepository.getAllSalaryTransaction(getCurrentUser());
	}

	@Override
	public Optional<SalaryTransaction> getSalaryTransactionById(Long id) {
		return salaryTransactionRepository.getSalaryTransactionById(getCurrentUser(), id);
	}

	@Override
	public void delete(SalaryTransaction salaryTransaction) {
		checkRights(salaryTransaction);
		BankAccount theBankAccount = salaryTransaction.getBankAccount();
		theBankAccount.setBalance(theBankAccount.getBalance().subtract(salaryTransaction.getSumTransaction()));
		bankAccountService.save(theBankAccount);
		salaryTransactionRepository.delete(salaryTransaction);
	}

	//service methods
	private void checkRights(SalaryTransaction salaryTransaction) {
		AppUser appUser = getCurrentUser();
		if (!appUser.equals(salaryTransaction.getAppUser())) {
			throw new UserGetDataSecurityExp("Someone trying modify Salary transaction, but he isn't owner");
		}
	}
	
	private AppUser getCurrentUser() {
		AppUser theAppUser = userService.getCurrentAppUserFromContextOrCreateDemoUser();
		return theAppUser;
	}

	@Override
	public SalaryTransaction findById(long id) {
		SalaryTransaction theSalaryTransaction = salaryTransactionRepository.getById(id);
		if (theSalaryTransaction == null) {
			throw new SalaryTransactionNotFoundExcp();
		}
		if (theSalaryTransaction.getAppUser().equals(getCurrentUser())) {
			return theSalaryTransaction;
		}
		else {
			throw new UserGetDataSecurityExp("Someone trying delete Salary transaction, but he isn't owner");
		}
	}

}
