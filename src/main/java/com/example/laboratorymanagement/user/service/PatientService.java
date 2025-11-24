package com.example.laboratorymanagement.user.service;

import com.example.laboratorymanagement.user.dto.PatientDTO;
import com.example.laboratorymanagement.common.entity.Patient;
import com.example.laboratorymanagement.user.repository.PatientRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PatientService {
    private final PatientRepository patientRepository;
    private final RestTemplate restTemplate = new RestTemplate();
    private final AccessLogService logService;

    @Value("${iam.service.url:http://localhost:8081/api/users}")
    private String iamServiceUrl;

    // AC01 + AC02 - Add new Patient
    // If createAccount=true, auto create IAM user.
    @Transactional
    public Patient addPatient(PatientDTO dto, String createdBy){
        //1. Validate
        if (dto.getFullName() == null || dto.getFullName().isEmpty()) {
            throw new RuntimeException("Full name is required");
        }
        //3. Save to DB
        Patient patient = Patient.builder()
                .fullName(dto.getFullName())
                .dateOfBirth(dto.getDateOfBirth())
                .gender(dto.getGender())
                .address(dto.getAddress())
                .phoneNumber(dto.getPhoneNumber())
                .email(dto.getEmail())
                .identityNumber(dto.getIdentityNumber())
                .isActive(true)
                .isDeleted(false)
                .createdAt(LocalDateTime.now())
                .createdBy(createdBy)
                .build();
        Patient saved = patientRepository.save(patient);
        logService.logActive(createdBy, "CREATE_PATIENT", "Patient:" + saved.getPatientId(),
                "Created patient record with code " + saved.getPatientCode());

        //4. Create IAM Account (AC02)
        if (dto.isCreateAccount()) {
            try {
                restTemplate.postForObject(iamServiceUrl, dto, String.class); //Mock IAM
            } catch (Exception e) {
                System.err.println("IAM service error: " + e.getMessage());
            }
        }
        return saved;
    }

    // AC03 - Update existing Patient
    @Transactional
    public Patient updatePatient(Long id, PatientDTO dto, String updatedBy){
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Patient not found"));
        if (patient.isDeleted()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot update deleted patient");
        }
        patient.setFullName(dto.getFullName());
        patient.setDateOfBirth(dto.getDateOfBirth());
        patient.setGender(dto.getGender());
        patient.setAddress(dto.getAddress());
        patient.setPhoneNumber(dto.getPhoneNumber());
        patient.setEmail(dto.getEmail());
        patient.setIdentityNumber(dto.getIdentityNumber());
        patient.setEmergencyContactName(dto.getEmergencyContactName());
        patient.setEmergencyContactPhone(dto.getEmergencyContactPhone());
        patient.setUpdatedBy(updatedBy);
        patient.setUpdatedAt(LocalDateTime.now());
        Patient updated = patientRepository.save(patient);
        logService.logActive(updatedBy, "UPDATE_PATIENT", "Patient:" + id,
                "Updated patient info");
        return updated;
    }

    // AC04 - Soft delete Patient record
    @Transactional
    public void deletePatient(Long id, String deletedBy){
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Patient not found"));
        if (patient.isDeleted()) {
            throw new ResponseStatusException(HttpStatus.GONE, "Patient already deleted");
        }
        patient.setDeleted(true);
        patient.setUpdatedBy(deletedBy);
        patient.setUpdatedAt(LocalDateTime.now());
        patientRepository.save(patient);
        logService.logActive(deletedBy, "DELETE_PATIENT", "Patient:" + id,
                "Marked patient record as deleted");
    }

    // AC05 - View all patients (with filters)
    @Transactional
    public Page<Patient> getAllPatients(String name, Boolean deleted, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return patientRepository.searchPatients(name, deleted, pageable);
    }

    // AC06 - View one patient detail
    @Transactional
    public Patient getPatientById(Long id) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient not found"));
        if (patient.isDeleted()) {
            throw new ResponseStatusException(HttpStatus.GONE, "Patient record was deleted");
        }
        return patient;
    }

    public Page<PatientDTO> searchPatients(String keyword, Boolean isDeleted, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Patient> patientPage = patientRepository.searchPatients(keyword, isDeleted, pageable);
        return patientPage.map(this::toDTO);
    }

    private PatientDTO toDTO(Patient patient) {
        if (patient == null) return null;
        return PatientDTO.builder()
                .patientId(patient.getPatientId().longValue())
                .patientCode(patient.getPatientCode())
                .fullName(patient.getFullName())
                .dateOfBirth(patient.getDateOfBirth())
                .gender(patient.getGender())
                .address(patient.getAddress())
                .phoneNumber(patient.getPhoneNumber())
                .email(patient.getEmail())
                .identityNumber(patient.getIdentityNumber())
                .emergencyContactName(patient.getEmergencyContactName())
                .emergencyContactPhone(patient.getEmergencyContactPhone())
                .build();
    }
}
