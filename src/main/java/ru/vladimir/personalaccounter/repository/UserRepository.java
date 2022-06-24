package ru.vladimir.personalaccounter.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import ru.vladimir.personalaccounter.entity.AppUser;

public interface UserRepository extends JpaRepository<AppUser, Long> {
	
    public AppUser findByUsername(String username);

    public AppUser findByEmail(String email);
}
