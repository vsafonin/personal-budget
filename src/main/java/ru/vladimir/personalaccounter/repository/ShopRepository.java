/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package ru.vladimir.personalaccounter.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import ru.vladimir.personalaccounter.entity.Shop;

/**
 *
 * @author vladimir
 */
public interface ShopRepository extends JpaRepository<Shop, Long>{
    
    Optional<Shop> findByName(String name);
    
}
