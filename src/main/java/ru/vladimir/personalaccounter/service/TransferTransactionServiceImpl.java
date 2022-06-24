package ru.vladimir.personalaccounter.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ru.vladimir.personalaccounter.client.CurrencyParseExcp;
import ru.vladimir.personalaccounter.entity.AppUser;
import ru.vladimir.personalaccounter.entity.TransferTransaction;
import ru.vladimir.personalaccounter.exception.UserGetDataSecurityExp;
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
	public void saveTransferTransaction(TransferTransaction transferTransaction) throws CurrencyParseExcp {
		//check if transfer transaction is new
		if (transferTransaction.getId() != 0) {
			//i think is good choice delete old transfer transaction in bankaccoun..otherwise i have to do get and modify bank accounts
			Optional<TransferTransaction> oldTransferTransaction = transferTransactionRepository.findById(transferTransaction.getId());
			if (oldTransferTransaction.isPresent()) {
				bankAccountService.changeBalanceDeleteTransaction(oldTransferTransaction.get());
			}
			else {
				throw new TransferTransactionNotFoundExcp("transfer not found, it's impossible");
			}
		}
	
		bankAccountService.changeBalanceSaveTransaction(transferTransaction);
		transferTransactionRepository.save(transferTransaction);
	}

	@Override
	public void deleteTransferTransaction(TransferTransaction transferTransaction) {
		AppUser theAppUser = userService.getCurrentAppUserFromContextOrCreateDemoUser();
		if (transferTransaction.getAppUser().equals(theAppUser)) {
			bankAccountService.changeBalanceDeleteTransaction(transferTransaction);
			transferTransactionRepository.delete(transferTransaction);
		}
		else {
			throw new UserGetDataSecurityExp("someone tryig delete transfer transaction but he isn't owner");
		}

	}

	@Override
	public TransferTransaction getTransferTransactionById(Long id) throws TransferTransactionNotFoundExcp {
		Optional<TransferTransaction> transferTransactionOptional = transferTransactionRepository.findById(id);
		if(!transferTransactionOptional.isPresent()) {
			throw new TransferTransactionNotFoundExcp("transfer transaction not found " + id );
		}
		AppUser theAppUser = userService.getCurrentAppUserFromContextOrCreateDemoUser();
		if (transferTransactionOptional.get().getAppUser().equals(theAppUser)) {
			return transferTransactionOptional.get();
		}
		else {
			throw new UserGetDataSecurityExp("someone tryig get transfer transaction but he isn't owner");
		}
	}

	@Override
	public List<TransferTransaction> getTransferTransactions() {
		return transferTransactionRepository.findAll();
	}

}
