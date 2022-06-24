package ru.vladimir.personalaccounter.service;

import java.util.List;

import ru.vladimir.personalaccounter.entity.DebtTransaction;

public interface DebtTransactionService {

	DebtTransaction findById(long id);
	
	DebtTransaction save(DebtTransaction debtTransaction);

	void delete(DebtTransaction debtTransaction);

	List<DebtTransaction> findAll();

}
