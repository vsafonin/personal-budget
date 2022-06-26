package ru.vladimir.personalaccounter.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import ru.vladimir.personalaccounter.entity.AppUser;
import ru.vladimir.personalaccounter.entity.TransferTransaction;

public interface TransferTransactionRepository extends JpaRepository<TransferTransaction, Long> {
	
	@Query("Select t From TransferTransaction t where t.appUser = :appUser and t.id = :id")
	Optional<TransferTransaction> findByAppUserAndId(@Param("appUser") AppUser appUser, @Param("id") long id);
	
	List<TransferTransaction> findAllByAppUserOrderByCreateTimeDesc(AppUser appUser);
}
