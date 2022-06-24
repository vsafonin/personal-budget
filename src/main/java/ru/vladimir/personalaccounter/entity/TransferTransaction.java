package ru.vladimir.personalaccounter.entity;

import java.math.BigDecimal;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "transfer_transaction")
@Getter
@Setter
@NoArgsConstructor
public class TransferTransaction {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private long id;
	
	@ManyToOne
	@JoinColumn(name = "app_user_id", nullable = false)
	private AppUser appUser;
	
	@ManyToOne
	@JoinColumn(name = "from_bank_account_id", nullable = false)
	@NotNull
	private BankAccount fromBankAccount;
	
	@ManyToOne
	@JoinColumn(name = "to_bank_account_id", nullable = false)
	@NotNull
	private BankAccount toBankAccount;
	
	/**
	 * cause we may have different currency in bank account, we have to save both value sum
	 * sum from and sum to
	 */
	@Column(name = "sum_transaction_from")
	@NotNull
	private BigDecimal sumTransactionFrom;

	@Column(name = "sum_transaction_to")
	private BigDecimal sumTransactionTo;
	
	@Column(name = "create_time", nullable = false)
	private Timestamp createTime;
}
