package ru.vladimir.personalaccounter.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import ru.vladimir.personalaccounter.entity.AppUser;
import ru.vladimir.personalaccounter.entity.DebtTransaction;

public interface DebtTransactionRepository extends JpaRepository<DebtTransaction, Long> {
	
	@Query("Select d from DebtTransaction d where d.appUser=:appUser")
	List<DebtTransaction> findAll(@Param("appUser") AppUser theAppUser);
	
	@Query("Select d from DebtTransaction d where d.appUser=:appUser and d.id=:id")
	Optional<DebtTransaction> findByIdAndAppUser(@Param("appUser") AppUser appUser, @Param("id") long id);
	
	
}
