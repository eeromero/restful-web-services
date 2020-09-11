package com.rest.webservices.flights.services;

import com.rest.webservices.flights.model.Timetable;
import com.rest.webservices.flights.integration.RyanairApiService;
import com.rest.webservices.flights.model.Flight;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ScheduleFlightsService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private RyanairApiService ryanairApiService;

    /**
     *  Return all the flights scheduled in a month from departure to arrival
     * @param departure origin airport
     * @param arrival destination airport
     * @param yearMonth month to seacrh flights
     * @return a timetable of all the flights scheduled in that month
     */
    public Optional<Timetable> getScheduledFlightsByMonth(String departure, String arrival, YearMonth yearMonth){
        return ryanairApiService
                .getSchedules(departure, arrival, yearMonth.getYear(), yearMonth.getMonthValue())
                .map(
                        schedules -> schedules
                                .getDays()
                                .parallelStream()
                                .flatMap(
                                        day -> {
                                            LocalDate date = LocalDate
                                                    .of(yearMonth.getYear(), yearMonth.getMonthValue(), day.getDay());
                                            return day
                                                    .getFlights()
                                                    .stream()
                                                    .map(f ->
                                                            new Flight(
                                                                    f.getNumber(), departure, arrival,
                                                                    LocalDateTime.of(date, f.getDepartureTime()),
                                                                    f.getDepartureTime().isBefore(f.getArrivalTime()) ?
                                                                            LocalDateTime.of(date, f.getArrivalTime()) :
                                                                            LocalDateTime.of(date.plusDays(1), f.getArrivalTime())
                                                            )
                                                    );
                                        }
                                )
                                .collect(Collectors.toList()))
                .map(flights -> new Timetable(departure, arrival, yearMonth, flights));
    }

    /**
     *  Get all the scheduled flights from departure to arrival between from and to dates
     * @param departure origin airport
     * @param arrival destination airport
     * @param months months between from and to dates
     * @param from departure date
     * @param to arrival date
     * @return list of flights
     */
    public List<Flight> getScheduledFlights(String departure, String arrival,
                                            List<YearMonth> months, LocalDateTime from, LocalDateTime to) {
        logger.debug("getScheduledFlights: " + departure + " -> " + arrival + " from " + from + " to " + to);
        return months
                .parallelStream()
                .map(month -> getScheduledFlightsByMonth(departure, arrival, month))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .flatMap(t -> t.getFlights().stream())
                .filter(f -> !f.getDepartureDateTime().isBefore(from) && !f.getArrivalDateTime().isAfter(to))
                .collect(Collectors.toList());
    }
}
