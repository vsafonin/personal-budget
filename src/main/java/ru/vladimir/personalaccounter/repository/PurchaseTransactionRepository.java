/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package ru.vladimir.personalaccounter.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import ru.vladimir.personalaccounter.entity.AppUser;
import ru.vladimir.personalaccounter.entity.PurchaseTransaction;

/**
 *
 * @author vladimir
 */
public interface PurchaseTransactionRepository extends JpaRepository<PurchaseTransaction, Long> {
	
	List<PurchaseTransaction> findAllByAppUserOrderByCreateTimeDesc(AppUser appUser);

	Optional<PurchaseTransaction> findByFiscalSign(long fiscalSign);
    
}
