package ru.vladimir.personalaccounter.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import ru.vladimir.personalaccounter.entity.AppUser;
import ru.vladimir.personalaccounter.entity.DebtTransaction;

public interface DebtTransactionRepository extends JpaRepository<DebtTransaction, Long> {
	
	List<DebtTransaction> findAllByAppUserOrderByCreateTimeDesc(AppUser theAppUser);
	
	Optional<DebtTransaction> findByAppUserAndId(AppUser appUser,  long id);
	
	
}
