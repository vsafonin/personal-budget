package ru.vladimir.personalaccounter.service;

import java.util.List;
import java.util.NoSuchElementException;

import ru.vladimir.personalaccounter.client.CurrencyParseExcp;
import ru.vladimir.personalaccounter.entity.TransferTransaction;

public interface TransferTransactionService {

	void saveTransferTransaction(TransferTransaction transferTransaction) throws CurrencyParseExcp;
	void deleteTransferTransaction(TransferTransaction transferTransaction);
	
	TransferTransaction getTransferTransactionById(Long id) throws NoSuchElementException;
	
	List<TransferTransaction> getTransferTransactions();
}
