package com.rest.webservices.flights.services;

import com.rest.webservices.flights.integration.RyanairApiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RouteService {

    private final String OPERATOR = "RYANAIR";
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private RyanairApiService ryanairApiService;

    public Map<String, List<String>> getAllAvailableRoutes() {
        Map<String, List<String>> graph = new HashMap<>();
        ryanairApiService.getRoutes()
                .stream()
                .filter(r -> r.getOperator().equals(OPERATOR) && r.getConnectingAirport() == null)
                .forEach(route -> {
                    graph.put(route.getAirportFrom(), graph.getOrDefault(route.getAirportFrom(), new ArrayList<>()));
                    graph.put(route.getAirportTo(), graph.getOrDefault(route.getAirportTo(), new ArrayList<>()));
                    if (!graph.get(route.getAirportFrom()).contains(route.getAirportTo())) {
                        graph.get(route.getAirportFrom()).add(route.getAirportTo());
                    }
                });
        return graph;
    }

    /**
     * Get all the routes between from and to with @totalStops
     * @param graph routes available
     * @param from  departure
     * @param to  destination
     * @param totalStops number of stops between from and to
     * @return a list of routes. Each route is a list of stops
     */
    public List<List<String>> getRoutesWithConnections(Map<String, List<String>> graph, String from, String to, int totalStops) {
        logger.info(String.format("getRoutesWithConnections: %s to %s", from, to));
        List<List<String>> routes = new ArrayList<>();
        getRoutes(graph, from, to, totalStops, 0, new ArrayList<>(), new ArrayList<>(), routes);
        return routes;
    }

    private void getRoutes(Map<String, List<String>> graph, String from, String to,
                           int totalStops, int stop, List<String> visitedStops, List<String> stops, List<List<String>> routes) {
        if (totalStops == stop) {
            if (graph.containsKey(from) && graph.get(from).contains(to)) {
                stops.add(from);
                stops.add(to);
                routes.add(stops);
            }
        } else {
            if (graph.containsKey(from) && !visitedStops.contains(from)) {
                stops.add(from);
                visitedStops.add(from);
                stop++;
                for (String stopInter : graph.get(from)) {
                    getRoutes(graph, stopInter, to, totalStops, stop, visitedStops, stops.stream().collect(Collectors.toList()), routes);
                }
            }
        }
    }
}
