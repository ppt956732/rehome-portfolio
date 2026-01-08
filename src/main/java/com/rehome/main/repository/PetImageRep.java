/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */

package com.rehome.main.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rehome.main.entity.PetImage;

/**
 *
 * @author user
 */
public interface PetImageRep extends JpaRepository<PetImage, Long>{
    Optional<PetImage> findFirstByPetCase_IdOrderBySortOrderAsc(Long caseId);
}
