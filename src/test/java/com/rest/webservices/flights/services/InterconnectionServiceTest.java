package com.rest.webservices.flights.services;

import com.rest.webservices.flights.exception.InvalidInputException;
import com.rest.webservices.flights.model.Flight;
import com.rest.webservices.flights.model.Interconnection;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class InterconnectionServiceTest {

    @Mock
    private ScheduleFlightsService scheduleFlightsService;

    @Mock
    private RouteService routeService;

    @InjectMocks
    @Spy
    private InterconnectionService interconnectionService;

    private static LocalDateTime departureDate;
    private static LocalDateTime arrivalDate;

    @BeforeAll
    static void beforeAll() {
        departureDate = LocalDateTime.of(2019, 5, 6, 12, 30);
        arrivalDate = LocalDateTime.of(2019, 5, 9, 12, 30);
    }


    @BeforeEach
    void setUp() {
    }

    @ParameterizedTest
    @MethodSource("getFlights_exception")
    void getFlights_exception(String testCase, String from, LocalDateTime departureDate, String to, LocalDateTime arrivalDate, int numStops) {
        InvalidInputException ex = assertThrows(InvalidInputException.class, () -> {
            interconnectionService
                    .getFlights(from, departureDate, to, arrivalDate, numStops);
        });
        assertThat(ex.getMessage(), is(testCase));
    }

    @ParameterizedTest
    @MethodSource("getFlights_noRoutesFoundParameters")
    void getFlights_noRoutesFound(String testCase, Map<String, List<String>> routes) throws Exception {
        when(routeService.getAllAvailableRoutes()).thenReturn(routes);
        List<Interconnection> interconnections = interconnectionService
                .getFlights("MAD", departureDate, "BCN", arrivalDate, 1);
        assertThat(interconnections, hasSize(2));
        assertThat(interconnections, hasItems(
                allOf(
                        hasProperty("stops", is(0)),
                        hasProperty("legs", is(empty()))
                ),
                allOf(
                        hasProperty("stops", is(1)),
                        hasProperty("legs", is(empty()))
                )
        ));

        verify(routeService).getAllAvailableRoutes();
        verify(scheduleFlightsService, never()).getScheduledFlights(any(), any(), any(), any(), any());
    }

    @Test
    void getFlights_onlyDirectFlights() throws Exception {
        LocalDateTime departureDate = LocalDateTime.of(2019, 5, 6, 14, 30);
        LocalDateTime arrivalDate = LocalDateTime.of(2019, 5, 6, 18, 30);

        when(routeService.getAllAvailableRoutes()).thenReturn(getRoutes());
        when(scheduleFlightsService.getScheduledFlights(any(), any(), any(), any(), any()))
                .thenReturn(
                        Arrays.asList(new
                                Flight("1",
                                "MAD",
                                "ALC",
                                departureDate,
                                arrivalDate
                        )));

        List<Interconnection> interconnections = interconnectionService
                .getFlights("MAD", departureDate, "ALC", departureDate.plusDays(2), 1);
        assertThat(interconnections, hasSize(2));
        assertThat(interconnections, hasItems(
                allOf(
                        hasProperty("stops", is(0)),
                        hasProperty("legs", hasSize(1)),
                        hasProperty("legs", hasItems(
                                allOf(hasProperty("departureAirport", is("MAD")),
                                        hasProperty("arrivalAirport", is("ALC")),
                                        hasProperty("departureDateTime", is(departureDate)),
                                        hasProperty("arrivalDateTime", is(arrivalDate)))
                                )
                        )),
                allOf(
                        hasProperty("stops", is(1)),
                        hasProperty("legs", is(empty()))
                )
        ));
        verify(routeService, times(1)).getAllAvailableRoutes();
        verify(routeService, times(1)).getRoutesWithConnections(getRoutes(), "MAD", "ALC", 1);
        verify(scheduleFlightsService, atMostOnce()).getScheduledFlights(any(), any(), any(), any(), any());
        verify(interconnectionService, never()).getAllConnectionsFlightsOfARoute(any(), any(), any(), any());
    }

    @Test
    void getFlights_onlyFlightsWithConnexion() throws Exception {
        Map routes = getRoutes();
        LocalDateTime MAD_BCN_DepartureDate = LocalDateTime.of(2019, 5, 6, 14, 30);
        LocalDateTime MAD_BCN_ArrivalDate = LocalDateTime.of(2019, 5, 6, 18, 30);

        LocalDateTime MAD_ALC_DepartureDate = LocalDateTime.of(2019, 5, 6, 14, 30);
        LocalDateTime MAD_ALC_ArrivalDate = LocalDateTime.of(2019, 5, 6, 18, 30);

        LocalDateTime ALC_TNF_DepartureDate1 = LocalDateTime.of(2019, 5, 6, 21, 30);
        LocalDateTime ALC_TNF_ArrivalDate1 = LocalDateTime.of(2019, 5, 7, 0, 30);

        LocalDateTime ALC_TNF_DepartureDate2 = LocalDateTime.of(2019, 5, 6, 22, 30);
        LocalDateTime ALC_TNF_ArrivalDate2 = LocalDateTime.of(2019, 5, 7, 1, 30);

        when(routeService.getAllAvailableRoutes()).thenReturn(routes);

        when(routeService.getRoutesWithConnections(routes, "MAD", "TNF", 1))
                .thenReturn(Arrays.asList(Arrays.asList("MAD", "BCN", "TNF"), Arrays.asList("MAD", "ALC", "TNF")));

        List<YearMonth> months = Arrays.asList(YearMonth.of(2019, 5));

        when(scheduleFlightsService.getScheduledFlights("MAD", "BCN", months,
                departureDate, arrivalDate))
                .thenReturn(
                        Arrays.asList(new Flight(
                                "1",
                                "MAD",
                                "BCN",
                                MAD_BCN_DepartureDate,
                                MAD_BCN_ArrivalDate
                        )));

        when(scheduleFlightsService.getScheduledFlights("BCN", "TNF", months,
                MAD_BCN_ArrivalDate.plusHours(2), arrivalDate))
                .thenReturn(Collections.EMPTY_LIST);

        when(scheduleFlightsService.getScheduledFlights("MAD", "ALC", months,
                departureDate, arrivalDate))
                .thenReturn(
                        Arrays.asList(new Flight(
                                "2", "MAD",
                                "ALC",
                                MAD_ALC_DepartureDate,
                                MAD_ALC_ArrivalDate
                        )));
        when(scheduleFlightsService.getScheduledFlights("ALC", "TNF", months,
                MAD_ALC_ArrivalDate.plusHours(2), arrivalDate))
                .thenReturn(
                        Arrays.asList(
                                new Flight(
                                        "2",
                                        "ALC",
                                        "TNF",
                                        ALC_TNF_DepartureDate1,
                                        ALC_TNF_ArrivalDate1
                                ),
                                new Flight(
                                        "2",
                                        "ALC",
                                        "TNF",
                                        ALC_TNF_DepartureDate2,
                                        ALC_TNF_ArrivalDate2
                                )
                        ));

        List<Interconnection> interconnections = interconnectionService
                .getFlights("MAD", departureDate, "TNF", arrivalDate, 1);

        assertThat(interconnections, hasSize(3));
        assertThat(interconnections, hasItems(
                allOf(
                        hasProperty("stops", is(0)),
                        hasProperty("legs", is(empty()))
                ),
                allOf(
                        hasProperty("stops", is(1)),
                        hasProperty("legs", hasSize(2)),
                        hasProperty("legs", hasItems(
                                allOf(hasProperty("departureAirport", is("MAD")),
                                        hasProperty("arrivalAirport", is("ALC")),
                                        hasProperty("departureDateTime", is(MAD_ALC_DepartureDate)),
                                        hasProperty("arrivalDateTime", is(MAD_ALC_ArrivalDate))),
                                allOf(hasProperty("departureAirport", is("ALC")),
                                        hasProperty("arrivalAirport", is("TNF")),
                                        hasProperty("departureDateTime", is(ALC_TNF_DepartureDate1)),
                                        hasProperty("arrivalDateTime", is(ALC_TNF_ArrivalDate1)))
                                )
                        )
                ),
                allOf(
                        hasProperty("stops", is(1)),
                        hasProperty("legs", hasSize(2)),
                        hasProperty("legs", hasItems(
                                allOf(hasProperty("departureAirport", is("MAD")),
                                        hasProperty("arrivalAirport", is("ALC")),
                                        hasProperty("departureDateTime", is(MAD_ALC_DepartureDate)),
                                        hasProperty("arrivalDateTime", is(MAD_ALC_ArrivalDate))),
                                allOf(hasProperty("departureAirport", is("ALC")),
                                        hasProperty("arrivalAirport", is("TNF")),
                                        hasProperty("departureDateTime", is(ALC_TNF_DepartureDate2)),
                                        hasProperty("arrivalDateTime", is(ALC_TNF_ArrivalDate2)))
                                )
                        )
                )
        ));
        verify(routeService, times(1)).getAllAvailableRoutes();
        verify(routeService, times(1)).getRoutesWithConnections(routes, "MAD", "TNF", 1);
        verify(scheduleFlightsService, times(4)).getScheduledFlights(any(), any(), any(), any(), any());
        verify(interconnectionService, times(2)).getAllConnectionsFlightsOfARoute(any(), any(), any(), any());

    }

    static Stream<Arguments> getFlights_noRoutesFoundParameters() {
        return Stream.of(
                Arguments.arguments("There is not routes", Collections.EMPTY_MAP),
                Arguments.arguments("There is not routes for MAD->BCN", getRoutes())
        );
    }

    private static Map<String, List<String>> getRoutes() {
        Map<String, List<String>> routes = new HashMap();
        routes.put("MAD", Arrays.asList("ALC", "DUB"));
        routes.put("DUB", Arrays.asList("TNF"));
        return routes;
    }

    static Stream<Arguments> getFlights_exception() {
        return Stream.of(
                Arguments.arguments("departure and arrival can not be the same", "MAD", departureDate, "MAD", arrivalDate, 0),
                Arguments.arguments("arrivalDateTime date can not be before departureDateTime", "MAD", departureDate, "BCN", departureDate, 0),
                Arguments.arguments("arrivalDateTime date can not be before departureDateTime", "MAD", departureDate, "BCN", departureDate.minusMinutes(1), 0),
                Arguments.arguments("num Stops can not negative", "MAD", departureDate, "BCN", arrivalDate, -1)
        );
    }
}