package ru.vladimir.personalaccounter.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation;
import org.springframework.data.repository.query.Param;

import ru.vladimir.personalaccounter.entity.ActivationUuid;
import ru.vladimir.personalaccounter.entity.AppUser;

public interface ActivationUuidRepostitory extends JpaRepositoryImplementation<ActivationUuid, Long> {
	@Query("Select appUser From ActivationUuid where uuid=:uuid")
	AppUser findByUuid (@Param("uuid")String uuid);
}
