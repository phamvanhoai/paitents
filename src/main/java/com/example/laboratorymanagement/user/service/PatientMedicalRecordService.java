package com.example.laboratorymanagement.user.service;

import com.example.laboratorymanagement.common.entity.AccessLog;
import com.example.laboratorymanagement.common.entity.Patient;
import com.example.laboratorymanagement.common.entity.PatientMedicalRecord;
import com.example.laboratorymanagement.user.dto.PatientMedicalRecordDTO;
import com.example.laboratorymanagement.user.repository.AccessLogRepository;
import com.example.laboratorymanagement.user.repository.PatientMedicalRecordRepository;
import com.example.laboratorymanagement.user.repository.PatientRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class PatientMedicalRecordService {

    private final PatientRepository patientRepository;
    private final PatientMedicalRecordRepository recordRepository;
    private final AccessLogRepository accessLogRepository;

    public PatientMedicalRecordService(PatientRepository patientRepository,
                                       PatientMedicalRecordRepository recordRepository,
                                       AccessLogRepository accessLogRepository) {
        this.patientRepository = patientRepository;
        this.recordRepository = recordRepository;
        this.accessLogRepository = accessLogRepository;
    }

    @Transactional
    public PatientMedicalRecord addRecord(Long patientId, PatientMedicalRecordDTO dto, String username) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient not found"));

        PatientMedicalRecord record = PatientMedicalRecord.builder()
                .patient(patient)
                .recordType(dto.getRecordType())
                .medicalHistory(dto.getMedicalHistory())
                .currentMedications(dto.getCurrentMedications())
                .allergies(dto.getAllergies())
                .clinicalNotes(dto.getClinicalNotes())
                .lastTestDate(dto.getLastTestDate())
                .patientAgeAtRecord(dto.getPatientAgeAtRecord())
                .recordDate(LocalDate.now())
                .versionNumber(1)
                .isCurrent(true)
                .isDeleted(false)
                .createdBy(username)
                .createdAt(LocalDateTime.now())
                .build();

        PatientMedicalRecord saved = recordRepository.save(record);

        // Audit log
        AccessLog log = AccessLog.builder()
                .username(username)
                .action("CREATE_RECORD")
                .target("Patient:" + patientId + " Record:" + saved.getRecordType())
                .detail("Created medical record")
                .timestamp(LocalDateTime.now())
                .build();
        accessLogRepository.save(log);

        return saved;
    }

    @Transactional
    public PatientMedicalRecord updateRecord(Integer recordId, PatientMedicalRecordDTO dto, String username) {
        PatientMedicalRecord existing  = recordRepository.findById(recordId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Record not found"));

        if (Boolean.TRUE.equals(existing.getPatient().isDeleted())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot update deleted record");
        }

        existing.setRecordType(dto.getRecordType());
        existing.setMedicalHistory(dto.getMedicalHistory());
        existing.setCurrentMedications(dto.getCurrentMedications());
        existing.setAllergies(dto.getAllergies());
        existing.setClinicalNotes(dto.getClinicalNotes());
        existing.setLastTestDate(dto.getLastTestDate());
        existing.setPatientAgeAtRecord(dto.getPatientAgeAtRecord());
        existing.setUpdatedBy(username);
        existing.setUpdatedAt(LocalDateTime.now());

        // bản ghi mới hoàn toàn
//        existing.setIsCurrent(false);
//        recordRepository.save(existing);

//        PatientMedicalRecord newRecord = PatientMedicalRecord.builder()
//                .patient(existing.getPatient())
//                .recordType(dto.getRecordType())
//                .medicalHistory(dto.getMedicalHistory())
//                .currentMedications(dto.getCurrentMedications())
//                .allergies(dto.getAllergies())
//                .clinicalNotes(dto.getClinicalNotes())
//                .lastTestDate(dto.getLastTestDate())
//                .patientAgeAtRecord(dto.getPatientAgeAtRecord())
//                .recordDate(LocalDate.now())
//                .versionNumber(existing.getVersionNumber() + 1)
//                .isCurrent(true)
//                .isDeleted(false)
//                .createdAt(LocalDateTime.now())
//                .createdBy(username)
//                .build();

        PatientMedicalRecord saved = recordRepository.save(existing);

        accessLogRepository.save(AccessLog.builder()
                .username(username)
                .action("UPDATE_MEDICAL_RECORD")
                .target("Record:" + recordId)
                .detail("Created new version:" + saved.getVersionNumber())
                .timestamp(LocalDateTime.now())
                .build());
        return saved;
    }

    @Transactional
    public void deleteRecord(Integer recordId, String username) {
        PatientMedicalRecord record = recordRepository.findById(recordId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Medical record not found"));
        if (Boolean.TRUE.equals(record.getIsDeleted())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Record already deleted");
        }

        record.setIsDeleted(true);
        record.setUpdatedBy(username);
        record.setUpdatedAt(LocalDateTime.now());
        recordRepository.save(record);

        accessLogRepository.save(AccessLog.builder()
                .username(username)
                .action("DELETE_MEDICAL_RECORD")
                .target("Record:" + recordId)
                .detail("Marked medical record as deleted")
                .timestamp(LocalDateTime.now())
                .build());
    }

    public List<PatientMedicalRecordDTO> getAllRecordByPatient(Integer patientId, Boolean includeDeleted) {
        List<PatientMedicalRecord> records = Boolean.TRUE.equals(includeDeleted)
                ? recordRepository.findByPatient_PatientId(patientId)
                : recordRepository.findByPatient_PatientIdAndIsDeleted(patientId, false);

        return records.stream()
                .map(this::mapToDTO)
                .toList();
    }

    public PatientMedicalRecord getRecordById(Integer recordId) {
        PatientMedicalRecord record = recordRepository.findById(recordId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Medical record not found"));

        if (Boolean.TRUE.equals(record.getIsDeleted())) {
            throw new ResponseStatusException(HttpStatus.GONE, "Medical record was deleted");
        }

        return record;
    }

    public Page<PatientMedicalRecordDTO> getRecordsByPatient(
            Integer patientId, String search, Pageable pageable) {

        Page<PatientMedicalRecord> page;

        if (search == null || search.isBlank()) {
            // Lấy records chưa xóa, sắp xếp theo lastTestDate giảm dần
            page = recordRepository.findByPatient_PatientIdAndIsDeletedOrderByLastTestDateDesc(
                    patientId, false, pageable);
        } else {
            // Lọc theo recordType, chưa xóa, sắp xếp theo lastTestDate giảm dần
            page = recordRepository.findByPatient_PatientIdAndRecordTypeContainingIgnoreCaseAndIsDeletedOrderByLastTestDateDesc(
                    patientId, search, false, pageable);
        }

        return page.map(this::mapToDTO);
    }

    private PatientMedicalRecordDTO mapToDTO(PatientMedicalRecord record) {
        return PatientMedicalRecordDTO.builder()
                .patientMedicalRecordId(Long.valueOf(record.getRecordId()))
                .recordType(record.getRecordType())
                .medicalHistory(record.getMedicalHistory())
                .currentMedications(record.getCurrentMedications())
                .allergies(record.getAllergies())
                .clinicalNotes(record.getClinicalNotes())
                .lastTestDate(record.getLastTestDate())
                .patientAgeAtRecord(record.getPatientAgeAtRecord())
                .patientName(record.getPatient().getFullName())
                .build();
    }

}