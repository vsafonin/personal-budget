/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package ru.vladimir.personalaccounter.repository;

import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation;

import ru.vladimir.personalaccounter.entity.AppUser;

/**
 *
 * @author vladimir
 */
public interface UserRepository extends JpaRepositoryImplementation<AppUser, Long> {    

    public AppUser findByUsername(String username);

    public AppUser findByEmail(String email);
}
