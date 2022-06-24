package ru.vladimir.personalaccounter.repository;


import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import ru.vladimir.personalaccounter.entity.AdjustmentTransaction;
import ru.vladimir.personalaccounter.entity.BankAccount;

public interface AdjustmentTransactionRepository extends JpaRepository<AdjustmentTransaction, Long> {
	
	@Query("Select u from AdjustmentTransaction u where u.bankAccount = :bankAccount")
	List<AdjustmentTransaction> getAll(@Param("bankAccount") BankAccount bankAccount);

}
