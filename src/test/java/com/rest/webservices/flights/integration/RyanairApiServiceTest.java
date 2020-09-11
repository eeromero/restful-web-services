package com.rest.webservices.flights.integration;

import com.rest.webservices.flights.integration.response.RouteResponse;
import com.rest.webservices.flights.integration.response.ScheduleResponse;
import com.rest.webservices.flights.services.RouteService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;

@RestClientTest(RyanairApiService.class)
class RyanairApiServiceTest {

    private final String ROUTES_URL = "https://services-api.ryanair.com/locate/3/routes";
    private final String SCHEDULES_URL = "https://services-api.ryanair.com/timtbl/3/schedules/%s/%s/years/%s/months/%s";

    private final String ROUTES_JSON = "[\n" +
            "    {\n" +
            "        \"airportFrom\": \"AAL\",\n" +
            "        \"airportTo\": \"STN\",\n" +
            "        \"connectingAirport\": null,\n" +
            "        \"newRoute\": false,\n" +
            "        \"seasonalRoute\": false,\n" +
            "        \"operator\": \"RYANAIR\",\n" +
            "        \"group\": \"CITY\",\n" +
            "        \"similarArrivalAirportCodes\": [],\n" +
            "        \"tags\": [],\n" +
            "        \"carrierCode\": \"FR\"\n" +
            "    },\n" +
            "    {\n" +
            "        \"airportFrom\": \"AAR\",\n" +
            "        \"airportTo\": \"GDN\",\n" +
            "        \"connectingAirport\": null,\n" +
            "        \"newRoute\": false,\n" +
            "        \"seasonalRoute\": false,\n" +
            "        \"operator\": \"RYANAIR\",\n" +
            "        \"group\": \"ETHNIC\",\n" +
            "        \"similarArrivalAirportCodes\": [],\n" +
            "        \"tags\": [],\n" +
            "        \"carrierCode\": \"FR\"\n" +
            "    }]";

    private final String SCHEDULE_JSON = "{\n" +
            "    \"month\": 9,\n" +
            "    \"days\": [\n" +
            "        {\n" +
            "            \"day\": 21,\n" +
            "            \"flights\": [\n" +
            "                {\n" +
            "                    \"carrierCode\": \"FR\",\n" +
            "                    \"number\": \"9456\",\n" +
            "                    \"departureTime\": \"16:10\",\n" +
            "                    \"arrivalTime\": \"19:50\"\n" +
            "                }\n" +
            "            ]\n" +
            "        },\n" +
            "        {\n" +
            "            \"day\": 23,\n" +
            "            \"flights\": [\n" +
            "                {\n" +
            "                    \"carrierCode\": \"FR\",\n" +
            "                    \"number\": \"9456\",\n" +
            "                    \"departureTime\": \"21:00\",\n" +
            "                    \"arrivalTime\": \"00:40\"\n" +
            "                }\n" +
            "            ]\n" +
            "        }]}";

    @Autowired
    private RyanairApiService ryanairApiService;

    @Autowired
    private  MockRestServiceServer server;


    @Test
    public void testRoutes() throws Exception {
        server.expect(requestTo(ROUTES_URL))
                .andRespond(withSuccess(ROUTES_JSON, MediaType.APPLICATION_JSON));

       List<RouteResponse> routeResponses= ryanairApiService.getRoutes();
       assertThat(routeResponses, hasSize(2));
       assertThat(routeResponses, hasItems(
               allOf(
                       hasProperty("airportFrom",is("AAL")),
                       hasProperty("airportTo",is("STN"))
               ),
               allOf(
                       hasProperty("airportFrom",is("AAR")),
                       hasProperty("airportTo",is("GDN"))
               )
       ));
    }

    @Test
    public void testSchedules() throws Exception {
        server.expect(requestTo(String.format(SCHEDULES_URL, "MAD","STN",2019,9)))
                .andRespond(withSuccess(SCHEDULE_JSON, MediaType.APPLICATION_JSON));

        Optional<ScheduleResponse> scheduleResponse = ryanairApiService.getSchedules("MAD","STN",2019,9);
        assertThat(scheduleResponse.isPresent(), is(Boolean.TRUE));
        ScheduleResponse schedule = scheduleResponse.get();
        assertThat(schedule, hasProperty("month",is(9)));
        assertThat(schedule.getDays(), hasSize(2));
        assertThat(schedule.getDays(), hasItems(
                allOf(
                        hasProperty("day",is(21)),
                        hasProperty("flights", hasSize(1))
                ),
                allOf(
                        hasProperty("day",is(23)),
                        hasProperty("flights",is(hasSize(1)))
                )
        ));
    }
}
