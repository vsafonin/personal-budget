package ru.vladimir.personalaccounter.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import ru.vladimir.personalaccounter.entity.AppUser;
import ru.vladimir.personalaccounter.entity.SalaryTransaction;

public interface SalaryTransactionRepository
		extends JpaRepository<ru.vladimir.personalaccounter.entity.SalaryTransaction, Long> {

	List<SalaryTransaction> findAllByAppUserOrderByCreateTimeDesc(AppUser appUser);
	
	Optional<SalaryTransaction> findByAppUserAndId(AppUser appUser, long id);
}
