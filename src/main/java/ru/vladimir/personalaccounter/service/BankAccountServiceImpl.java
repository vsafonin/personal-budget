package ru.vladimir.personalaccounter.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.TypedQuery;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ru.vladimir.personalaccounter.client.CurrencyConverterClient;
import ru.vladimir.personalaccounter.client.CurrencyParseExcp;
import ru.vladimir.personalaccounter.entity.AbstractTransaction;
import ru.vladimir.personalaccounter.entity.AppUser;
import ru.vladimir.personalaccounter.entity.BankAccount;
import ru.vladimir.personalaccounter.entity.DebtTransaction;
import ru.vladimir.personalaccounter.entity.PurchaseTransaction;
import ru.vladimir.personalaccounter.entity.SalaryTransaction;
import ru.vladimir.personalaccounter.entity.TransferTransaction;
import ru.vladimir.personalaccounter.enums.TypeOfOperation;
import ru.vladimir.personalaccounter.exception.BankAccountNotFoundException;
import ru.vladimir.personalaccounter.repository.BankAccountRepository;

@Service
public class BankAccountServiceImpl implements BankAccountService {

	@Autowired
	private BankAccountRepository accountRepository;

	@Autowired
	private UserService userService;

	@Autowired
	private EntityManagerFactory entityManagerFactory; // i don't want get salarty,debt,purchase transaction, use jpl

	@Autowired
	private CurrencyConverterClient currencyConverterClient;

	@Override
	public List<BankAccount> getBankAccounts() {
		AppUser theAppUser = userService.getCurrentAppUserFromContextOrCreateDemoUser();
		return accountRepository.getBankAccounts(theAppUser);
	}

	@Override
	public BankAccount getBankAccountById(Long id) {
		AppUser theAppUser = userService.getCurrentAppUserFromContextOrCreateDemoUser();
		Optional<BankAccount> bankAccount = accountRepository.findById(theAppUser, id);
		if (bankAccount.isPresent()) {
			return bankAccount.get();
		}
		throw new BankAccountNotFoundException("trying get bank account, but this isn't exist");
	}

	@Override
	public BankAccount save(BankAccount bankAccount) {
		AppUser theAppUser = userService.getCurrentAppUserFromContextOrCreateDemoUser();
		List<BankAccount> bankAccounts = accountRepository.getBankAccounts(theAppUser);
		//if we have only one bank account set it default
		if (bankAccounts.size() == 1 ) {
				if (!bankAccount.isDefaultAccount()) {
					bankAccount.setDefaultAccount(true);
				}
				}
		else if (bankAccount.isDefaultAccount()) {
			bankAccounts
			.stream()
				.filter(ba -> !ba.equals(bankAccount))
				.filter(ba -> ba.isDefaultAccount() == true).forEach(ba -> {
					ba.setDefaultAccount(false);
					accountRepository.save(ba);
				});
		}
		
		return accountRepository.save(bankAccount);
	}

	@Override
	public void delete(BankAccount bankAccount) {
		accountRepository.delete(bankAccount);
	}

	@Override
	public boolean bankAccountNameIsNotExist(String bankAccountName) {
		AppUser theAppUser = userService.getCurrentAppUserFromContextOrCreateDemoUser();
		BankAccount bankAccount = accountRepository.getBankAccounts(theAppUser).stream()
				.filter(a -> a.getName().equals(bankAccountName)).findAny().orElse(null);
		return bankAccount != null;
	}

	@Override
	public boolean bankAccountHasTransaction(BankAccount bankAccount) {
		String sqlSalary = "Select s from SalaryTransaction as s where s.bankAccount = :BankAccount";
		String sqlPurchase = "Select p from PurchaseTransaction as p where p.bankAccount = :BankAccount";
		String sqlDebt = "Select d from DebtTransaction as d where d.bankAccount = :BankAccount";
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		TypedQuery<SalaryTransaction> querySalary = entityManager.createQuery(sqlSalary, SalaryTransaction.class);
		List<SalaryTransaction> salarys = querySalary.setParameter("BankAccount", bankAccount).setMaxResults(1)
				.getResultList();

		TypedQuery<PurchaseTransaction> queryPurchase = entityManager.createQuery(sqlPurchase,
				PurchaseTransaction.class);
		List<PurchaseTransaction> purchases = queryPurchase.setParameter("BankAccount", bankAccount).setMaxResults(1)
				.getResultList();

		TypedQuery<DebtTransaction> queryDebt = entityManager.createQuery(sqlDebt, DebtTransaction.class);
		List<DebtTransaction> debts = queryDebt.setParameter("BankAccount", bankAccount).setMaxResults(1)
				.getResultList();

		List<List<? extends AbstractTransaction>> result = new ArrayList<List<? extends AbstractTransaction>>();
		result.add(salarys);
		result.add(purchases);
		result.add(debts);
		return result != null;
	}

	@Override
	public void delete(BankAccount bankAccount, boolean force) {
		if (force) {
			String sqlDeleteTransactionsSalary = "Delete from SalaryTransaction t where t.bankAccount = :bankAccount";
			String sqlDeleteTransactionsDebt = "Delete from DebtTransaction d where d.bankAccount = :bankAccount";
			String sqlDeleteTransactionsPurchase = "Delete from PurchaseTransaction p where p.bankAccount = :bankAccount";
			EntityManager entityManager = entityManagerFactory.createEntityManager();
			entityManager.getTransaction().begin();
			entityManager.createQuery(sqlDeleteTransactionsSalary).setParameter("bankAccount", bankAccount)
					.executeUpdate();
			entityManager.createQuery(sqlDeleteTransactionsDebt).setParameter("bankAccount", bankAccount)
					.executeUpdate();
			entityManager.createQuery(sqlDeleteTransactionsPurchase).setParameter("bankAccount", bankAccount)
					.executeUpdate();
			entityManager.getTransaction().commit();
			;
			delete(bankAccount);
		} else {
			delete(bankAccount);
		}
	}

	@Override
	public void changeBalanceSaveTransaction(AbstractTransaction abstractTransaction) {
		BankAccount theBankAccount = abstractTransaction.getBankAccount();
		if (abstractTransaction.getTypeOfOperation() == TypeOfOperation.DECREASE) {
			theBankAccount.setBalance(theBankAccount.getBalance().subtract(abstractTransaction.getSumTransaction()));
		} else if (abstractTransaction.getTypeOfOperation() == TypeOfOperation.INCREASE) {
			theBankAccount.setBalance(theBankAccount.getBalance().add(abstractTransaction.getSumTransaction()));
		}
		save(theBankAccount);

	}

	@Override
	public void changeBalanceSaveTransaction(AbstractTransaction oldTransaction, AbstractTransaction newTransaction) {
		BankAccount theBankAccount = newTransaction.getBankAccount();
		BigDecimal oldSum = oldTransaction.getSumTransaction();
		BigDecimal newSum = newTransaction.getSumTransaction();
		int compareSum = oldSum.compareTo(newSum);
		BigDecimal diffSum = compareSum > 0 ? oldSum.subtract(newSum) : newSum.subtract(oldSum);
		if (oldTransaction.getBankAccount().equals(newTransaction.getBankAccount())) {
			if (compareSum > 0) {
				if (oldTransaction.getTypeOfOperation() == TypeOfOperation.DECREASE) {
					theBankAccount.setBalance(theBankAccount.getBalance().add(diffSum));
				} else if (oldTransaction.getTypeOfOperation() == TypeOfOperation.INCREASE) {
					theBankAccount.setBalance(theBankAccount.getBalance().subtract(diffSum));
				}
			} else if (compareSum < 0) {

				if (oldTransaction.getTypeOfOperation() == TypeOfOperation.DECREASE) {
					theBankAccount.setBalance(theBankAccount.getBalance().subtract(diffSum));
				} else if (oldTransaction.getTypeOfOperation() == TypeOfOperation.INCREASE) {
					theBankAccount.setBalance(theBankAccount.getBalance().add(diffSum));
				}
			}
		} else {
			// change bank
			BankAccount theOldBankAccount = oldTransaction.getBankAccount();
			if (oldTransaction.getTypeOfOperation() == TypeOfOperation.DECREASE) {
				theOldBankAccount.setBalance(theOldBankAccount.getBalance().add(oldSum));
			} else if (oldTransaction.getTypeOfOperation() == TypeOfOperation.INCREASE) {
				theOldBankAccount.setBalance(theOldBankAccount.getBalance().subtract(oldSum));
			}
			if (newTransaction.getTypeOfOperation() == TypeOfOperation.DECREASE) {
				theBankAccount.setBalance(theBankAccount.getBalance().subtract(newSum));
			} else if (newTransaction.getTypeOfOperation() == TypeOfOperation.INCREASE) {
				theBankAccount.setBalance(theBankAccount.getBalance().add(newSum));
			}
			save(theOldBankAccount);
		}
		save(theBankAccount);
	}

	@Override
	public void changeBalanceDeleteTransaction(AbstractTransaction abstractTransaction) {
		BankAccount theAccount = abstractTransaction.getBankAccount();
		if (abstractTransaction.getTypeOfOperation() == TypeOfOperation.DECREASE) {
			theAccount.setBalance(theAccount.getBalance().add(abstractTransaction.getSumTransaction()));
		} else if (abstractTransaction.getTypeOfOperation() == TypeOfOperation.INCREASE) {
			theAccount.setBalance(theAccount.getBalance().subtract(abstractTransaction.getSumTransaction()));
		}
		save(theAccount);
	}

	@Override
	public void changeBalanceSaveTransaction(TransferTransaction transferTransaction) throws CurrencyParseExcp {

		BankAccount oldBankAccount = transferTransaction.getFromBankAccount();
		BankAccount newBankAccount = transferTransaction.getToBankAccount();
		// check currency
		BigDecimal sumForAddToNewAccount;
		BigDecimal sumForOldBankAccount = oldBankAccount.getBalance().subtract(transferTransaction.getSumTransactionFrom());
		if (!oldBankAccount.getCurrency().equals(newBankAccount.getCurrency())) {
			sumForAddToNewAccount = currencyConverterClient.getConvertedCurrencySum(oldBankAccount.getCurrency(),
					newBankAccount.getCurrency(), transferTransaction.getSumTransactionFrom());
		} else {
			sumForAddToNewAccount = transferTransaction.getSumTransactionFrom();
		}

		transferTransaction.setSumTransactionTo(sumForAddToNewAccount);
		oldBankAccount.setBalance(sumForOldBankAccount);
		newBankAccount.setBalance(newBankAccount.getBalance().add(sumForAddToNewAccount));
		save(newBankAccount);
		save(oldBankAccount);
	}

	@Override
	public void changeBalanceDeleteTransaction(TransferTransaction transferTransaction) {
		BankAccount oldBankAccount = transferTransaction.getFromBankAccount();
		BankAccount newBankAccount = transferTransaction.getToBankAccount();

		newBankAccount.setBalance(newBankAccount.getBalance().subtract(transferTransaction.getSumTransactionTo()));
		oldBankAccount.setBalance(oldBankAccount.getBalance().add(transferTransaction.getSumTransactionFrom()));
		save(newBankAccount);
		save(oldBankAccount);
	}

}
