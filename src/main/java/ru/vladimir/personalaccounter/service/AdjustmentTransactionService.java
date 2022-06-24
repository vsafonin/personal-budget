package ru.vladimir.personalaccounter.service;

import java.util.List;

import ru.vladimir.personalaccounter.entity.AdjustmentTransaction;
import ru.vladimir.personalaccounter.entity.BankAccount;

public interface AdjustmentTransactionService {

	List<AdjustmentTransaction> getTransactions(BankAccount bankAccount);
	
	AdjustmentTransaction getTransactioById(Long id);
	
	void save(AdjustmentTransaction transaction);
	
	void delete(AdjustmentTransaction transaction);
}
