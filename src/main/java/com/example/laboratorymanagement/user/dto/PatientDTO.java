package com.example.laboratorymanagement.user.dto;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PatientDTO {
    private Long patientId;
    private String patientCode;
    private String fullName;
    private LocalDate dateOfBirth;
    private String gender;
    private String address;
    private String phoneNumber;
    private String email;
    private String identityNumber;
    private String emergencyContactName;
    private String emergencyContactPhone;
    private boolean createAccount;
}
