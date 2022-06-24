package ru.vladimir.personalaccounter.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import ru.vladimir.personalaccounter.entity.AppUser;
import ru.vladimir.personalaccounter.entity.BankAccount;

public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {
	
	@Query("Select u from BankAccount u where u.appUser = :appUser")
	List<BankAccount> getBankAccounts(@Param("appUser") AppUser appUser);
	
	@Query("Select u from BankAccount u where u.appUser = :appUser and u.id=:id" )
	Optional<BankAccount> findById(@Param("appUser") AppUser theAppUser, @Param("id") Long id);
}
