package ru.vladimir.personalaccounter.service;

import java.util.List;
import java.util.Optional;

import ru.vladimir.personalaccounter.entity.PurchaseTransaction;

public interface PurchaseTransactionService {

	PurchaseTransaction save(PurchaseTransaction purchaseTransaction);

	List<PurchaseTransaction> getAllPurchaseTransactions();

	Optional<PurchaseTransaction> findById(Long id);

	void delete(PurchaseTransaction purchaseTransaction);

	Optional<PurchaseTransaction> findByFiscalSign(long fiscalSign);
	
	
}
