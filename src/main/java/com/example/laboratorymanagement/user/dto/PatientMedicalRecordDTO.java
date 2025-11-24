package com.example.laboratorymanagement.user.dto;


import jakarta.validation.constraints.NotBlank;
import lombok.*;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatientMedicalRecordDTO {
    private Long patientMedicalRecordId;

    @NotBlank(message = "recordType is required")
    private String recordType;
    private String medicalHistory;       // tiền sử bệnh lý
    private String currentMedications;   // thuốc đang dùng
    private String allergies;            // dị ứng
    private String clinicalNotes;        // ghi chú lâm sàng
    private LocalDate lastTestDate;      // ngày xét nghiệm gần nhất
    private Integer patientAgeAtRecord;  // tuổi bệnh nhân tại thời điểm lưu
    private String patientName;
}
