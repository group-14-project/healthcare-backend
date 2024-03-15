package com.example.server.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class MainSpecializationAddRequest {
    @JsonProperty("specialization")
    private MainSpecializationAddRequest.MainSpecialization mainSpecialization;

    @Getter
    public static class MainSpecialization{
        @JsonProperty("name")
        private String name;

        @JsonProperty("symptoms")
        private String symptoms;
    }
}
