package ru.vladimir.personalaccounter.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import ru.vladimir.personalaccounter.entity.AppUser;
import ru.vladimir.personalaccounter.entity.DebtTransaction;

public interface DebtTransactionRepository extends JpaRepository<DebtTransaction, Long> {
	
	@Query("Select c from DebtTransaction c where c.appUser=:appUser")
	List<DebtTransaction> findAll(@Param("appUser") AppUser theAppUser);
	
	
}
