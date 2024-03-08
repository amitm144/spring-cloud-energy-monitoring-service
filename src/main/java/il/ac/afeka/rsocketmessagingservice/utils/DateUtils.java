package il.ac.afeka.rsocketmessagingservice.utils;

import java.time.LocalDate;

public class DateUtils {

    public static boolean isLastDayOfMonth() {
        LocalDate today = LocalDate.now();
        LocalDate lastDayOfMonth = today.withDayOfMonth(today.lengthOfMonth());

        return today.equals(lastDayOfMonth);
    }
}
