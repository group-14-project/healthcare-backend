package com.example.server.dto.request;

import lombok.Data;

@Data
public class ChangePassword {
    private String email;
    private String role;
    private String password;
    private String otp;
}
