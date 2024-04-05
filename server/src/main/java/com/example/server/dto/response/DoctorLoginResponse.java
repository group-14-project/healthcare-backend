package com.example.server.dto.response;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class DoctorLoginResponse
{
    @JsonProperty("doctorId")
    private Integer doctorId;

    @JsonProperty("firstName")
    private String firstName;

    @JsonProperty("lastName")
    private String lastName;

    @JsonProperty("registrationId")
    private String registrationId;

    @JsonProperty("degree")
    private String degree;

    @JsonProperty("firstTimeLogin")
    private boolean firstTimeLogin;

    @JsonProperty("pastAppointments")
    private List<AppointmentDetailsDto> pastAppointmentDetails;

    @JsonProperty("futureAppointments")
    private List<AppointmentDetailsDto> futureAppointmentDetails;

    @JsonProperty("totalPatients")
    private Integer totalPatients;

    @JsonProperty("totalAppointments")
    private Integer totalAppointments;

    @JsonProperty("eachDayCounts")
    private List<EachDayCount> eachDayCounts;
}
