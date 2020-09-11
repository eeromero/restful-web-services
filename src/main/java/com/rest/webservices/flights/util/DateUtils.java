package com.rest.webservices.flights.util;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.LinkedList;
import java.util.List;

public class DateUtils {

    public static List<YearMonth> getMonthsBetween(LocalDateTime start, LocalDateTime end) {
        YearMonth from = YearMonth.from(start);
        YearMonth to = YearMonth.from(end);
        List<YearMonth> months = new LinkedList<>();
        YearMonth yearMonth = from;
        while (!yearMonth.isAfter(to)) {
            months.add(yearMonth);
            yearMonth = yearMonth.plusMonths(1);
        }
        return months;
    }
}
