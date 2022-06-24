package ru.vladimir.personalaccounter.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ru.vladimir.personalaccounter.entity.AppUser;
import ru.vladimir.personalaccounter.entity.PurchaseTransaction;
import ru.vladimir.personalaccounter.repository.PurchaseTransactionRepository;

@Service
public class PurchaseTransactionServiceImpl implements PurchaseTransactionService {

	@Autowired
	private PurchaseTransactionRepository purchaseTransactionRepository;
	
	@Autowired
	private BankAccountService bankAccountService;
	
	@Autowired
	private UserService userService;
	
	@Override
	public PurchaseTransaction save(PurchaseTransaction purchaseTransaction) {
		//if this is 0 (new) - minus from bank account money
		if (purchaseTransaction.getId() == 0) {
			bankAccountService.changeBalanceSaveTransaction(purchaseTransaction);
		}
		else {
			//if this is isn't 0 (edit) - minus different money from bank account
			Optional<PurchaseTransaction> oldPurchaseTransactionOptional = purchaseTransactionRepository.findById(purchaseTransaction.getId());
			if (oldPurchaseTransactionOptional.isPresent()) {
				bankAccountService.changeBalanceSaveTransaction(oldPurchaseTransactionOptional.get(), purchaseTransaction);
			}
		}

		
		return purchaseTransactionRepository.save(purchaseTransaction);
	}

	@Override
	public List<PurchaseTransaction> getAllPurchaseTransactions() {
		AppUser theAppUser = userService.getCurrentAppUserFromContextOrCreateDemoUser();
		return purchaseTransactionRepository.getAllPurchaseTransactionByAppUser(theAppUser);
	}

	@Override
	public Optional<PurchaseTransaction> findById(Long id) {
		return purchaseTransactionRepository.findById(id);
	}

	@Override
	public void delete(PurchaseTransaction purchaseTransaction) {
		bankAccountService.changeBalanceDeleteTransaction(purchaseTransaction);
		purchaseTransactionRepository.delete(purchaseTransaction);
	}

	@Override
	public Optional<PurchaseTransaction> findByFiscalSign(long fiscalSign) {
		return purchaseTransactionRepository.findByFiscalSign(fiscalSign);
	}

}
