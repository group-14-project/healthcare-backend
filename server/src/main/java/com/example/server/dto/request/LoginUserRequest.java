package com.example.server.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class LoginUserRequest {
    @JsonProperty("user")
    private User user;

    @Getter
    public static class User{
        @JsonProperty("password")
        private String password;

        @JsonProperty("email")
        private String email;
    }

}

