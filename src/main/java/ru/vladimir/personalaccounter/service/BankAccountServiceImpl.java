package ru.vladimir.personalaccounter.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ru.vladimir.personalaccounter.client.CurrencyConverterClient;
import ru.vladimir.personalaccounter.client.CurrencyParseExcp;
import ru.vladimir.personalaccounter.entity.AbstractTransaction;
import ru.vladimir.personalaccounter.entity.AppUser;
import ru.vladimir.personalaccounter.entity.BankAccount;
import ru.vladimir.personalaccounter.entity.TransferTransaction;
import ru.vladimir.personalaccounter.enums.TypeOfOperation;
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
	public BankAccount getBankAccountById(Long id) throws NoSuchElementException{
		AppUser theAppUser = userService.getCurrentAppUserFromContextOrCreateDemoUser();
		Optional<BankAccount> bankAccount = accountRepository.findById(theAppUser, id);
		return bankAccount.get();
	}

	@Override
	public BankAccount save(BankAccount bankAccount) {
		AppUser theAppUser = userService.getCurrentAppUserFromContextOrCreateDemoUser();
		List<BankAccount> bankAccounts = accountRepository.getBankAccounts(theAppUser);
		//if we have only one bank account set it default
		if (bankAccounts == null) {
				if (!bankAccount.isDefaultAccount()) {
					bankAccount.setDefaultAccount(true);
				}
				}
		else if (bankAccount.isDefaultAccount()) {
			for (int i = 0 ; i < bankAccounts.size(); i++) {
				if (bankAccounts.get(i).isDefaultAccount()) {
					BankAccount tmpBankAccount = bankAccounts.get(i);
					tmpBankAccount.setDefaultAccount(false);
					accountRepository.save(tmpBankAccount);
				}
			}
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
		boolean result = false;
		String sqlCountRows =  "select sum(tb.transactions) "
				+ "from ( select count(*) as transactions from salary_transaction where bank_account_id = :bankAccountId "
				+ "union all select count(*) as transactions from debt_transaction where bank_account_id = :bankAccountId "
				+ "union all select count(*) as transactions from purchase_transaction where bank_account_id = :bankAccountId "
				+ "union all select count(*) as transactions from transfer_transaction where "
				+ "from_bank_account_id = :bankAccountId or to_bank_account_id = :bankAccountId "
				+ ")tb";
		EntityManager em = entityManagerFactory.createEntityManager();
		Query query = em.createNativeQuery(sqlCountRows);
		query.setParameter("bankAccountId", bankAccount.getId());
		BigDecimal count = (BigDecimal) query.getSingleResult();
		if (count != null && count.compareTo(BigDecimal.ZERO) != 0) {
			result = true;
		}
		return result;
	}

	@Override
	public void delete(BankAccount bankAccount, boolean force) {
		if (force) {
			String sqlDeleteTransactionsSalary = "Delete from SalaryTransaction t where t.bankAccount = :bankAccount";
			String sqlDeleteTransactionsDebt = "Delete from DebtTransaction d where d.bankAccount = :bankAccount";
			String sqlDeleteTransactionsPurchase = "Delete from PurchaseTransaction p where p.bankAccount = :bankAccount";
			String sqlDeleteTransactionsеTransfer = "Delete from TransferTransaction p where p.fromBankAccount = :bankAccount "
					+ "or p.toBankAccount = :bankAccount";
			EntityManager entityManager = entityManagerFactory.createEntityManager();
			
			entityManager.getTransaction().begin();
				entityManager.createQuery(sqlDeleteTransactionsSalary).setParameter("bankAccount", bankAccount)
						.executeUpdate();
				entityManager.createQuery(sqlDeleteTransactionsDebt).setParameter("bankAccount", bankAccount)
						.executeUpdate();
				entityManager.createQuery(sqlDeleteTransactionsPurchase).setParameter("bankAccount", bankAccount)
						.executeUpdate();
				entityManager.createQuery(sqlDeleteTransactionsеTransfer).setParameter("bankAccount", bankAccount)
				.executeUpdate();
			entityManager.getTransaction().commit();

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
