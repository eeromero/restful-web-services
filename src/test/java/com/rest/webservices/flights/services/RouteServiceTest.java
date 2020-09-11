package com.rest.webservices.flights.services;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;

import com.rest.webservices.flights.integration.RyanairApiService;
import com.rest.webservices.flights.integration.response.RouteResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.is;

import java.util.*;

@ExtendWith(MockitoExtension.class)
class RouteServiceTest {

    @Mock
    private RyanairApiService ryanairApiService;

    @InjectMocks
    private RouteService routeService;

    @Test
    void getAllAvailableRoutes_empty() {
        when(ryanairApiService.getRoutes()).thenReturn(Collections.EMPTY_LIST);
        assertThat(routeService.getAllAvailableRoutes(), is(anEmptyMap()));
    }

    @Test
    void getAllAvailableRoutes_oneRoute() {
        when(ryanairApiService.getRoutes())
                .thenReturn(Arrays.asList(getRouteMock("ALC", "BCN")));
        Map<String, List<String>> graph = routeService.getAllAvailableRoutes();
        assertThat(graph, aMapWithSize(2));
        assertThat(graph, hasEntry("ALC", Arrays.asList("BCN")));
        assertThat(graph, hasEntry("BCN", Collections.EMPTY_LIST));
    }

    @Test
    void getAllAvailableRoutes_repeatedRoutes() {
        when(ryanairApiService.getRoutes())
                .thenReturn(Arrays.asList(getRouteMock("ALC", "BCN"), getRouteMock("ALC", "BCN"), getRouteMock("ALC", "BCN")));
        Map<String, List<String>> graph = routeService.getAllAvailableRoutes();
        assertThat(graph, aMapWithSize(2));
        assertThat(graph, hasEntry("ALC", Arrays.asList("BCN")));
        assertThat(graph, hasEntry("BCN", Collections.EMPTY_LIST));
    }

    @Test
    void getAllAvailableRoutes_routesWithOperatorDifferentFromRyanair() {
        when(ryanairApiService.getRoutes())
                .thenReturn(Arrays.asList(getRouteMock("ALC", "BCN", "otroOperator", null)));
        assertThat(routeService.getAllAvailableRoutes(), is(anEmptyMap()));
    }

    @Test
    void getAllAvailableRoutes_routesWithConnectionAirport() {
        when(ryanairApiService.getRoutes())
                .thenReturn(Arrays.asList(getRouteMock("ALC", "BCN", "RYANAIR", "MAD")));
        assertThat(routeService.getAllAvailableRoutes(), is(anEmptyMap()));
    }


    @Test
    void getAllAvailableRoutes() {
        when(ryanairApiService.getRoutes())
                .thenReturn(Arrays.asList(
                        getRouteMock("ALC", "BCN"),
                        getRouteMock("ALC", "DUB"),
                        getRouteMock("ABC", "BCN"),
                        getRouteMock("LLO", "BCN"),
                        getRouteMock("BCN", "MAD"),
                        getRouteMock("MAD", "ALC")
                ));
        Map<String, List<String>> graph = routeService.getAllAvailableRoutes();

        assertThat(graph, aMapWithSize(6));
        assertThat(graph, hasEntry("ALC", Arrays.asList("BCN", "DUB")));
        assertThat(graph, hasEntry("ABC", Arrays.asList("BCN")));
        assertThat(graph, hasEntry("LLO", Arrays.asList("BCN")));
        assertThat(graph, hasEntry("BCN", Arrays.asList("MAD")));
        assertThat(graph, hasEntry("MAD", Arrays.asList("ALC")));
        assertThat(graph, hasEntry("DUB", Collections.EMPTY_LIST));
    }

    @Test
    void getRoutesWithConnections_noStops() {
        List<List<String>> routes = routeService.getRoutesWithConnections(getRoutesGraph(), "MAD", "DUB", 0);
        assertThat(routes, is(empty()));
    }

    @Test
    void getRoutesWithConnections_withOneStop() {
        List<List<String>> routes = routeService.getRoutesWithConnections(getRoutesGraph(), "MAD", "DUB", 1);
        assertThat(routes, hasSize(2));
        assertThat(routes, hasItem(Arrays.asList("MAD", "ALC", "DUB")));
        assertThat(routes, hasItem(Arrays.asList("MAD", "LLO", "DUB")));
    }

    @Test
    void getRoutesWithConnections_withTwoStop() {
        List<List<String>> routes = routeService.getRoutesWithConnections(getRoutesGraph(), "MAD", "DUB", 2);
        assertThat(routes, hasSize(2));
        assertThat(routes, hasItem(Arrays.asList("MAD", "ALC", "BCN","DUB")));
        assertThat(routes, hasItem(Arrays.asList("MAD", "LLO", "ALC", "DUB")));
    }

    private Map<String, List<String>> getRoutesGraph() {
        Map<String, List<String>> map = new HashMap<>();
        map.put("ALC", Arrays.asList("BCN", "DUB"));
        map.put("ABC", Arrays.asList("BCN"));
        map.put("LLO", Arrays.asList("ALC", "DUB"));
        map.put("BCN", Arrays.asList("MAD", "DUB"));
        map.put("MAD", Arrays.asList("ALC", "LLO"));
        map.put("DUB", Arrays.asList("MAD"));
        return map;
    }

    private RouteResponse getRouteMock(String from, String to, String operator, String connectingAirport) {
        RouteResponse r = new RouteResponse();
        r.setAirportFrom(from);
        r.setAirportTo(to);
        r.setOperator(operator);
        r.setConnectingAirport(connectingAirport);
        return r;
    }

    private RouteResponse getRouteMock(String from, String to) {
        return getRouteMock(from, to, "RYANAIR", null);
    }
}