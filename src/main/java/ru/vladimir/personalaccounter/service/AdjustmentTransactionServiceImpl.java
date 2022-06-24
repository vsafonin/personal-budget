package ru.vladimir.personalaccounter.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.NonNull;
import ru.vladimir.personalaccounter.entity.AdjustmentTransaction;
import ru.vladimir.personalaccounter.entity.AppUser;
import ru.vladimir.personalaccounter.entity.BankAccount;
import ru.vladimir.personalaccounter.enums.TypeOfOperation;
import ru.vladimir.personalaccounter.exception.UsrTransactionNotFoundExp;
import ru.vladimir.personalaccounter.repository.AdjustmentTransactionRepository;

@Service
public class AdjustmentTransactionServiceImpl implements AdjustmentTransactionService {

	@Autowired
	private AdjustmentTransactionRepository adjustmentTransactionRepository;
	
	@Autowired
	private BankAccountService accountService;
	
	@Autowired
	private UserService userService;
	
	@Override
	public List<AdjustmentTransaction> getTransactions(BankAccount bankAccount) {		
		return adjustmentTransactionRepository.getAll(bankAccount);
	}

	@Override
	public AdjustmentTransaction getTransactioById(Long id) {
			AppUser theAppUser = userService.getCurrentAppUserFromContextOrCreateDemoUser();
            Optional<AdjustmentTransaction> usrTransaction =  adjustmentTransactionRepository.findById(theAppUser,id);
            if (usrTransaction.isPresent()) {
            	return usrTransaction.get();
            }
            
            throw new UsrTransactionNotFoundExp();
	}

	@Override
	public void save(@NonNull AdjustmentTransaction transaction) {
		//we have to change balance on bank account
		BankAccount bankAccount = transaction.getBankAccount();
		if (bankAccount == null) {
			throw new IllegalArgumentException("UsrTransaction trying save, but it has bank account is null");
		}
		if (transaction.getTypeOfOperation() == TypeOfOperation.INCREASE) {
			bankAccount.setBalance(bankAccount.getBalance().add(transaction.getSumTransaction()));
		}
		else {
			bankAccount.setBalance(bankAccount.getBalance().subtract(transaction.getSumTransaction()));
		}
		accountService.save(bankAccount);
		adjustmentTransactionRepository.save(transaction);		
	}

	@Override
	public void delete(@NonNull AdjustmentTransaction transaction) {
		//we have to return balance to bankAccount
		BankAccount bankAccount = transaction.getBankAccount();
		if (bankAccount == null) {
			throw new IllegalArgumentException("UsrTransaction trying delete, but it has bank account is null");
		}
		if (transaction.getTypeOfOperation() == TypeOfOperation.INCREASE) {
			bankAccount.setBalance(bankAccount.getBalance().subtract(transaction.getSumTransaction()));
		}
		else {
			bankAccount.setBalance(bankAccount.getBalance().add(transaction.getSumTransaction()));
		}
		accountService.save(bankAccount);
		
		adjustmentTransactionRepository.delete(transaction);
	}

	
}
