package com.rest.webservices.flights.model;

import java.util.List;

public class Interconnection {

    private int stops;
    private List<Flight> legs;


    public Interconnection(int stops, List<Flight> legs) {
        this.stops = stops;
        this.legs = legs;
    }

    public int getStops() {
        return stops;
    }

    public void setStops(int stops) {
        this.stops = stops;
    }

    public List<Flight> getLegs() {
        return legs;
    }

    public void setLegs(List<Flight> legs) {
        this.legs = legs;
    }
}
