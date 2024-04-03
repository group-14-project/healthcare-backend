package com.example.server.dto.response;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;

@Data
public class EachDayCount
{
    @JsonProperty("date")
    private LocalDate date;

    @JsonProperty("count")
    private Long count;

    public EachDayCount(LocalDate key, Long value) {
        this.date = key;
        this.count = value;
    }
}
