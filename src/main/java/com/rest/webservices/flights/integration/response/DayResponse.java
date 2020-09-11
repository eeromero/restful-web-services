package com.rest.webservices.flights.integration.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DayResponse {

    private int day;
    private List<FlightResponse> flights;

    public DayResponse(int day, List<FlightResponse> flights) {
        this.day = day;
        this.flights = flights;
    }

    public DayResponse() {
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public List<FlightResponse> getFlights() {
        return flights;
    }

    public void setFlights(List<FlightResponse> flights) {
        this.flights = flights;
    }

    @Override
    public String toString() {
        return "DayResponse{" +
                "day='" + day + '\'' +
                ", flights=" + flights +
                '}';
    }
}
