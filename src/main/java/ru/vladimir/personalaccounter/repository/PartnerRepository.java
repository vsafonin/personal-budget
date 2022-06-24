package ru.vladimir.personalaccounter.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import ru.vladimir.personalaccounter.entity.AppUser;
import ru.vladimir.personalaccounter.entity.Partner;

public interface PartnerRepository extends JpaRepository<Partner, Long> {
	@Query("Select p from Partner p where p.appUser=:appUser and p.name=:name")
	Partner findByName(@Param("appUser") AppUser appUser,@Param("name")String name);

}
