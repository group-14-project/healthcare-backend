package com.example.server.dto.response;

import com.example.server.patient.PatientEntity;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
public class PatientResponse {
        @JsonProperty("email")
        private final String email;

        @JsonProperty("firstName")
        private final String firstName;

        @JsonProperty("lastName")
        private String lastName;

        @JsonProperty("height")
        private String height;

        @JsonProperty("weight")
        private String weight;

        @JsonProperty("bloodGroup")
        private String bloodGroup;

        @JsonProperty("gender")
        private String gender;

        @JsonProperty("firstTimeLogin")
        private boolean firstTimeLogin;

        @JsonProperty("pastAppointments")
        private List<AppointmentDetailsDto> pastAppointmentDetails;

        @JsonProperty("futureAppointments")
        private List<AppointmentDetailsDto> futureAppointmentDetails;



//    public static PatientResponse fromPatientEntity(PatientEntity patientEntity, String token){
//        return new PatientResponse(
//                new Patient(
//                        patientEntity.getEmail(),
//                        token,
//                        patientEntity.getFirstName()
//                )
//        );
//    }
}
