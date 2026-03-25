package airline.controllers;

import airline.repository.MockFlightRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FlightControllerTests {
    private MockFlightRepository mfr;

    @BeforeEach
    void setup() {
        mfr = new MockFlightRepository();
    }

    @AfterEach
    void cleanup() {
        mfr = null;
    }

    @Test
    @DisplayName("Halló")
    void test() {
        assertEquals(1, 0, "1 er ekki sama og 0");
    }
}
