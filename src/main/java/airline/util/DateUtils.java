package src.main.java.airline.util;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

public class DateUtils {
    public static long zonedDateTimeDifference(ZonedDateTime d1, ZonedDateTime d2, ChronoUnit unit) {
        return unit.between(d1, d2);
    }
}
