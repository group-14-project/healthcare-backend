package com.example.server.hospital;

import lombok.Getter;

@Getter
public enum Role {
    PATIENT("Patient"),
    ADMIN("Admin"),
    DOCTOR("Doctor"),
    SENIOR_DOCTOR("SeniorDoctor");

    private final String value;

    Role(String value) {
        this.value = value;
    }

}