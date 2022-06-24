package ru.vladimir.personalaccounter.service;

import ru.vladimir.personalaccounter.entity.DebtPayment;

public interface DebtPaymentService {

	void save(DebtPayment debtPayment);
	
	void delete(DebtPayment debtPayment);

	DebtPayment findById(long id);
}
