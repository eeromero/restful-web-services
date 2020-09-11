package com.rest.webservices.flights.services;

import com.rest.webservices.flights.integration.RyanairApiService;
import com.rest.webservices.flights.integration.response.DayResponse;
import com.rest.webservices.flights.integration.response.FlightResponse;
import com.rest.webservices.flights.integration.response.ScheduleResponse;
import com.rest.webservices.flights.model.Flight;
import com.rest.webservices.flights.model.Timetable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.*;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScheduleFlightsServiceTest {

    @Mock
    private RyanairApiService ryanairApiService;

    @InjectMocks
    private ScheduleFlightsService scheduleFlightsService;

    @Test
    void getScheduledFlightsByMonths_emptyScheduledFlights() {
        when(ryanairApiService.getSchedules(any(), anyString(), anyInt(), anyInt())).thenReturn(Optional.empty());
        YearMonth yearMonth = YearMonth.now();
        Optional<Timetable> timetable = scheduleFlightsService.getScheduledFlightsByMonth("MAD", "DUB", yearMonth);
        assertThat(timetable, is(Optional.empty()));
        verify(ryanairApiService).getSchedules("MAD", "DUB", yearMonth.getYear(), yearMonth.getMonthValue());
    }

    @Test
    void getScheduledFlightsByMonth() {
        YearMonth yearMonth = YearMonth.of(2018, 5);
        int day1 = 2;
        int day2 = 31;
        when(ryanairApiService.getSchedules(any(), anyString(), anyInt(), anyInt())).thenReturn(getScheduled(yearMonth, day1, day2));
        Optional<Timetable> timetable = scheduleFlightsService.getScheduledFlightsByMonth("MAD", "DUB", yearMonth);
        assertThat(timetable, not(Optional.empty()));
        Timetable t = timetable.get();
        assertThat(t.getDeparture(), is("MAD"));
        assertThat(t.getArrival(), is("DUB"));
        assertThat(t.getYearMonth(), is(yearMonth));
        assertThat(t.getFlights(), hasSize(4));
        assertThat(t.getFlights(), hasItems(
                allOf(
                        hasProperty("number", is("121")),
                        hasProperty("departureAirport", is("MAD")),
                        hasProperty("arrivalAirport", is("DUB")),
                        hasProperty("departureDateTime",
                                is(LocalDateTime.of(yearMonth.getYear(), yearMonth.getMonthValue(), day1, 3, 0))),
                        hasProperty("arrivalDateTime",
                                is(LocalDateTime.of(yearMonth.getYear(), yearMonth.getMonthValue(), day1, 6, 20)))
                ),
                allOf(
                        hasProperty("number", is("122")),
                        hasProperty("departureAirport", is("MAD")),
                        hasProperty("arrivalAirport", is("DUB")),
                        hasProperty("departureDateTime",
                                is(LocalDateTime.of(yearMonth.getYear(), yearMonth.getMonthValue(), day1, 21, 0))),
                        hasProperty("arrivalDateTime",
                                is(LocalDateTime.of(yearMonth.getYear(), yearMonth.getMonthValue(), day1 + 1, 00, 20)))
                ),
                allOf(
                        hasProperty("number", is("121")),
                        hasProperty("departureAirport", is("MAD")),
                        hasProperty("arrivalAirport", is("DUB")),
                        hasProperty("departureDateTime",
                                is(LocalDateTime.of(yearMonth.getYear(), yearMonth.getMonthValue(), day2, 3, 0))),
                        hasProperty("arrivalDateTime",
                                is(LocalDateTime.of(yearMonth.getYear(), yearMonth.getMonthValue(), day2, 6, 20)))
                ),
                allOf(
                        hasProperty("number", is("122")),
                        hasProperty("departureAirport", is("MAD")),
                        hasProperty("arrivalAirport", is("DUB")),
                        hasProperty("departureDateTime",
                                is(LocalDateTime.of(yearMonth.getYear(), yearMonth.getMonthValue(), day2, 21, 0))),
                        hasProperty("arrivalDateTime",
                                is(LocalDateTime.of(yearMonth.getYear(), yearMonth.plusMonths(1).getMonthValue(), 1, 0, 20)))
                )
        ));
        verify(ryanairApiService).getSchedules("MAD", "DUB", yearMonth.getYear(), yearMonth.getMonthValue());
    }

    private Optional<ScheduleResponse> getScheduled(YearMonth yearMonth, int day1, int day2) {
        FlightResponse f1 = new FlightResponse("FRA", "121", LocalTime.of(3, 00), LocalTime.of(6, 20));
        FlightResponse f2 = new FlightResponse("FRA", "122", LocalTime.of(21, 00), LocalTime.of(0, 20));
        DayResponse dayResponse1 = new DayResponse(day1, Arrays.asList(f1, f2));
        DayResponse dayResponse2 = new DayResponse(day2, Arrays.asList(f1, f2));
        return Optional.of(new ScheduleResponse(yearMonth.getMonthValue(), Arrays.asList(dayResponse1, dayResponse2)));
    }

    @Test
    void getScheduledFlights_noFlights() {
        YearMonth month1 = YearMonth.of(2019, 12);
        YearMonth month2 = YearMonth.of(2020, 01);

        List<YearMonth> months = Arrays.asList(month1, month2);
        LocalDateTime departureDate = LocalDateTime.of(2019, 12, 21, 3, 0);
        LocalDateTime arrivalDate = LocalDateTime.of(2020, 1, 05, 14, 35);

        //no flights for month1
        when(ryanairApiService.getSchedules("MAD", "DUB", month1.getYear(), month1.getMonthValue()))
                .thenReturn(Optional.empty());

        //return timetable with flights on days 7th an 8th for month2. These flights are after arrivalDate
        when(ryanairApiService.getSchedules("MAD", "DUB", month2.getYear(), month2.getMonthValue()))
                .thenReturn(getScheduled(month2, 7, 8));

        List<Flight> flights = scheduleFlightsService.getScheduledFlights("MAD", "DUB", months, departureDate, arrivalDate);
        assertThat(flights, is(empty()));

        verify(ryanairApiService).getSchedules("MAD", "DUB", month1.getYear(), month1.getMonthValue());
        verify(ryanairApiService).getSchedules("MAD", "DUB", month2.getYear(), month2.getMonthValue());
    }

    @Test
    void getScheduledFlights() {
        YearMonth month1 = YearMonth.of(2019, 12);
        YearMonth month2 = YearMonth.of(2020, 01);

        List<YearMonth> months = Arrays.asList(month1, month2);
        LocalDateTime departureDate = LocalDateTime.of(2019, 12, 21, 3, 0);
        LocalDateTime arrivalDate = LocalDateTime.of(2020, 1, 05, 14, 35);

        when(ryanairApiService.getSchedules("MAD", "DUB", month1.getYear(), month1.getMonthValue()))
                .thenReturn(getScheduled(month1, 2, 21));

        when(ryanairApiService.getSchedules("MAD", "DUB", month2.getYear(), month2.getMonthValue()))
                .thenReturn(getScheduled(month2, 3, 5));

        List<Flight> flights = scheduleFlightsService.getScheduledFlights("MAD", "DUB", months, departureDate, arrivalDate);
        assertThat(flights, hasSize(5));
        assertThat(flights, everyItem(hasProperty("departureDateTime", is(greaterThanOrEqualTo(departureDate)))));
        assertThat(flights, everyItem(hasProperty("arrivalDateTime", is(lessThanOrEqualTo(arrivalDate)))));
        assertThat(flights, hasItems(
                allOf(
                        hasProperty("number", is("121")),
                        hasProperty("departureAirport", is("MAD")),
                        hasProperty("arrivalAirport", is("DUB")),
                        hasProperty("departureDateTime",
                                is(LocalDateTime.of(month1.getYear(), month1.getMonthValue(), 21, 3, 0))),
                        hasProperty("arrivalDateTime",
                                is(LocalDateTime.of(month1.getYear(), month1.getMonthValue(), 21, 6, 20)))
                ),
                allOf(
                        hasProperty("number", is("122")),
                        hasProperty("departureAirport", is("MAD")),
                        hasProperty("arrivalAirport", is("DUB")),
                        hasProperty("departureDateTime",
                                is(LocalDateTime.of(month1.getYear(), month1.getMonthValue(), 21, 21, 0))),
                        hasProperty("arrivalDateTime",
                                is(LocalDateTime.of(month1.getYear(), month1.getMonthValue(), 21 + 1, 00, 20)))
                ),
                allOf(
                        hasProperty("number", is("121")),
                        hasProperty("departureAirport", is("MAD")),
                        hasProperty("arrivalAirport", is("DUB")),
                        hasProperty("departureDateTime",
                                is(LocalDateTime.of(month2.getYear(), month2.getMonthValue(), 3, 3, 0))),
                        hasProperty("arrivalDateTime",
                                is(LocalDateTime.of(month2.getYear(), month2.getMonthValue(), 3, 6, 20)))
                ),
                allOf(
                        hasProperty("number", is("122")),
                        hasProperty("departureAirport", is("MAD")),
                        hasProperty("arrivalAirport", is("DUB")),
                        hasProperty("departureDateTime",
                                is(LocalDateTime.of(month2.getYear(), month2.getMonthValue(), 3, 21, 0))),
                        hasProperty("arrivalDateTime",
                                is(LocalDateTime.of(month2.getYear(), month2.getMonthValue(), 4, 0, 20)))
                ),
                allOf(
                        hasProperty("number", is("121")),
                        hasProperty("departureAirport", is("MAD")),
                        hasProperty("arrivalAirport", is("DUB")),
                        hasProperty("departureDateTime",
                                is(LocalDateTime.of(month2.getYear(), month2.getMonthValue(), 5, 3, 0))),
                        hasProperty("arrivalDateTime",
                                is(LocalDateTime.of(month2.getYear(), month2.getMonthValue(), 5, 6, 20)))
                )
        ));
        verify(ryanairApiService).getSchedules("MAD", "DUB", month1.getYear(), month1.getMonthValue());
        verify(ryanairApiService).getSchedules("MAD", "DUB", month2.getYear(), month2.getMonthValue());
    }

}