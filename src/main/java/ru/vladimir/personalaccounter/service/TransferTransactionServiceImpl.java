package ru.vladimir.personalaccounter.service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ru.vladimir.personalaccounter.client.CurrencyParseExcp;
import ru.vladimir.personalaccounter.entity.AppUser;
import ru.vladimir.personalaccounter.entity.TransferTransaction;
import ru.vladimir.personalaccounter.exception.TransferTransactionNotFoundExcp;
import ru.vladimir.personalaccounter.repository.TransferTransactionRepository;
@Service
public class TransferTransactionServiceImpl implements TransferTransactionService {
	
	@Autowired
	private TransferTransactionRepository transferTransactionRepository;
	
	@Autowired
	private BankAccountService bankAccountService;
	
	@Autowired
	private UserService userService;
	
	@Override
	public void saveTransferTransaction(TransferTransaction transferTransaction) throws CurrencyParseExcp,TransferTransactionNotFoundExcp {
		if (transferTransaction.getId() != 0) {
			TransferTransaction oldTransferTransaction = getTransferTransactionById(transferTransaction.getId());
			bankAccountService.changeBalanceDeleteTransaction(oldTransferTransaction);
		}
	
		bankAccountService.changeBalanceSaveTransaction(transferTransaction);
		transferTransactionRepository.save(transferTransaction);
	}

	@Override
	public void deleteTransferTransaction(TransferTransaction transferTransaction) {
		bankAccountService.changeBalanceDeleteTransaction(transferTransaction);
		transferTransactionRepository.delete(transferTransaction);
	}

	@Override
	public TransferTransaction getTransferTransactionById(Long id) throws NoSuchElementException {
		AppUser appUser = userService.getCurrentAppUserFromContextOrCreateDemoUser();
		Optional<TransferTransaction> transferTransactionOptional = transferTransactionRepository.findByAppUserAndId(appUser,id);
		return transferTransactionOptional.get();
	}

	@Override
	public List<TransferTransaction> getTransferTransactions() {
		AppUser theAppUser = userService.getCurrentAppUserFromContextOrCreateDemoUser();
		return transferTransactionRepository.findAllByAppUserOrderByCreateTimeDesc(theAppUser);
	}

}
