package airline.controllers;

import airline.model.Airplane;
import airline.model.Airport;
import airline.model.Flight;
import airline.model.FlightStatus;
import airline.repository.EmptyFlightRepositoryMock;
import airline.repository.MockFlightRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Test fixture for FlightController.
 * It verifies controller-level behavior using a mocked storage dependency.
 */
public class FlightControllerTests {
    private FlightController controller;
    private MockFlightRepository mockFlightRepository;
    private Flight f1;
    private Flight f2;
    private Flight f3;
    private Flight f4;
    private ZonedDateTime baseDate;

    // Creates a clean controller + mock setup and deterministic fixture data for each test.
    @BeforeEach
    void setup() {
        mockFlightRepository = new MockFlightRepository();
        controller = new FlightController(mockFlightRepository);

        Airport kef = new Airport("KEF", "Keflavik", "Reykjanesbaer", "Iceland");
        Airport lhr = new Airport("LHR", "Heathrow", "London", "UK");
        Airport cph = new Airport("CPH", "Kastrup", "Copenhagen", "Denmark");
        Airplane plane = new Airplane("B737", 180);

        ZoneId zone = ZoneId.of("UTC");
        baseDate = ZonedDateTime.of(2026, 4, 1, 0, 0, 0, 0, zone);

        f1 = new Flight("FI100", baseDate.plusHours(10), baseDate.plusHours(13), 100.0,
                FlightStatus.SCHEDULED, 180, lhr, kef, plane);
        f2 = new Flight("FI101", baseDate.plusDays(1).plusHours(10), baseDate.plusDays(1).plusHours(13), 110.0,
                FlightStatus.SCHEDULED, 180, lhr, kef, plane);
        f3 = new Flight("FI200", baseDate.plusHours(11), baseDate.plusHours(14), 120.0,
                FlightStatus.SCHEDULED, 180, cph, kef, plane);
        f4 = new Flight("FI201", baseDate.plusHours(9), baseDate.plusHours(12), 130.0,
                FlightStatus.SCHEDULED, 180, cph, kef, plane);

        mockFlightRepository.save(f1);
        mockFlightRepository.save(f2);
        mockFlightRepository.save(f3);
        mockFlightRepository.save(f4);
    }

    // Clears references after each test so state does not leak between tests.
    @AfterEach
    void cleanup() {
        controller = null;
        mockFlightRepository = null;
        f1 = null;
        f2 = null;
        f3 = null;
        f4 = null;
        baseDate = null;
    }

    // Success case: valid route/date should return matching flights.
    @Test
    @DisplayName("searchFlights returns matching flight for route and date")
    void searchFlights_returnsMatchingFlight_whenRouteAndDateMatch() {
        List<Flight> result = controller.searchFlights("KEF", "LHR", baseDate.plusHours(12));

        assertEquals(1, result.size());
        assertEquals("FI100", result.get(0).getFlightNumber());
    }

    // Unsuccessful search: valid input with no match should return an empty list.
    @Test
    @DisplayName("searchFlights returns empty list when there is no match")
    void searchFlights_returnsEmptyList_whenNoFlightsMatch() {
        List<Flight> result = controller.searchFlights("KEF", "LHR", baseDate.plusDays(2));

        assertEquals(0, result.size());
    }

    // Bonus behavior: with an empty-storage mock, search should still return an empty list.
    @Test
    @DisplayName("searchFlights returns empty list with empty repository mock")
    void searchFlights_returnsEmptyList_withEmptyRepositoryMock() {
        FlightController emptyController = new FlightController(new EmptyFlightRepositoryMock());
        List<Flight> result = emptyController.searchFlights("KEF", "LHR", baseDate.plusHours(12));

        assertEquals(0, result.size());
    }

    // Invalid input: null departure code should be rejected by controller validation.
    @Test
    @DisplayName("searchFlights throws for invalid input")
    void searchFlights_throwsIllegalArgumentException_whenInputIsInvalid() {
        assertThrows(IllegalArgumentException.class,
                () -> controller.searchFlights(null, "LHR", baseDate.plusHours(12)));
    }

    // Invalid input: blank arrival code should be rejected by controller validation.
    @Test
    @DisplayName("searchFlights throws when arrival code is blank")
    void searchFlights_throwsIllegalArgumentException_whenArrivalCodeIsBlank() {
        assertThrows(IllegalArgumentException.class,
                () -> controller.searchFlights("KEF", "   ", baseDate.plusHours(12)));
    }

    // Invalid input: null date should be rejected by controller validation.
    @Test
    @DisplayName("searchFlights throws when date is null")
    void searchFlights_throwsIllegalArgumentException_whenDateIsNull() {
        assertThrows(IllegalArgumentException.class,
                () -> controller.searchFlights("KEF", "LHR", null));
    }

    // Time-range filtering should include only flights strictly inside the interval.
    @Test
    @DisplayName("filterByDepartureTimeRange returns flights strictly inside range")
    void filterByDepartureTimeRange_returnsFlightsStrictlyInsideRange() {
        List<Flight> input = List.of(f1, f2, f3, f4);
        ZonedDateTime start = baseDate.plusHours(9);
        ZonedDateTime end = baseDate.plusHours(12);

        List<Flight> result = controller.filterByDepartureTimeRange(input, start, end);

        assertEquals(2, result.size());
        assertEquals("FI100", result.get(0).getFlightNumber());
        assertEquals("FI200", result.get(1).getFlightNumber());
    }

    // Boundary case: flights exactly at range start/end are excluded (strict comparison).
    @Test
    @DisplayName("filterByDepartureTimeRange excludes flights at start and end boundary")
    void filterByDepartureTimeRange_excludesFlightsAtStartAndEndBoundary() {
        List<Flight> input = List.of(f1, f3, f4);
        ZonedDateTime start = baseDate.plusHours(9);
        ZonedDateTime end = baseDate.plusHours(11);

        List<Flight> result = controller.filterByDepartureTimeRange(input, start, end);

        assertEquals(1, result.size());
        assertEquals("FI100", result.get(0).getFlightNumber());
    }

    // Invalid range: start after end should throw an exception.
    @Test
    @DisplayName("filterByDepartureTimeRange throws when start is after end")
    void filterByDepartureTimeRange_throwsIllegalArgumentException_whenStartAfterEnd() {
        List<Flight> input = List.of(f1, f3, f4);
        ZonedDateTime start = baseDate.plusHours(12);
        ZonedDateTime end = baseDate.plusHours(9);

        assertThrows(IllegalArgumentException.class,
                () -> controller.filterByDepartureTimeRange(input, start, end));
    }

    // Invalid input: null flight list should be rejected.
    @Test
    @DisplayName("filterByDepartureTimeRange throws when input list is null")
    void filterByDepartureTimeRange_throwsIllegalArgumentException_whenInputListIsNull() {
        assertThrows(IllegalArgumentException.class,
                () -> controller.filterByDepartureTimeRange(null, baseDate.plusHours(9), baseDate.plusHours(12)));
    }

    // Invalid input: null start time should be rejected.
    @Test
    @DisplayName("filterByDepartureTimeRange throws when start is null")
    void filterByDepartureTimeRange_throwsIllegalArgumentException_whenStartIsNull() {
        List<Flight> input = List.of(f1, f3, f4);

        assertThrows(IllegalArgumentException.class,
                () -> controller.filterByDepartureTimeRange(input, null, baseDate.plusHours(12)));
    }

    // Invalid input: null end time should be rejected.
    @Test
    @DisplayName("filterByDepartureTimeRange throws when end is null")
    void filterByDepartureTimeRange_throwsIllegalArgumentException_whenEndIsNull() {
        List<Flight> input = List.of(f1, f3, f4);

        assertThrows(IllegalArgumentException.class,
                () -> controller.filterByDepartureTimeRange(input, baseDate.plusHours(9), null));
    }

    // Edge case: equal start and end should return an empty result with strict bounds.
    @Test
    @DisplayName("filterByDepartureTimeRange returns empty list when start equals end")
    void filterByDepartureTimeRange_returnsEmptyList_whenStartEqualsEnd() {
        List<Flight> input = List.of(f1, f2, f3, f4);
        ZonedDateTime pointInTime = baseDate.plusHours(10);

        List<Flight> result = controller.filterByDepartureTimeRange(input, pointInTime, pointInTime);

        assertEquals(0, result.size());
    }
}

