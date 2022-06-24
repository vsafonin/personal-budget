package ru.vladimir.personalaccounter.entity;

import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.Future;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "debt_transaction")
public class DebtTransaction extends AbstractTransaction {

	private static final long serialVersionUID = 1L;

	@Column
	private boolean active;
	
	@Column
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	@Future(message = "{transaction.debt.valid.endDate}")
	private Date endDate;
	
	@ManyToOne(cascade = {CascadeType.PERSIST,CascadeType.DETACH,CascadeType.REFRESH,CascadeType.MERGE})
	@JoinColumn(name = "partner_id", nullable = false)
	private Partner partner;
	
	@OneToMany(mappedBy = "debtTransaction", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	private List<DebtPayment> debtPayment;
	
	
	@Column(name = "description")
	private String description;
}
