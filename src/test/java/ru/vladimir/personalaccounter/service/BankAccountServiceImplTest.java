package ru.vladimir.personalaccounter.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import ru.vladimir.personalaccounter.client.CurrencyConverterClient;
import ru.vladimir.personalaccounter.client.CurrencyParseExcp;
import ru.vladimir.personalaccounter.entity.AbstractTransaction;
import ru.vladimir.personalaccounter.entity.AppUser;
import ru.vladimir.personalaccounter.entity.BankAccount;
import ru.vladimir.personalaccounter.entity.SalaryTransaction;
import ru.vladimir.personalaccounter.entity.TransferTransaction;
import ru.vladimir.personalaccounter.enums.TypeOfOperation;
import ru.vladimir.personalaccounter.repository.BankAccountRepository;

@ExtendWith(MockitoExtension.class)
@ExtendWith(SpringExtension.class)
class BankAccountServiceImplTest {
	
	
	@Mock
	private UserService userService;
	
	@InjectMocks
	private BankAccountService bankAccountService = new BankAccountServiceImpl();
	
	@Mock
	private BankAccountRepository accountRepository;
	
	@Mock
	private CurrencyConverterClient currencyConverterClient;
	
	
	private AppUser appUser;
	
	public BankAccountServiceImplTest() {
		appUser = new AppUser();
		
	}
	
	@BeforeEach
	private void doInit() {
		when(userService.getCurrentAppUserFromContextOrCreateDemoUser()).thenReturn(appUser);
	}
	/*
	 * тестирую что создании нового банковского счета и он единственный, ему присвоен статус isDefault
	 */
	@Test
	void testSaveBankAccount_should_set_isDefault() {
		when(accountRepository.getBankAccounts(any())).thenReturn(null);
		BankAccount thBankAccount = new BankAccount();
		bankAccountService.save(thBankAccount);
		assertThat(thBankAccount.isDefaultAccount()).isTrue();
	}
	
	
	/*
	 * тестирую что при создании нового банковского счет и их несколько, и у этого счета установлен признак isDefault()
	 * у другого счета этот признак был снят
	 */
	@Test
	void testSaveBankAccount_shoud_disable_isDefault_in_anotherAccounts() {
		BankAccount theOldBankAccount = new BankAccount();
		theOldBankAccount.setName("oldBankAccount");
		theOldBankAccount.setId(1L);
		theOldBankAccount.setDefaultAccount(true);
		
		BankAccount theNewBankAccount = new BankAccount();
		theNewBankAccount.setName("newBankAccount");
		theNewBankAccount.setDefaultAccount(true);
		
		when(accountRepository.getBankAccounts(any())).thenReturn(List.of(theOldBankAccount));
		bankAccountService.save(theNewBankAccount);
		assertThat(theNewBankAccount.isDefaultAccount()).isTrue();
		assertThat(theOldBankAccount.isDefaultAccount()).isFalse();
	}
	
	/*
	 * тестирую что при вызове метода changeBalanceSaveTransaction меняется сумма баланса BankAccount
	 * проверяю что меняется в нужную сторону.
	 */
	@Test
	void testChangeBalanceSaveTransactionAbstractTransaction() {
		
		BankAccount theBankAccount = new BankAccount();
		theBankAccount.setBalance(BigDecimal.valueOf(100));
		theBankAccount.setAppUser(appUser);
		when(accountRepository.getBankAccounts(appUser)).thenReturn(List.of(theBankAccount));
		
		AbstractTransaction theAbstractTransaction = new SalaryTransaction();
		theAbstractTransaction.setTypeOfOperation(TypeOfOperation.DECREASE);
		theAbstractTransaction.setSumTransaction(BigDecimal.TEN);
		theAbstractTransaction.setBankAccount(theBankAccount);
		theAbstractTransaction.setAppUser(appUser);
		
		bankAccountService.changeBalanceSaveTransaction(theAbstractTransaction);
		assertThat(theBankAccount.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(90));
		
		theAbstractTransaction.setTypeOfOperation(TypeOfOperation.INCREASE);
		bankAccountService.changeBalanceSaveTransaction(theAbstractTransaction);
		assertThat(theBankAccount.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(100));
	}

	/*
	 *тест похож на testChangeBalanceSaveTransactionAbstractTransaction()
	 *но только для операций когда заданы 2 транзакции. 
	 */
	@Test
	void testChangeBalanceSaveTransactionAbstractTransactionAbstractTransaction() {
		BankAccount theBankAccount = new BankAccount();
		theBankAccount.setBalance(BigDecimal.valueOf(100));
		theBankAccount.setAppUser(appUser);
		theBankAccount.setName("test");
		when(accountRepository.getBankAccounts(appUser)).thenReturn(List.of(theBankAccount));
		
		AbstractTransaction theAbstractTransactionOld = new SalaryTransaction();
		theAbstractTransactionOld.setTypeOfOperation(TypeOfOperation.DECREASE);
		theAbstractTransactionOld.setSumTransaction(BigDecimal.TEN);
		theAbstractTransactionOld.setBankAccount(theBankAccount);

		AbstractTransaction theAbstractTransactionNew = new SalaryTransaction();
		theAbstractTransactionNew.setTypeOfOperation(TypeOfOperation.DECREASE);
		theAbstractTransactionNew.setSumTransaction(BigDecimal.valueOf(20));
		theAbstractTransactionNew.setBankAccount(theBankAccount);
		
		bankAccountService.changeBalanceSaveTransaction(theAbstractTransactionOld,theAbstractTransactionNew);
		assertThat(theBankAccount.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(90)); //was 100 - 10 *old - ( new20 - old10) = 90
		
	}
	/*
	 * тест такой же как testChangeBalanceSaveTransactionAbstractTransactionAbstractTransaction только старая транзакция была больше чем новая
	 */
	@Test
	void testChangeBalanceSaveTransactionAbstractTransactionAbstractTransactionOldBetterThenNew() {
		BankAccount theBankAccount = new BankAccount();
		theBankAccount.setBalance(BigDecimal.valueOf(100));
		theBankAccount.setAppUser(appUser);
		theBankAccount.setName("test");
		when(accountRepository.getBankAccounts(appUser)).thenReturn(List.of(theBankAccount));
		
		AbstractTransaction theAbstractTransactionOld = new SalaryTransaction();
		theAbstractTransactionOld.setTypeOfOperation(TypeOfOperation.DECREASE);
		theAbstractTransactionOld.setSumTransaction(BigDecimal.valueOf(20));
		theAbstractTransactionOld.setBankAccount(theBankAccount);
		
		AbstractTransaction theAbstractTransactionNew = new SalaryTransaction();
		theAbstractTransactionNew.setTypeOfOperation(TypeOfOperation.DECREASE);
		theAbstractTransactionNew.setSumTransaction(BigDecimal.TEN);
		theAbstractTransactionNew.setBankAccount(theBankAccount);
		
		bankAccountService.changeBalanceSaveTransaction(theAbstractTransactionOld,theAbstractTransactionNew);
		assertThat(theBankAccount.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(110)); //was 100 + 20 *old - ( old20 - new10) = 110
		
	}
	/*
	 * тестирую когда при сохранении транзакций у них разные банковские счета
	 */
	@Test
	void testChangeBalanceSaveTransactionAbstractTransactionAbstractTransactionDifferentBank() {
		BankAccount theBankAccountOld = new BankAccount();
		theBankAccountOld.setBalance(BigDecimal.valueOf(100));
		theBankAccountOld.setAppUser(appUser);
		theBankAccountOld.setName("test");
		BankAccount theBankAccountNew = new BankAccount();
		theBankAccountNew.setBalance(BigDecimal.valueOf(100));
		theBankAccountNew.setAppUser(appUser);
		theBankAccountNew.setName("test2");
		
		when(accountRepository.getBankAccounts(appUser)).thenReturn(List.of(theBankAccountNew,theBankAccountOld));
		
		AbstractTransaction theAbstractTransactionOld = new SalaryTransaction();
		theAbstractTransactionOld.setTypeOfOperation(TypeOfOperation.DECREASE);
		theAbstractTransactionOld.setSumTransaction(BigDecimal.TEN);
		theAbstractTransactionOld.setBankAccount(theBankAccountOld);
		
		AbstractTransaction theAbstractTransactionNew = new SalaryTransaction();
		theAbstractTransactionNew.setTypeOfOperation(TypeOfOperation.DECREASE);
		theAbstractTransactionNew.setSumTransaction(BigDecimal.TEN);
		theAbstractTransactionNew.setBankAccount(theBankAccountNew);
		
		bankAccountService.changeBalanceSaveTransaction(theAbstractTransactionOld,theAbstractTransactionNew);
		assertThat(theBankAccountNew.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(90)); //was 100 - 10
		assertThat(theBankAccountOld.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(110)); //was 100 - 10
		
	}
	/*
	 * тестирую самую сложную ситуацию когда у транзакций разные банки и суммы
	 */
	@Test
	void testChangeBalanceSaveTransactionAbstractTransactionAbstractTransactionDifferentBankAndSum() {
		BankAccount theBankAccountOld = new BankAccount();
		theBankAccountOld.setBalance(BigDecimal.valueOf(100));
		theBankAccountOld.setAppUser(appUser);
		theBankAccountOld.setName("test");
		BankAccount theBankAccountNew = new BankAccount();
		theBankAccountNew.setBalance(BigDecimal.valueOf(100));
		theBankAccountNew.setAppUser(appUser);
		theBankAccountNew.setName("test2");
		
		when(accountRepository.getBankAccounts(appUser)).thenReturn(List.of(theBankAccountNew,theBankAccountOld));
		
		AbstractTransaction theAbstractTransactionOld = new SalaryTransaction();
		theAbstractTransactionOld.setTypeOfOperation(TypeOfOperation.DECREASE);
		theAbstractTransactionOld.setSumTransaction(BigDecimal.TEN);
		theAbstractTransactionOld.setBankAccount(theBankAccountOld);
		
		AbstractTransaction theAbstractTransactionNew = new SalaryTransaction();
		theAbstractTransactionNew.setTypeOfOperation(TypeOfOperation.DECREASE);
		theAbstractTransactionNew.setSumTransaction(BigDecimal.valueOf(20));
		theAbstractTransactionNew.setBankAccount(theBankAccountNew);
		
		bankAccountService.changeBalanceSaveTransaction(theAbstractTransactionOld,theAbstractTransactionNew);
		assertThat(theBankAccountNew.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(80)); //was 100 - 10
		assertThat(theBankAccountOld.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(110)); //was 100 - 10
		
	}
	/*
	 * проверяю что баланс меняет в обратную сторону, так как этот метод вызывается при удалении транзакции.
	 */
	@Test
	void testChangeBalanceDeleteTransactionAbstractTransaction() {
		BankAccount theBankAccount = new BankAccount();
		theBankAccount.setBalance(BigDecimal.valueOf(100));
		theBankAccount.setAppUser(appUser);
		theBankAccount.setName("test");
		when(accountRepository.getBankAccounts(appUser)).thenReturn(List.of(theBankAccount));
		
		AbstractTransaction theAbstractTransaction = new SalaryTransaction();
		theAbstractTransaction.setTypeOfOperation(TypeOfOperation.DECREASE);
		theAbstractTransaction.setSumTransaction(BigDecimal.TEN);
		theAbstractTransaction.setBankAccount(theBankAccount);
		theAbstractTransaction.setAppUser(appUser);
		
		bankAccountService.changeBalanceDeleteTransaction(theAbstractTransaction);
		assertThat(theBankAccount.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(110)); //was 100 + 10
		theAbstractTransaction.setTypeOfOperation(TypeOfOperation.INCREASE);
		bankAccountService.changeBalanceDeleteTransaction(theAbstractTransaction);
		assertThat(theBankAccount.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(100)); //was 110 - 10
	}
	
	/*
	 * тестирую что баланс меняется при сохранении TransferTransaction
	 */
	@Test
	void testChangeBalanceSaveTransactionTransferTransaction() throws CurrencyParseExcp {
		BankAccount theBankAccountOld = new BankAccount();
		theBankAccountOld.setBalance(BigDecimal.valueOf(100));
		theBankAccountOld.setAppUser(appUser);
		theBankAccountOld.setName("test");
		theBankAccountOld.setCurrency(Currency.getInstance("RUB"));
		BankAccount theBankAccountNew = new BankAccount();
		theBankAccountNew.setBalance(BigDecimal.valueOf(100));
		theBankAccountNew.setAppUser(appUser);
		theBankAccountNew.setName("test2");
		theBankAccountNew.setCurrency(Currency.getInstance("RUB"));
		when(accountRepository.getBankAccounts(appUser)).thenReturn(List.of(theBankAccountOld,theBankAccountNew));
		
		TransferTransaction theTransactionTransferTransaction = new TransferTransaction();
		theTransactionTransferTransaction.setFromBankAccount(theBankAccountOld);
		theTransactionTransferTransaction.setToBankAccount(theBankAccountNew);
		theTransactionTransferTransaction.setAppUser(appUser);
		theTransactionTransferTransaction.setSumTransactionFrom(BigDecimal.valueOf(20));
		
		bankAccountService.changeBalanceSaveTransaction(theTransactionTransferTransaction);
		
		assertThat(theBankAccountOld.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(80));
		assertThat(theBankAccountNew.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(120));
	}
	/*
	 * тестирую что баланс BankAccount меняется при вызове changeBalanceSaveTransaction( TransferTransaction tr)
	 *  (вызывается при внесении изменений в TransferTransactions)
	 */
	@Test
	void testChangeBalanceSaveTransactionTransferTransactionDifferentCurrency() throws CurrencyParseExcp {
		BankAccount theBankAccountOld = new BankAccount();
		theBankAccountOld.setBalance(BigDecimal.valueOf(100));
		theBankAccountOld.setAppUser(appUser);
		theBankAccountOld.setName("test");
		theBankAccountOld.setCurrency(Currency.getInstance("RUB"));
		BankAccount theBankAccountNew = new BankAccount();
		theBankAccountNew.setBalance(BigDecimal.valueOf(100));
		theBankAccountNew.setAppUser(appUser);
		theBankAccountNew.setName("test2");
		theBankAccountNew.setCurrency(Currency.getInstance("USD"));
		when(accountRepository.getBankAccounts(appUser)).thenReturn(List.of(theBankAccountOld,theBankAccountNew));
		
		TransferTransaction theTransactionTransferTransaction = new TransferTransaction();
		theTransactionTransferTransaction.setFromBankAccount(theBankAccountOld);
		theTransactionTransferTransaction.setToBankAccount(theBankAccountNew);
		theTransactionTransferTransaction.setAppUser(appUser);
		theTransactionTransferTransaction.setSumTransactionFrom(BigDecimal.valueOf(20));
		
		when(currencyConverterClient.getConvertedCurrencySum(Currency.getInstance("RUB"), Currency.getInstance("USD"), BigDecimal.valueOf(20)))
			.thenReturn(BigDecimal.valueOf(5));
		bankAccountService.changeBalanceSaveTransaction(theTransactionTransferTransaction);
		
		assertThat(theBankAccountOld.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(80));
		assertThat(theBankAccountNew.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(105));
	}
	
	/*
	 * проверяю что при вызове метода changeBalanceDeleteTransaction(TransferTransaction tr)
	 * средства возвращяются BankAccount
	 */

	@Test
	void testChangeBalanceDeleteTransactionTransferTransaction() {
		BankAccount theBankAccountOld = new BankAccount();
		theBankAccountOld.setBalance(BigDecimal.valueOf(100));
		theBankAccountOld.setAppUser(appUser);
		theBankAccountOld.setName("test");
		theBankAccountOld.setCurrency(Currency.getInstance("RUB"));
		BankAccount theBankAccountNew = new BankAccount();
		theBankAccountNew.setBalance(BigDecimal.valueOf(100));
		theBankAccountNew.setAppUser(appUser);
		theBankAccountNew.setName("test2");
		theBankAccountNew.setCurrency(Currency.getInstance("USD"));
		when(accountRepository.getBankAccounts(appUser)).thenReturn(List.of(theBankAccountOld,theBankAccountNew));
		
		TransferTransaction theTransactionTransferTransaction = new TransferTransaction();
		theTransactionTransferTransaction.setFromBankAccount(theBankAccountOld);
		theTransactionTransferTransaction.setToBankAccount(theBankAccountNew);
		theTransactionTransferTransaction.setAppUser(appUser);
		theTransactionTransferTransaction.setSumTransactionFrom(BigDecimal.valueOf(20));
		theTransactionTransferTransaction.setSumTransactionTo(BigDecimal.valueOf(20));
		
		bankAccountService.changeBalanceDeleteTransaction(theTransactionTransferTransaction);
		assertThat(theBankAccountOld.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(120));
		assertThat(theBankAccountNew.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(80));
	}

}
