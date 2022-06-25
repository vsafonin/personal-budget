package ru.vladimir.personalaccounter.service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ru.vladimir.personalaccounter.entity.AppUser;
import ru.vladimir.personalaccounter.entity.BankAccount;
import ru.vladimir.personalaccounter.entity.SalaryTransaction;
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
		return salaryTransactionRepository.getSalaryTransactionByAppUserAndId(getCurrentUser(), id);
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
	/**
	 * @param - id which SalaryTransaction we want to find
	 * @return - if found SalaryTransaction, otherwise throws NoSuchElementException
	 */
	@Override
	public SalaryTransaction findById(long id) throws NoSuchElementException{
		AppUser theAppUser = userService.getCurrentAppUserFromContextOrCreateDemoUser();
		Optional<SalaryTransaction> theSalaryTransactionOptional = 
				salaryTransactionRepository.getSalaryTransactionByAppUserAndId(theAppUser,id);
		return theSalaryTransactionOptional.get();
	}

}
