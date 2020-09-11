package com.rest.webservices.flights.integration.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
@JsonIgnoreProperties
public class ScheduleResponse {

    private int month;
    private List<DayResponse> days;

    public ScheduleResponse() {
    }

    public ScheduleResponse(int month, List<DayResponse> days) {
        this.month = month;
        this.days = days;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public List<DayResponse> getDays() {
        return days;
    }

    public void setDays(List<DayResponse> days) {
        this.days = days;
    }

    @Override
    public String toString() {
        return "ScheduleResponse{" +
                "month='" + month + '\'' +
                ", days=" + days +
                '}';
    }
}
