package ru.vladimir.personalaccounter.service;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ru.vladimir.personalaccounter.entity.AppUser;
import ru.vladimir.personalaccounter.entity.DebtTransaction;
import ru.vladimir.personalaccounter.entity.Partner;
import ru.vladimir.personalaccounter.exception.DebtTransactionNotFoundExp;
import ru.vladimir.personalaccounter.exception.UserGetDataSecurityExp;
import ru.vladimir.personalaccounter.repository.DebtTransactionRepository;
import ru.vladimir.personalaccounter.repository.PartnerRepository;

@Service
public class DebtTransactionServiceImpl implements DebtTransactionService {

	@Autowired
	private DebtTransactionRepository debtTransactionRepository;

	@Autowired
	private BankAccountService bankAccountService;

	@Autowired
	private PartnerRepository pathnerRepository;
	
	@Autowired
	private UserService userService;
	
	private Map<Long, DebtTransaction> cacheDebtTransaction = Collections
			.synchronizedMap(new WeakHashMap<Long, DebtTransaction>());

	@Override
	public DebtTransaction findById(long id) {
		DebtTransaction theDebtTransaction = null;
		AppUser theAppUser = userService.getCurrentAppUserFromContextOrCreateDemoUser();
		if (cacheDebtTransaction.containsKey(id) &&
				cacheDebtTransaction.get(id).getAppUser().equals(theAppUser)) {
			theDebtTransaction = cacheDebtTransaction.get(id);
		} else {
			Optional<DebtTransaction> thedebtTransactionOptional = debtTransactionRepository.findByAppUserAndId(theAppUser,id);
			if (!thedebtTransactionOptional.isPresent()) {
				throw new DebtTransactionNotFoundExp();
			}
			theDebtTransaction = thedebtTransactionOptional.get();
			cacheDebtTransaction.put(theDebtTransaction.getId(), theDebtTransaction);
		}

		return theDebtTransaction;
	}

	@Override
	public DebtTransaction save(DebtTransaction debtTransaction) {
		boolean setPartner = false;
		
		if (debtTransaction.getId() == 0) {
			// create new
			bankAccountService.changeBalanceSaveTransaction(debtTransaction);
			setPartner = true;
			debtTransaction.setActive(true);
		} else {
			// edit
			// refresh cache - otherwise findById return changed object
			DebtTransaction oldDebtTransaction = findById(debtTransaction.getId());
			if (oldDebtTransaction == null) {
				throw new DebtTransactionNotFoundExp();
			}
			
			if (debtTransaction.getSumTransaction().compareTo(BigDecimal.ZERO) < 0) {
				debtTransaction.setActive(false);
			}

			// check partner is changed?
			setPartner = !oldDebtTransaction.getPartner().equals(debtTransaction.getPartner());
			
			bankAccountService.changeBalanceSaveTransaction(oldDebtTransaction, debtTransaction);
			
		}

		// set partner

		if (setPartner) {
			Partner thePartner = pathnerRepository.findByName(debtTransaction.getAppUser(),
					debtTransaction.getPartner().getName());
			if (thePartner == null) {
				thePartner = new Partner();
				thePartner.setAppUser(debtTransaction.getAppUser());
				thePartner.setName(debtTransaction.getPartner().getName());
				debtTransaction.setPartner(thePartner);
			} else {
				debtTransaction.setPartner(thePartner);
			}
		}

		// set currency
		debtTransaction.setCurrency(debtTransaction.getBankAccount().getCurrency());
		DebtTransaction debtTransactionInDb = debtTransactionRepository.save(debtTransaction);
		cacheDebtTransaction.put(debtTransactionInDb.getId(), debtTransactionInDb);
		return debtTransactionInDb;
	}

	@Override
	public void delete(DebtTransaction debtTransaction) {
		checkRight(debtTransaction);
		bankAccountService.changeBalanceDeleteTransaction(debtTransaction);
		cacheDebtTransaction.remove(debtTransaction.getId());
		debtTransactionRepository.delete(debtTransaction);
	}

	private void checkRight(DebtTransaction thedebtTransaction) {
		AppUser theAppUser = userService.getCurrentAppUserFromContextOrCreateDemoUser();
		if (!thedebtTransaction.getAppUser().equals(theAppUser)) {
			throw new UserGetDataSecurityExp(theAppUser.getUsername() + " trying get data, but he isn't owner it");
		}
	}

	@Override
	public List<DebtTransaction> findAll() {
		AppUser theAppUser = userService.getCurrentAppUserFromContextOrCreateDemoUser();
		return debtTransactionRepository.findAllByAppUserOrderByCreateTimeDesc(theAppUser);
	}

}
