package ru.vladimir.personalaccounter.repository;

import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation;

import ru.vladimir.personalaccounter.entity.Authorities;

public interface RoleRepository extends JpaRepositoryImplementation<Authorities, Long>{
        Authorities findByName(String name);
}
