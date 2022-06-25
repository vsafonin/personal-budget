package ru.vladimir.personalaccounter.controllerAdvice;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import ru.vladimir.personalaccounter.exception.BankAccountNotFoundExp;
import ru.vladimir.personalaccounter.exception.DebtPaymentNotFoundExp;
import ru.vladimir.personalaccounter.exception.DebtTransactionNotFoundExp;
import ru.vladimir.personalaccounter.exception.DefaultBankAccountNotFoundExcp;
import ru.vladimir.personalaccounter.exception.PurchaseTransactionExistsExp;
import ru.vladimir.personalaccounter.exception.PurchaseTransactionNotFound;
import ru.vladimir.personalaccounter.exception.SalaryTransactionNotFoundExcp;
import ru.vladimir.personalaccounter.exception.TransferTransactionNotFoundExcp;
import ru.vladimir.personalaccounter.exception.UserGetDataSecurityExp;
import ru.vladimir.personalaccounter.exception.AdjustmentTransactionNotFoundExp;

@ControllerAdvice
public class ExceptionHandlerControllerAdvice {

	
	@ExceptionHandler(UserGetDataSecurityExp.class)
	@ResponseStatus(code = HttpStatus.FORBIDDEN)
	public void handleUserGetDataSecurityException(UserGetDataSecurityExp exp) {
	}
	
	@ExceptionHandler(AdjustmentTransactionNotFoundExp.class)
	@ResponseStatus(code = HttpStatus.NOT_FOUND)
	public void handleUsrTransactionNotFoundExp(AdjustmentTransactionNotFoundExp exp) {
	}

	@ExceptionHandler(BankAccountNotFoundExp.class)
	@ResponseStatus(code = HttpStatus.NOT_FOUND)
	public void handleBankAccountNotFoundExp(BankAccountNotFoundExp exp) {
	}
	
	@ExceptionHandler(PurchaseTransactionNotFound.class)
	@ResponseStatus(code = HttpStatus.NOT_FOUND)
	public void handlePurchaseTransactionNotFoundExp(PurchaseTransactionNotFound exp) {
	}
	
	@ExceptionHandler(SalaryTransactionNotFoundExcp.class)
	@ResponseStatus(code = HttpStatus.NOT_FOUND)
	public void handleSalaryTransactionNotFoundExcp(SalaryTransactionNotFoundExcp exp) {
	}
	
	@ExceptionHandler(DebtPaymentNotFoundExp.class)
	@ResponseStatus(code = HttpStatus.NOT_FOUND)
	public void handledebtPaymentNotFoundExp(DebtPaymentNotFoundExp exp) {
	}
	
	@ExceptionHandler(TransferTransactionNotFoundExcp.class)
	@ResponseStatus(code = HttpStatus.NOT_FOUND)
	public void handleTransferTransactionNotFoundExcp(TransferTransactionNotFoundExcp exp) {
	}
	
	@ExceptionHandler(DebtTransactionNotFoundExp.class)
	@ResponseStatus(code = HttpStatus.NOT_FOUND)
	public void handleDebtTransactionNotFoundExcp(DebtTransactionNotFoundExp exp) {
	}
	
	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<Object> handleIllegalArgumentException(IllegalArgumentException exp) {
		        return new ResponseEntity<Object>(
		          exp.getMessage(), new HttpHeaders(), HttpStatus.UNPROCESSABLE_ENTITY);
	}
	
	@ExceptionHandler(PurchaseTransactionExistsExp.class)
	public ResponseEntity<Object> handlePurchaseTransactionExistsException(PurchaseTransactionExistsExp exp) {
		        return new ResponseEntity<Object>(
		          exp.getMessage(), new HttpHeaders(), HttpStatus.CONFLICT);
	}
	
	@ExceptionHandler(DefaultBankAccountNotFoundExcp.class)
	public ResponseEntity<Object> handdleDefaultBankAccountNotFoundExcp(DefaultBankAccountNotFoundExcp exp) {
		 return new ResponseEntity<Object>(
		          exp.getMessage(), new HttpHeaders(), HttpStatus.UNPROCESSABLE_ENTITY);
	}
}
