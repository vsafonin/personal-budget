package ru.vladimir.personalaccounter.entity;


import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;
import ru.vladimir.personalaccounter.enums.TypeOfOperation;

@Entity
@Table(name = "salary_transaction")
@Getter
@Setter
public class SalaryTransaction extends AbstractTransaction {

	
	private static final long serialVersionUID = 1L;

	@ManyToOne(cascade = {CascadeType.PERSIST,CascadeType.DETACH,CascadeType.REFRESH,CascadeType.MERGE})
	@NotNull
	@JoinColumn(name = "partner_id", nullable = false)
	private Partner partner;

	@Column(name = "description")
	private String description;

	public SalaryTransaction() {
		this.setTypeOfOperation(TypeOfOperation.INCREASE); // salary always is increase (i think so)
	}
}
