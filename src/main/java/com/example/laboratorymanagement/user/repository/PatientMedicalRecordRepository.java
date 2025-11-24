package com.example.laboratorymanagement.user.repository;

import com.example.laboratorymanagement.common.entity.PatientMedicalRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PatientMedicalRecordRepository extends JpaRepository<PatientMedicalRecord, Integer> {

    // 1. Lấy tất cả record theo patientId (không phân trang, không quan tâm xóa)
    List<PatientMedicalRecord> findByPatient_PatientId(Integer patientId);

    // 2. Lấy tất cả record theo patientId + isDeleted (không phân trang)
    List<PatientMedicalRecord> findByPatient_PatientIdAndIsDeleted(Integer patientId, Boolean isDeleted);

    // 3. Lấy record theo patientId + search recordType + phân trang
    Page<PatientMedicalRecord> findByPatient_PatientIdAndRecordTypeContainingIgnoreCaseAndIsDeletedOrderByLastTestDateDesc(
            Integer patientId, String recordType, Boolean isDeleted, Pageable pageable);

    // 4. Nếu search rỗng, dùng phân trang theo patientId + isDeleted + sắp xếp lastTestDate giảm dần
    Page<PatientMedicalRecord> findByPatient_PatientIdAndIsDeletedOrderByLastTestDateDesc(
            Integer patientId, Boolean isDeleted, Pageable pageable);
}
