package com.example.server.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class HospitalResponse
{
    @JsonProperty("hospitalName")
    private String hospitalName;

    @JsonProperty("email")
    private String email;

    @JsonProperty("address")
    private String address;

    @JsonProperty("city")
    private String city;

    @JsonProperty("firstTimeLogin")
    private boolean firstTimeLogin;

    @JsonProperty("doctors")
    private  List<DoctorDetailsResponse> doctors;

    @JsonProperty("reviews")
    private List<ViewReviewsResponse> reviewsResponses;

    @JsonProperty("eachDayCounts")
    private List<EachDayCount> eachDayCounts;

    @JsonProperty("specialization")
    private List<String> specialization;

    // No-argument constructor
    public HospitalResponse() {
    }
}
