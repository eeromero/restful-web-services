package com.rest.webservices.flights.integration;

import com.rest.webservices.flights.integration.response.RouteResponse;
import com.rest.webservices.flights.integration.response.ScheduleResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class RyanairApiService {

    private final String ROUTES_URL = "https://services-api.ryanair.com/locate/3/routes";
    private final String SCHEDULES_URL = "https://services-api.ryanair.com/timtbl/3/schedules/%s/%s/years/%s/months/%s";
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private RestTemplate restTemplate;

    @Cacheable(value = "routesCache")
    public List<RouteResponse> getRoutes() {
        logger.info("getRoutes: ");
        List<RouteResponse> routes = Collections.EMPTY_LIST;
        try {
            ResponseEntity<RouteResponse[]> response = restTemplate.getForEntity(ROUTES_URL, RouteResponse[].class);
            if (response.getStatusCode() == HttpStatus.OK) {
                routes = Arrays.stream(response.getBody()).collect(Collectors.toList());
            }
        } catch (Exception e) {
            logger.error(String.format("Something wrong happened retrieving routes: %s", e.getMessage()));
        }
        return routes;
    }

    @Cacheable(value = "scheduleCache")
    public Optional<ScheduleResponse> getSchedules(String departure, String arrival, Integer year, Integer month) {
        Optional<ScheduleResponse> schedules = Optional.empty();
        String url = String.format(SCHEDULES_URL, departure, arrival, year, month);
        logger.debug("getSchedules: " + url);

        try {
            ResponseEntity<ScheduleResponse> response = restTemplate.getForEntity(url, ScheduleResponse.class);
            if (response.getStatusCode() == HttpStatus.OK) {
                schedules = Optional.of(response.getBody());
            }
        } catch (Exception e) {
            logger.error(String.format("Something wrong happened retrieving schedules for %s: %s", url, e.getMessage()));
        }
        return schedules;
    }

    // This can be an option to increase the performance. This is not used
    @Async
    @Cacheable(value = "routesAsyncCache")
    public CompletableFuture<Optional<ScheduleResponse>> getSchedulesAsync(String departure, String arrival, int year, int month) {
        Optional<ScheduleResponse> response = getSchedules(departure, arrival, year, month);
        return CompletableFuture.completedFuture(response);
    }
}
