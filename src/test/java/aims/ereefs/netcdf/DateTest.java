package aims.ereefs.netcdf;

import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;

/**
 * Created by gcoleman on 14/11/2016.
 */
public class DateTest {
    @Test
    public void testUdUnits() {
//        final String s = "1990-01-01 00:00:00 +10";
        String s = "0.0 days since 1990-01-01 00:00:00 +10";

        s = s.substring(s.indexOf("since ")+6);
        final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("y-M-d H:m:s X");
        final ZonedDateTime d = dateTimeFormatter.parse(s, ZonedDateTime::from);

        System.out.println(d.getZone());


    }

}


