package com.rest.webservices.flights.model;

import java.util.List;

public class TreeRoute {
    private Flight flight;
    private List<TreeRoute> connections;

    public TreeRoute(Flight flight, List<TreeRoute> connections) {
        this.flight = flight;
        this.connections = connections;
    }

    public Flight getFlight() {
        return flight;
    }

    public void setFlight(Flight flight) {
        this.flight = flight;
    }

    public List<TreeRoute> getConnections() {
        return connections;
    }

    public void setConnections(List<TreeRoute> connections) {
        this.connections = connections;
    }

    @Override
    public String toString() {
        return "Node{" +
                "flight=" + flight +
                ", connections=" + connections +
                '}';
    }
}