package ru.vladimir.personalaccounter.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import ru.vladimir.personalaccounter.entity.AppUserJwtToken;

public interface AppUserJwtRepository extends JpaRepository<AppUserJwtToken, Long> {

}
