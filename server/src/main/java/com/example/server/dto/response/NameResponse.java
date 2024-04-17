package com.example.server.dto.response;

import lombok.Data;

@Data
public class NameResponse {
    private String firstName;
    private String lastName;
    private String email;
}
