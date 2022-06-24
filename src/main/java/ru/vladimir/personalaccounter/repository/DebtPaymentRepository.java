package ru.vladimir.personalaccounter.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import ru.vladimir.personalaccounter.entity.DebtPayment;

public interface DebtPaymentRepository extends JpaRepository<DebtPayment, Long> {

}
