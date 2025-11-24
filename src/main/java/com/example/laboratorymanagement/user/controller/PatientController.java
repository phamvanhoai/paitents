package com.example.laboratorymanagement.user.controller;

import com.example.laboratorymanagement.user.dto.PatientDTO;
import com.example.laboratorymanagement.user.dto.PatientMedicalRecordDTO;
import com.example.laboratorymanagement.common.entity.Patient;
import com.example.laboratorymanagement.user.service.PatientService;
import com.example.laboratorymanagement.user.service.PatientMedicalRecordService;
import com.example.laboratorymanagement.user.utils.ResponseHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/patients")
@RequiredArgsConstructor
public class PatientController {

    private final PatientService patientService;
    private final PatientMedicalRecordService recordService;

    // ✅ Đổi path để KHÔNG trùng với searchPatients()
    @PreAuthorize("hasAuthority('READ_ONLY')")
    @GetMapping("/summary")
    public ResponseEntity<?> getAll() {
        return ResponseHandler.success();
    }

    // Add Patient
    @PostMapping
    public ResponseEntity<?> addPatient(@RequestBody PatientDTO patientDTO,
                                        @RequestHeader(value = "X-User", defaultValue = "system") String createdBy){
        Patient patient = patientService.addPatient(patientDTO, createdBy);
        return ResponseEntity.status(201).body(patient);
    }

    // Update Patient
    @PutMapping("/{id}")
    public ResponseEntity<?> updatePatient(@PathVariable Long id,
                                           @RequestBody PatientDTO patientDTO,
                                           @RequestHeader(value = "X-User", defaultValue = "system") String updatedBy){
        Patient updated = patientService.updatePatient(id, patientDTO, updatedBy);
        return ResponseEntity.ok(updated);
    }

    // Delete Patient
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePatient(@PathVariable Long id,
                                           @RequestHeader(value = "X-User", defaultValue = "system") String deletedBy){
        patientService.deletePatient(id, deletedBy);
        return ResponseEntity.ok("Patient ID: " + id + " deleted successfully");
    }

    // View All Patient (có phân trang, keyword, isDeleted) - dùng URL /api/patients/all
    @GetMapping("/all")
    public ResponseEntity<Map<String, Object>> getAllPatients(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean isDeleted) {

        Page<Patient> pagePatients = patientService.getAllPatients(keyword, isDeleted, page, size);

        Map<String, Object> response = new HashMap<>();
        response.put("patients", pagePatients.getContent());
        response.put("currentPage", pagePatients.getNumber());
        response.put("totalItems", pagePatients.getTotalElements());
        response.put("totalPages", pagePatients.getTotalPages());

        return ResponseEntity.ok(response);
    }

    // Search Patient for name - FE đang gọi GET /api/patients?keyword=&deleted=&page=&size=
    @GetMapping
    public ResponseEntity<?> searchPatients(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false, defaultValue = "false") Boolean deleted,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "5") int size) {

        var result = patientService.searchPatients(keyword, deleted, page, size);
        return ResponseEntity.ok(result);
    }

    // View Detail One Patient
    @GetMapping("/{id}")
    public ResponseEntity<?> getPatientById(@PathVariable Long id){
        return ResponseEntity.ok(patientService.getPatientById(id));
    }

    // Add Patient Medical Record
    @PostMapping("/{patientId}/records")
    public ResponseEntity<?> addMedicalRecord(@PathVariable Long patientId,
                                              @Validated @RequestBody PatientMedicalRecordDTO dto,
                                              Principal principal) {
        String username = principal != null ? principal.getName() : "anonymous";
        var saved = recordService.addRecord(patientId, dto, username);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // Update Patient Medical Record
    @PutMapping("/records/{recordId}")
    public ResponseEntity<?> updateMedicalRecord(
            @PathVariable Integer recordId,
            @Validated @RequestBody PatientMedicalRecordDTO dto,
            @RequestHeader(value = "X-User", defaultValue = "system") String updatedBy) {
        var updatedRecord = recordService.updateRecord(recordId, dto, updatedBy);
        return ResponseEntity.ok(updatedRecord);
    }

    // Delete Patient Medical Record
    @DeleteMapping("/records/{recordId}")
    public ResponseEntity<?> deleteMedicalRecord(
            @PathVariable Integer recordId,
            @RequestHeader(value = "X-User", defaultValue = "system") String deletedBy) {
        recordService.deleteRecord(recordId, deletedBy);
        return ResponseEntity.ok("Medical record ID: " + recordId + " deleted successfully.");
    }

    // View All Patient Medical Records
    @GetMapping("/{patientId}/records")
    public ResponseEntity<?> getMedicalRecords(
            @PathVariable Integer patientId,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PatientMedicalRecordDTO> records = recordService.getRecordsByPatient(patientId, search, pageable);

        Map<String, Object> response = new HashMap<>();
        response.put("records", records.getContent());
        response.put("currentPage", records.getNumber());
        response.put("totalItems", records.getTotalElements());
        response.put("totalPages", records.getTotalPages());

        return ResponseEntity.ok(response);
    }

    // View Detail Patient Medical Record
    @GetMapping("/{patientId}/records/{recordId}")
    public ResponseEntity<?> getMedicalRecordById(@PathVariable Integer recordId) {
        var record = recordService.getRecordById(recordId);
        return ResponseEntity.ok(record);
    }
}
