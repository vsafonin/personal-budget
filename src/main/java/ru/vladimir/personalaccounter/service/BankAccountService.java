package ru.vladimir.personalaccounter.service;

import java.util.List;

import ru.vladimir.personalaccounter.client.CurrencyParseExcp;
import ru.vladimir.personalaccounter.entity.AbstractTransaction;
import ru.vladimir.personalaccounter.entity.BankAccount;
import ru.vladimir.personalaccounter.entity.TransferTransaction;

public interface BankAccountService {
	
	List<BankAccount> getBankAccounts();
	
	BankAccount getBankAccountById(Long id);

	BankAccount save(BankAccount bankAccount);

	void delete(BankAccount bankAccount);
	void delete(BankAccount bankAccount, boolean force); //force delete bank account with all transactions
	
	void changeBalanceSaveTransaction(AbstractTransaction abstractTransaction);
	void changeBalanceSaveTransaction(AbstractTransaction oldTransaction, AbstractTransaction newTransaction);
	void changeBalanceDeleteTransaction(AbstractTransaction abstractTransaction); //when delete login is another...
	void changeBalanceSaveTransaction(TransferTransaction transferTransaction) throws CurrencyParseExcp; //when save transferTransaction
	void changeBalanceDeleteTransaction(TransferTransaction transferTransaction); //when delete transferTransaction
	
	boolean bankAccountNameIsNotExist(String name);

	boolean bankAccountHasTransaction(BankAccount bankAccount);

	


}
