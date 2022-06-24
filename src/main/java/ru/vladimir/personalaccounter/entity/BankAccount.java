package ru.vladimir.personalaccounter.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "bank_account")
public class BankAccount implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	
	@Column
	@NotBlank
	@Length(min = 2, max = 256, message = "{bankAccount.validation.name}")
	private String name;
	
	@Column
	@NotNull
	@Digits(integer = 20, fraction = 2, message = "{bankAccount.validation.balance}")
	private BigDecimal balance;
	
	@Column
	private String description;
	
	@Column
	@NotNull
	private Currency currency;
	
	@ManyToOne
	@JoinColumn(name = "app_user_id", nullable = false)
	@NotNull
	@JsonIgnore
	private AppUser appUser;
	
	@OneToMany(mappedBy = "bankAccount",cascade = CascadeType.ALL)
	private List<AdjustmentTransaction> transactions;

	
	@Column(name = "default_account")
	private boolean defaultAccount;
	
	
	public BankAccount(@NotBlank @Length(min = 2, max = 256, message = "{bankAccount.validation.name}") String name,
			@Digits(integer = 20, fraction = 2, message = "{bankAccount.validation.balance}") BigDecimal balance,
			String description, @NotNull AppUser appUser, Currency currency) {
		super();
		this.name = name;
		this.balance = balance;
		this.description = description;
		this.appUser = appUser;
		this.currency = currency;
	}
	/**
	 *TODO check, maybe it use only for test 
	 * @param bankAccount
	 */
	public BankAccount(BankAccount bankAccount) {
		super();
		this.name = bankAccount.getName();
		this.balance = bankAccount.getBalance();
		this.description = bankAccount.getDescription();
		this.appUser = bankAccount.getAppUser();
		this.currency = bankAccount.getCurrency();
	}
	
	

	public BankAccount(Long id,
			@NotBlank @Length(min = 2, max = 256, message = "{bankAccount.validation.name}") String name,
			@Digits(integer = 20, fraction = 2, message = "{bankAccount.validation.balance}") BigDecimal balance,
			String description, @NotNull Currency currency, @NotNull AppUser appUser,
			List<AdjustmentTransaction> transactions) {
		super();
		this.id = id;
		this.name = name;
		this.balance = balance;
		this.description = description;
		this.currency = currency;
		this.appUser = appUser;
		this.transactions = transactions;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		
		if (obj.getClass() != this.getClass()) {
			return false;
		}
		BankAccount accountForCompare = (BankAccount) obj;
		if (accountForCompare.getId() != this.id) {
			return false;
		}
		
		if (!accountForCompare.getName().equals(this.name)) {
			return false;
		}
		
		return true;
		
	}
	
	public void addBankAccountAdjstmens(AdjustmentTransaction accountAdjustment) {
		if (transactions == null) transactions = new ArrayList<>();
		transactions.add(accountAdjustment);
	}
}
