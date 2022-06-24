package ru.vladimir.personalaccounter.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Currency;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.Setter;
import ru.vladimir.personalaccounter.enums.TypeOfOperation;

@MappedSuperclass
@Getter
@Setter
public abstract class AbstractTransaction implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private long id;

	@ManyToOne
	@JoinColumn(name = "bank_account_id", nullable = false)
	private BankAccount bankAccount;

	@Column(name = "type_of_operations", nullable = false)
	private TypeOfOperation typeOfOperation;

	@Column(name = "create_time", nullable = false)
	private Timestamp createTime;

	@ManyToOne
	@JoinColumn(name = "app_user_id", nullable = false)
	@JsonIgnore
	private AppUser appUser;

	@Column(name = "sum_transaction")
	@DecimalMin(value = "0.01", message = "{purchaseTransaction.validation.message.totalSum}")
	@NotNull
	private BigDecimal sumTransaction;

	@Column(name = "currency")
    private Currency currency;
}
