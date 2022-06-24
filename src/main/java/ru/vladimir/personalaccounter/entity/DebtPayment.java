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

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "debt_payments")
@Getter
@Setter
@NoArgsConstructor
public class DebtPayment {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private long id;
	
	@Column(name = "pay_date")
	private Timestamp payDate;
	
	@Column(name = "pay_sum")
	private BigDecimal paySum;
	
	@ManyToOne
	@JoinColumn(name = "debt_transaction_id", nullable = false)
	private DebtTransaction debtTransaction;

}
