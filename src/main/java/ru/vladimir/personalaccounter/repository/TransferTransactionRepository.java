package ru.vladimir.personalaccounter.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import ru.vladimir.personalaccounter.entity.TransferTransaction;

public interface TransferTransactionRepository extends JpaRepository<TransferTransaction, Long> {

}
