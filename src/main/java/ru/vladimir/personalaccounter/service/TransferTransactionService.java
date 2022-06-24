package ru.vladimir.personalaccounter.service;

import java.util.List;

import ru.vladimir.personalaccounter.client.CurrencyParseExcp;
import ru.vladimir.personalaccounter.entity.TransferTransaction;

public interface TransferTransactionService {

	void saveTransferTransaction(TransferTransaction transferTransaction) throws CurrencyParseExcp;
	void deleteTransferTransaction(TransferTransaction transferTransaction);
	
	TransferTransaction getTransferTransactionById(Long id);
	
	List<TransferTransaction> getTransferTransactions();
}
