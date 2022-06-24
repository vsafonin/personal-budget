package ru.vladimir.personalaccounter.repository;


import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import ru.vladimir.personalaccounter.entity.AdjustmentTransaction;
import ru.vladimir.personalaccounter.entity.AppUser;
import ru.vladimir.personalaccounter.entity.BankAccount;

public interface AdjustmentTransactionRepository extends JpaRepository<AdjustmentTransaction, Long> {
	
	@Query("Select u from AdjustmentTransaction u where u.bankAccount = :bankAccount")
	List<AdjustmentTransaction> getAll(@Param("bankAccount") BankAccount bankAccount);
	
	@Query("Select t from AdjustmentTransaction t where t.appUser = :appUser and t.id = :id")
	Optional<AdjustmentTransaction> findById(@Param("appUser") AppUser appUser, @Param("id") long id);

}
