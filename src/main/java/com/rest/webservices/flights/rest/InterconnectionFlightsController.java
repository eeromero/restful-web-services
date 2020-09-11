package com.rest.webservices.flights.rest;

import com.rest.webservices.flights.model.Interconnection;
import com.rest.webservices.flights.services.InterconnectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.Pattern;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@Validated
public class InterconnectionFlightsController {

    private final int NUM_MAX_STOPS = 1;
    private final String VALIDATION_IATA_CODE_REGEX = "^[A-Za-z]{3}$";

    @Autowired
    private InterconnectionService interconnectionService;

    @GetMapping("/interconnections")
    public ResponseEntity<List<Interconnection>> interconnections(
            @RequestParam @Pattern(regexp = VALIDATION_IATA_CODE_REGEX, message="Invalid departure format") String departure,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime departureDateTime,
            @RequestParam @Pattern(regexp = VALIDATION_IATA_CODE_REGEX, message="Invalid arrival format") String arrival,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime arrivalDateTime)
            throws Exception {

        List<Interconnection> interconnectionFlights = interconnectionService.
                getFlights(departure.toUpperCase(), departureDateTime, arrival.toUpperCase(), arrivalDateTime, NUM_MAX_STOPS);
        return new ResponseEntity<List<Interconnection>>(interconnectionFlights,HttpStatus.OK);
    }

}
