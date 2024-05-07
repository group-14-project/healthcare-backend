package com.example.server.dto.response;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;

import java.io.IOException;

@Data
public class DoctorStatus {
    private Integer doctorId;
    private String email;
    private String status;
    private String firstName;
    private String lastName;
    private String degree;
    private String hospitalName;
    private String specialization;

//    public String toJsonString() throws IOException {
//        ObjectMapper mapper = new ObjectMapper();
//        return mapper.writeValueAsString(this);
//    }
}
