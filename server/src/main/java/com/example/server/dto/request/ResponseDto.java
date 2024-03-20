package com.example.server.dto.request;
import lombok.Data;
@Data
public class ResponseDto
{
    private Integer id;

    private String hospitalName;

    private String email;

    private boolean emailVerify;

    private String message;
}