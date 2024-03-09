package il.ac.afeka.energyservice.utils;

import org.springframework.cglib.core.Local;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Date;

public class DateUtils {
    private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS";
    private static final DateTimeFormatter format = new DateTimeFormatterBuilder()
            .appendPattern(DATE_FORMAT)
            .appendOffset("+HHMM", "Z")
            .toFormatter();

    public static boolean isValidDate(String inputDate) {
        DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
        dateFormat.setLenient(false); // set strict parsing

        try {
            // Parse the input date and Check if the parsed date is the same as the input date.
            // This helps to catch cases where the input date is valid, but contains additional information
            // (e.g., "31-12-2022 10:30:00" would be invalid in this case)
            Date date = dateFormat.parse(inputDate);
            return inputDate.equals(dateFormat.format(date));
        } catch (ParseException | NullPointerException e) {
            return false;
        }
    }

    public static boolean isValidDate(String inputDate, String dateFormatString) {
        DateFormat dateFormat = new SimpleDateFormat(dateFormatString);
        dateFormat.setLenient(false); // Strict parsing

        try {
            // Attempt to parse the date against the given format string
            Date date = dateFormat.parse(inputDate);
            return inputDate.equals(dateFormat.format(date));
        } catch (ParseException | NullPointerException e) {
            return false; // Date is invalid according to the format
        }
    }

    public static LocalDate parseDate(String date) {
        return LocalDate.parse(date, format);
    }

    public static LocalDate parseDate(String date, String dateFormatString) {
        return LocalDate.parse(date, DateTimeFormatter.ofPattern(dateFormatString));
    }

    public static String toValidDateString(LocalDate date) {
        return date.format(format);
    }

    public static boolean isLastDayOfMonth() {
        LocalDate today = LocalDate.now();
        LocalDate lastDayOfMonth = today.withDayOfMonth(today.lengthOfMonth());

        return today.equals(lastDayOfMonth);
    }
}
