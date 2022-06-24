package ru.vladimir.personalaccounter.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation;
import org.springframework.data.repository.query.Param;

import ru.vladimir.personalaccounter.entity.ActivationEmail;
import ru.vladimir.personalaccounter.entity.AppUser;

public interface ActivationEmailRepository extends JpaRepositoryImplementation<ActivationEmail, Long> {
		
	@Query("Select appUser From ActivationEmail where uuid=:uuid")
	AppUser findAppUserByUUID(@Param("uuid") String uuid);

	@Query("Select email From ActivationEmail where uuid=:uuid")
	String findEmailByUUID(@Param("uuid") String uuid);
	
	
}
