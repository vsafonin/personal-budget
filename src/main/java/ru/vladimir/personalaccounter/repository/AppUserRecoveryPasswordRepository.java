package ru.vladimir.personalaccounter.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import ru.vladimir.personalaccounter.entity.AppUserRecoveryPasswordModel;

public interface AppUserRecoveryPasswordRepository extends JpaRepository<AppUserRecoveryPasswordModel, Long> {

	Optional<AppUserRecoveryPasswordModel> findByToken(String token);

}
