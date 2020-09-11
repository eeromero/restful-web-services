package com.rest.webservices.flights.rest;

import com.rest.webservices.flights.model.Flight;
import com.rest.webservices.flights.model.Interconnection;
import com.rest.webservices.flights.services.InterconnectionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;


import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

import static org.hamcrest.CoreMatchers.is;


@ContextConfiguration(classes = {InterconnectionFlightsController.class, InterconnectionService.class})
@WebMvcTest
class InterconnectionFlightsControllerTest {

    private static final String INTERCONNECTIONS_URI = "/interconnections?departure={departure}&arrival={arrival}&departureDateTime={departureDateTime}&arrivalDateTime={arrivalDateTime}";
    LocalDateTime departureDateTime = LocalDateTime.of(2019, 9, 9, 11, 20);
    LocalDateTime arrivalDateTime = LocalDateTime.of(2019, 9, 11, 17, 17);

    String departureDateTimeS = DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(departureDateTime);
    String arrivalDateTimeS = DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(arrivalDateTime);

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private InterconnectionService interconnectionService;

    @Test
    void getFlights_status_OK() throws Exception {

        given(interconnectionService.getFlights("WRO", departureDateTime, "VGO", arrivalDateTime, 1))
                .willReturn(
                        Arrays.asList(
                                new Interconnection(0, Arrays.asList(new Flight("1", "WRO", "VGO", departureDateTime, arrivalDateTime))),
                                new Interconnection(1,
                                        Arrays.asList(
                                                new Flight("1", "WRO", "LON", departureDateTime, departureDateTime.plusHours(2)),
                                                new Flight("1", "LON", "WRO", departureDateTime.plusDays(1), departureDateTime.plusDays(1).plusHours(2))
                                        ))

                        )
                );

        mockMvc.perform(get(INTERCONNECTIONS_URI, "WRO", "VGO", departureDateTimeS, arrivalDateTimeS))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()", is(2)))
                .andExpect(jsonPath("$[0].stops", is(0)))
                .andExpect(jsonPath("$[0].legs.length()", is(1)))
                .andExpect(jsonPath("$[0].legs[0].departureAirport", is("WRO")))
                .andExpect(jsonPath("$[0].legs[0].arrivalAirport", is("VGO")))
                .andExpect(jsonPath("$[0].legs[0].departureDateTime", is(departureDateTimeS)))
                .andExpect(jsonPath("$[0].legs[0].arrivalDateTime", is(arrivalDateTimeS)))
                .andExpect(jsonPath("$[1].stops", is(1)))
                .andExpect(jsonPath("$[1].legs.length()", is(2)))
        ;

    }

    @Test
    void getFlights_bad_request_required_departure() throws Exception {
        MvcResult result = mockMvc
                .perform(get( "/interconnections?arrival={arrival}&departureDateTime={departureDateTime}&arrivalDateTime={arrivalDateTime}", "WRO", "VGO", departureDateTimeS, arrivalDateTimeS))
                .andExpect(status().isBadRequest())
                .andReturn();
        assertThat(result.getResponse().getErrorMessage(), is("Required String parameter 'departure' is not present"));
    }


    @Test
    void getFlights_bad_request_required_arrival() throws Exception {
        MvcResult result = mockMvc
                .perform(get("/interconnections?departure={departure}&arriwval={arrival}&departureDateTime={departureDateTime}&arrivalDateTime={arrivalDateTime}","WRO", "VGO", departureDateTimeS, arrivalDateTimeS))
                .andExpect(status().isBadRequest())
                .andReturn();
        assertThat(result.getResponse().getErrorMessage(), is("Required String parameter 'arrival' is not present"));
    }


    @Test
    void getFlights_bad_request_required_departureDate() throws Exception {
        MvcResult result = mockMvc
                .perform(get("/interconnections?departure={departure}&arrival={arrival}&depar_tureDateTime={departureDateTime}&arrivalDateTime={arrivalDateTime}","WRO", "VGO", departureDateTimeS, arrivalDateTimeS))
                .andExpect(status().isBadRequest())
                .andReturn();
        assertThat(result.getResponse().getErrorMessage(), is("Required LocalDateTime parameter 'departureDateTime' is not present"));
    }


    @Test
    void getFlights_bad_request_required_arrivalDate() throws Exception {
        MvcResult result = mockMvc
                .perform(get("/interconnections?departure={departure}&arrival={arrival}&departureDateTime={departureDateTime}&arriv_alDateTime={arrivalDateTime}","WRO", "VGO", departureDateTimeS, arrivalDateTimeS))
                .andExpect(status().isBadRequest())
                .andReturn();
        assertThat(result.getResponse().getErrorMessage(), is("Required LocalDateTime parameter 'arrivalDateTime' is not present"));
    }
}