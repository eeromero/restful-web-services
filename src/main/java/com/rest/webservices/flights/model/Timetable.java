package com.rest.webservices.flights.model;

import java.time.YearMonth;
import java.util.List;

public class Timetable {
    private String departure;
    private String arrival;
    private YearMonth yearMonth;
    private List<Flight> flights;

    public Timetable(String departure, String arrival, YearMonth yearMonth, List<Flight> flights) {
        this.departure = departure;
        this.arrival = arrival;
        this.yearMonth = yearMonth;
        this.flights = flights;
    }

    public String getDeparture() {
        return departure;
    }

    public void setDeparture(String departure) {
        this.departure = departure;
    }

    public String getArrival() {
        return arrival;
    }

    public void setArrival(String arrival) {
        this.arrival = arrival;
    }

    public YearMonth getYearMonth() {
        return yearMonth;
    }

    public void setYearMonth(YearMonth yearMonth) {
        this.yearMonth = yearMonth;
    }

    public List<Flight> getFlights() {
        return flights;
    }

    public void setFlights(List<Flight> flights) {
        this.flights = flights;
    }
}

