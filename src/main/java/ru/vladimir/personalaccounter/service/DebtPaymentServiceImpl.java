package ru.vladimir.personalaccounter.service;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ru.vladimir.personalaccounter.entity.AppUser;
import ru.vladimir.personalaccounter.entity.DebtPayment;
import ru.vladimir.personalaccounter.entity.DebtTransaction;
import ru.vladimir.personalaccounter.exception.DebtPaymentNotFoundExp;
import ru.vladimir.personalaccounter.exception.UserGetDataSecurityExp;
import ru.vladimir.personalaccounter.repository.DebtPaymentRepository;

@Service
public class DebtPaymentServiceImpl implements DebtPaymentService {

	@Autowired
	private DebtPaymentRepository debtPaymentRepository;

	@Autowired
	private DebtTransactionService debtTransactionService;
	
	@Autowired
	private UserService userService;
	
	
	@Override
	public void save(DebtPayment debtPayment) {
		DebtTransaction thedebtTransaction = debtPayment.getDebtTransaction();
		
		boolean isNew = debtPayment.getId() == 0;

		if (isNew) {
			thedebtTransaction
					.setSumTransaction(thedebtTransaction.getSumTransaction().subtract(debtPayment.getPaySum()));
			//add this to list debtTransaction
			thedebtTransaction.getDebtPayment().add(debtPayment);
			//set create time
			debtPayment.setPayDate(new Timestamp(System.currentTimeMillis()));
			debtTransactionService.save(thedebtTransaction);
		} else {
			// get old debtPayment
			Optional<DebtPayment> olddebtPaymentOptional = debtPaymentRepository.findById(debtPayment.getId());
			if (olddebtPaymentOptional.isPresent()) {
				BigDecimal oldSum = olddebtPaymentOptional.get().getPaySum();
				BigDecimal newSum = debtPayment.getPaySum();
				if (oldSum != newSum) {
					thedebtTransaction.setSumTransaction(thedebtTransaction.getSumTransaction().add(oldSum));
					thedebtTransaction.setSumTransaction(thedebtTransaction.getSumTransaction().subtract(newSum));
					debtTransactionService.save(thedebtTransaction);
				}
			}
		}
		
		debtPaymentRepository.save(debtPayment);
	}

	@Override
	public void delete(DebtPayment debtPayment) {
		DebtTransaction thedebtTransaction = debtPayment.getDebtTransaction();
		thedebtTransaction.setSumTransaction(thedebtTransaction.getSumTransaction().add(debtPayment.getPaySum()));
		thedebtTransaction.getDebtPayment().remove(debtPayment);
		debtPaymentRepository.delete(debtPayment);
		debtTransactionService.save(thedebtTransaction);
	}

	@Override
	public DebtPayment findById(long id) {
		//get user
		AppUser appUser = userService.getCurrentAppUserFromContextOrCreateDemoUser();
		Optional<DebtPayment> thedebtPaymentOptional = debtPaymentRepository.findById(id);
		if (thedebtPaymentOptional.isPresent()) {
			if (!appUser.equals(thedebtPaymentOptional.get().getDebtTransaction().getAppUser())) {
				throw new UserGetDataSecurityExp(appUser.getUsername() + " trying get debt payment, but he isn't owner");
			}
		}
		else {
			throw new DebtPaymentNotFoundExp();
		}
		return thedebtPaymentOptional.get();
	}

}
