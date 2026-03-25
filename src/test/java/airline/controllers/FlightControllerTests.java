package airline.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class FlightControllerTests {

    @BeforeEach
    void setup() {
        System.out.println("Byrja");
    }

    @Test
    @DisplayName("Halló")
    void test() {
        assertEquals(1, 0, "1 er ekki sama og 0");
    }
}
