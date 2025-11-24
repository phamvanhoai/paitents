package com.example.laboratorymanagement.user.repository;

import com.example.laboratorymanagement.common.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PatientRepository extends JpaRepository<Patient, Long> {
    @Query("SELECT p FROM Patient p " +
            "WHERE (:keyword IS NULL OR LOWER(p.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:isDeleted IS NULL OR p.isDeleted = :isDeleted) " +
            "ORDER BY p.patientId ASC")
    Page<Patient> searchPatients(@Param("keyword") String keyword,
                                 @Param("isDeleted") Boolean isDeleted,
                                 Pageable pageable);
}