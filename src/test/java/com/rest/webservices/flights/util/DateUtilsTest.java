package com.rest.webservices.flights.util;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class DateUtilsTest {

    @Test
    void getMonthsBetween_noFound() {
        LocalDateTime from = LocalDateTime.of(2019, 5, 6, 12, 30);
        LocalDateTime to = LocalDateTime.of(2019, 8, 6, 12, 30);
        List<YearMonth> months = DateUtils.getMonthsBetween(to, from);
        assertThat(months, is(empty()));
    }

    @Test
    void getMonthsBetween_datesInTheSameMonth() {
        LocalDateTime from = LocalDateTime.of(2019, 5, 6, 12, 30);
        LocalDateTime to = LocalDateTime.of(2019, 5, 6, 12, 30);
        List<YearMonth> months = DateUtils.getMonthsBetween(from, to);
        assertThat(months, hasSize(1));
        assertThat(months, hasItem( allOf(
                hasProperty("year", is(2019)),
                hasProperty("monthValue", is(5))
                ))
        );
    }

    @Test
    void getMonthsBetween() {
        LocalDateTime from = LocalDateTime.of(2019, 5, 6, 12, 30);
        LocalDateTime to = LocalDateTime.of(2019, 8, 6, 12, 30);
        List<YearMonth> months = DateUtils.getMonthsBetween(from, to);
        assertThat(months, hasSize(4));
        assertThat(months, hasItems(
                allOf(
                        hasProperty("year", is(2019)),
                        hasProperty("monthValue", is(5))
                ),
                allOf(
                        hasProperty("year", is(2019)),
                        hasProperty("monthValue", is(6))
                ),
                allOf(
                        hasProperty("year", is(2019)),
                        hasProperty("monthValue", is(7))
                ),
                allOf(
                        hasProperty("year", is(2019)),
                        hasProperty("monthValue", is(8))
                ))
        );
    }
}