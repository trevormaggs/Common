package common;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

/**
 * This utility class provides a method to convert a date string into a Date object. While it may
 * not be entirely infallible, every effort has been made to accurately interpret and process
 * the date string using internal logic.
 *
 * If the conversion fails, an exception of type {@code IllegalArgumentException} will be thrown.
 *
 * <p>
 * <b>Version History:</b>
 * </p>
 *
 * <ul>
 * <li>Trevor Maggs created on 9 June 2024</li>
 * <li>Trevor Maggs modified on 12 November 2025</li>
 * </ul>
 *
 * @author Trevor Maggs
 * @version 0.2
 * @since 12 November 2025
 */
public final class DateParser
{
    /**
     * Array of date separators used to split the input date string.
     */
    private static final String[] DATE_SEPARATORS = {"/", "-", ":", " "};

    /**
     * Array of time formats used to parse the input date string.
     */
    private static final String[] TIME_FORMATS = {"HH:mm:ss", "HH:mm"};

    /**
     * Map of date templates to regular expressions used to match the input date string.
     */
    private static final Map<String, String> MAP_TEMPLATE;

    static
    {
        MAP_TEMPLATE = new HashMap<String, String>()
        {
            {
                put("yyyy-MM-dd'T'HH:mm:ss", "\\d{4}-\\d{1,2}-\\d{1,2}T\\d{1,2}:\\d{1,2}:\\d{1,2}.*");
                put("yyyy[sep]MM[sep]dd", "\\d{4}[sep]\\d{1,2}[sep]\\d{1,2}.*");
                put("yyyy[sep]MMM[sep]dd", "\\d{4}[sep]\\w{3}[sep]\\d{1,2}.*");
                put("dd[sep]MM[sep]yyyy", "\\d{1,2}[sep]\\d{1,2}[sep]\\d{4}.*");
                put("dd[sep]MMM[sep]yyyy", "\\d{1,2}[sep]\\w{3}[sep]\\d{4}.*");
                put("dd[sep]MM[sep]yy", "\\d{1,2}[sep]\\w{1,2}[sep]\\d{2}.*");
                put("dd[sep]MMM[sep]yy", "\\d{1,2}[sep]\\w{3}[sep]\\d{2}.*");

                // For Indian Date formats
                put("MMM[sep]dd,[sep]yyyy", "\\w{3}[sep]\\d{1,2},[sep]\\d{4}.*");
            }
        };
    }

    /**
     * Returns the format pattern for the given date string.
     *
     * @param date
     *        the input date string
     * 
     * @return the format pattern for the date string, or null if the date string is invalid
     */
    private static String getFormatPattern(String date)
    {
        for (String sep : DATE_SEPARATORS)
        {
            for (Entry<String, String> entry : MAP_TEMPLATE.entrySet())
            {
                if (date.matches(getPatternBySeparator(entry.getValue(), sep)))
                {
                    return entry.getKey();
                }
            }
        }

        return null;
    }

    /**
     * Creates a regular expression pattern by replacing the template's separator with the given
     * separator.
     *
     * @param template
     *        the date template
     * @param sep
     *        the date separator
     *
     * @return the regular expression pattern with the specified separator
     */
    private static String getPatternBySeparator(String template, String sep)
    {
        /* Escape conflicting 'T' from the string, i.e.: 2011-10-07T22:59:20 */
        String regexSep = sep.equals("T") ? "T" : sep;

        return template.replace("[sep]", regexSep);
    }

    /**
     * Parses the input date string using the given pattern and returns a Date object.
     *
     * @param input
     *        the input date string
     * @param pattern
     *        the format pattern for the date string
     * @return the parsed Date object, or null if the date string is invalid
     */
    private static Date parseInput(String input, String pattern)
    {
        try
        {
            return new SimpleDateFormat(pattern, Locale.ENGLISH).parse(input);
        }

        catch (ParseException exc)
        {
            return null;
        }
    }

    /**
     * Converts the input date string to a Date object.
     *
     * @param input
     *        the input date string
     * 
     * @return the converted Date object
     * 
     * @throws NullPointerException
     *         if the input is null
     * @throws IllegalArgumentException
     *         if the date format is invalid
     */
    public static Date convertToDate(String input)
    {
        if (input != null)
        {
            String cleaned = input.trim();
            String dateFormat = getFormatPattern(cleaned);

            if (dateFormat != null)
            {
                Date date;

                for (String sep : DATE_SEPARATORS)
                {
                    String actualFormat = getPatternBySeparator(dateFormat, sep);

                    // Check for time formats
                    for (String time : TIME_FORMATS)
                    {
                        date = parseInput(cleaned, actualFormat + " " + time);

                        if (date != null)
                        {
                            return date;
                        }
                    }

                    // If time formats didn't work, get the date-time formats only
                    date = parseInput(cleaned, actualFormat);

                    if (date != null)
                    {
                        return date;
                    }
                }
            }

            throw new IllegalArgumentException("Date is an invalid format");
        }

        else
        {
            throw new NullPointerException("Date input is null");
        }
    }
}