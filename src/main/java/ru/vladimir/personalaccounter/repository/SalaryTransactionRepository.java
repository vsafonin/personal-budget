package ru.vladimir.personalaccounter.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import ru.vladimir.personalaccounter.entity.AppUser;
import ru.vladimir.personalaccounter.entity.SalaryTransaction;

public interface SalaryTransactionRepository
		extends JpaRepository<ru.vladimir.personalaccounter.entity.SalaryTransaction, Long> {

	@Query("Select s from SalaryTransaction s Where s.appUser=:appUser")
	List<SalaryTransaction> getAllSalaryTransaction(@Param("appUser") AppUser appUser);
	
	@Query("Select s from SalaryTransaction s Where s.appUser=:appUser and s.id=:id" )
	Optional<SalaryTransaction> getSalaryTransactionByAppUserAndId(@Param("appUser") AppUser appUser, @Param("id") long id);
}
