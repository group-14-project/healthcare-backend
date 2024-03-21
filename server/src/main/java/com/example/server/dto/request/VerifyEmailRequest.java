package com.example.server.dto.request;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
@Getter
public class VerifyEmailRequest
{
    @JsonProperty("user")
    private User user;

    @Getter
    public static class User{
        @JsonProperty("otp")
        private String otp;

        @JsonProperty("email")
        private String email;
    }
}
