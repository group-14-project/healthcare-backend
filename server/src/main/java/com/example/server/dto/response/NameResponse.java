package com.example.server.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class NameResponse {
    private String firstName;
    private String lastName;
    private String email;

}
