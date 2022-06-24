package ru.vladimir.personalaccounter.service;

import java.util.List;
import java.util.Optional;

import ru.vladimir.personalaccounter.entity.SalaryTransaction;

public interface SalaryTransactionService {
	
	SalaryTransaction save(SalaryTransaction salaryTransaction);
	
	List<SalaryTransaction> getAllSalaryTransactions();
	
	Optional<SalaryTransaction> getSalaryTransactionById(Long id);
	
	void delete(SalaryTransaction salaryTransaction);

	SalaryTransaction findById(long id);


}
