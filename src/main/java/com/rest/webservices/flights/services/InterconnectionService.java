package com.rest.webservices.flights.services;

import com.rest.webservices.flights.model.Flight;
import com.rest.webservices.flights.model.Interconnection;
import com.rest.webservices.flights.model.TreeRoute;
import com.rest.webservices.flights.exception.InvalidInputException;
import com.rest.webservices.flights.util.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class InterconnectionService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final int DEPARTURE_STOP_POSITION = 0;
    private final int FIRST_STOP_POSITION = 1;
    private final int SECOND_STOP_POSITION = 2;
    private final int TIME_BETWEEN_STOPS_IN_HOURS = 2;

    @Autowired
    private ScheduleFlightsService scheduleFlightsService;

    @Autowired
    private RouteService routeService;


    /**
     * Get all the possible interconnections flights between 2 airports with a max of numMaxStops stops
     *
     * @param departure         airport of origin
     * @param departureDateTime all the flights will be no before than this date
     * @param arrival           airport of destination
     * @param arrivalDateTime   all the flights will be no after than this date
     * @param numMaxStops       stops between the 2 airports
     * @return a list of interconnection flights
     * @throws InvalidInputException
     */
    public List<Interconnection> getFlights(String departure, LocalDateTime departureDateTime, String arrival,
                                            LocalDateTime arrivalDateTime, int numMaxStops) throws Exception {

        if (!departureDateTime.isBefore(arrivalDateTime)) {
            throw new InvalidInputException("arrivalDateTime date can not be before departureDateTime");
        }

        if (departure.equals(arrival)) {
            throw new InvalidInputException("departure and arrival can not be the same");
        }

        if (numMaxStops < 0) {
            throw new InvalidInputException("num Stops can not negative");
        }

        return getInterconnections(routeService.getAllAvailableRoutes(),
                DateUtils.getMonthsBetween(departureDateTime, arrivalDateTime),
                departure, departureDateTime, arrival, arrivalDateTime, numMaxStops);
    }

    /**
     *
     * Get all the interconnections flights between 2 airports starting with direct flights and followed
     * with a list of lights according to numMaxStops
     * @param routesGraph all the routes available
     * @param yearMonths months where the flights need to be searched
     * @param departure airport of origin
     * @param departureDate
     * @param arrival airport of destination
     * @param arrivalDate
     * @param numMaxStops num max stops
     * @return List of interconnections
     */
    private List<Interconnection> getInterconnections(Map<String, List<String>> routesGraph, List<YearMonth> yearMonths,
                                                      String departure, LocalDateTime departureDate, String arrival,
                                                      LocalDateTime arrivalDate, int numMaxStops) {
        List<Interconnection> interconnections = new ArrayList<>();
        searchDirectFlights(interconnections, routesGraph, yearMonths, departure, departureDate, arrival, arrivalDate);
        searchFlightsWithConnections(interconnections, routesGraph, yearMonths, departure, departureDate, arrival, arrivalDate, numMaxStops);
        return interconnections;
    }


    private void searchDirectFlights(List<Interconnection> interconnections, Map<String, List<String>> routesGraph,
                                     List<YearMonth> yearMonths, String departure, LocalDateTime departureDate,
                                     String arrival, LocalDateTime arrivalDate) {
        Interconnection interconnection = new Interconnection(0, Collections.EMPTY_LIST);
        if (routesGraph.containsKey(departure) && routesGraph.get(departure).contains(arrival)) {
            scheduleFlightsService
                    .getScheduledFlights(departure, arrival, yearMonths, departureDate, arrivalDate)
                    .stream()
                    .map(flight -> new Interconnection(0, Arrays.asList(flight)))
                    .forEach(interconnections::add);
        } else {
            interconnections.add(interconnection);
        }
    }

    private void searchFlightsWithConnections(List<Interconnection> interconnections, Map<String, List<String>> routesGraph,
                                              List<YearMonth> yearMonths, String departure, LocalDateTime departureDate,
                                              String arrival, LocalDateTime arrivalDate, int numMaxStops) {
        for (int i = 1; i <= numMaxStops; i++) {
            int numStop = i;
            //Get all the routes between departure and arrival airports with i stops
            List<List<String>> routes = routeService.getRoutesWithConnections(routesGraph, departure, arrival, numStop);
            List<List<Flight>> allConnectionsFlights = new ArrayList<>();
            routes.stream()
                    .forEach(
                            route -> {
                                List<List<Flight>> connectionsFlights =
                                        getAllConnectionsFlightsOfARoute(route, yearMonths, departureDate, arrivalDate);
                                allConnectionsFlights.addAll(connectionsFlights);
                                connectionsFlights
                                        .stream()
                                        .map(flightsConnection -> new Interconnection(numStop, flightsConnection))
                                        .forEach(interconnections::add);
                            });
            if (allConnectionsFlights.isEmpty()) {
                interconnections.add(new Interconnection(numStop, Collections.EMPTY_LIST));
            }

        }
    }


    /**
     * Search all the flights available for the route(stops)
     * route: [MAD, BCN, TNF]
     * search all the flights from MAD->BCN and BCN->TNF between the dates departureDate and arrivalDate
     *
     * @param route         List of stops (Route)
     * @param yearMonths    list of the months to search
     * @param departureDate date to departure from origin
     * @param arrivalDate
     * @return
     */
    protected List<List<Flight>> getAllConnectionsFlightsOfARoute(List<String> route, List<YearMonth> yearMonths,
                                                                  LocalDateTime departureDate, LocalDateTime arrivalDate) {
        logger.info("getAllConnectionsFlightsOfARoute:" + route);
        // route: [DUB, MAD, VLC]
        //get flights from departure to the first stop in route: DUB -> MAD
        List<TreeRoute> flightsNode = scheduleFlightsService
                .getScheduledFlights(route.get(DEPARTURE_STOP_POSITION), route.get(FIRST_STOP_POSITION),
                        yearMonths, departureDate, arrivalDate)
                .stream()
                .map(flight -> new TreeRoute(flight, null))//flight connections are null for now
                .collect(Collectors.toList());

        //get flights to the next stops
        getAllConnectionsFlightsToTheNextStop(flightsNode, route, SECOND_STOP_POSITION, yearMonths, arrivalDate);
        List<List<Flight>> result = new ArrayList<>();
        //route.size() - 2 is the number of stops. route has [departure, stop1, stop2 arrival}
        convertToList(flightsNode, route.size() - 2, 0, new ArrayList<>(), result);
        return result;
    }

    /**
     * Finds flights from each arrival of flightsNode to the next stop specified by route and stopPosition
     * flightsNode[route1, route1] route1 and route2 are trees whose node root is the flight from departure to the next stop of route
     *
     * @param flightsNode  a list of TreeRoute (This structure represent all the routes available from 2 stops)
     * @param route        stops of the route
     * @param stopPosition position of the stop in @stops
     * @param yearMonths   months where the flights need to be searched
     * @param arrivalDate  flights have not to arrive after this date
     */
    private void getAllConnectionsFlightsToTheNextStop(List<TreeRoute> flightsNode, List<String> route, int stopPosition, List<YearMonth> yearMonths, LocalDateTime arrivalDate) {
        // Search flights between the arrival airport of each of the flights in flightsNode to the next stop route[stopPosition]
        if (stopPosition <= route.size() - 1) {
            Iterator<TreeRoute> previousFlightIterator = flightsNode.iterator();
            while (previousFlightIterator.hasNext()) {
                // for each flight we search the next connections flights
                TreeRoute previousFlightNode = previousFlightIterator.next();
                //list of flights from flightNode(previous flight) to the next stop specified by stops and stopPosition
                List<Flight> connectionFlights = scheduleFlightsService
                        .getScheduledFlights(
                                previousFlightNode.getFlight().getArrivalAirport(),
                                route.get(stopPosition), //current stop
                                yearMonths,
                                previousFlightNode.getFlight().getArrivalDateTime().plusHours(TIME_BETWEEN_STOPS_IN_HOURS), //next flight has to depart no less than 2 hours after arriving the previous flight
                                arrivalDate);
                if (connectionFlights.isEmpty()) {
                    //if there is not next connections, that route needs to be removed
                    previousFlightIterator.remove();
                } else {
                    //otherwise we save those connections and continue searching for more until the destination
                    previousFlightNode.setConnections(
                            connectionFlights
                                    .stream()
                                    .map(flight -> new TreeRoute(flight, null))
                                    .collect(Collectors.toList()));
                    getAllConnectionsFlightsToTheNextStop(previousFlightNode.getConnections(), route, stopPosition + 1, yearMonths, arrivalDate);
                }
            }
        }
    }

    /**
     * Convert flights tree into a list
     *
     * @param flightsNode flights represented as a tree
     * @param depth       depth of the tree
     * @param stop        level we are converting, starts from the node root 0
     * @param flights     flights of the route
     * @param result      list of routes
     */
    private void convertToList(List<TreeRoute> flightsNode, int depth, int stop, List<Flight> flights, List<List<Flight>> result) {
        if (flightsNode != null) {
            for (TreeRoute node : flightsNode) {
                List<Flight> newRoute = flights.stream().collect(Collectors.toList());
                newRoute.add(node.getFlight());
                if (depth == stop) {
                    result.add(newRoute);
                } else {
                    convertToList(node.getConnections(), depth, stop + 1, newRoute, result);
                }
            }
        }
    }

}
