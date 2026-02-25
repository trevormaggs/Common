package common;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * This static only class is designed to provide a number of useful generic utilities.
 * </p>
 * 
 * <p>
 * <b>Note</b>, all methods provided in this class are accessed in a static way and the class itself
 * cannot be instantiated.
 * </p>
 * 
 * @author Created by Trevor Maggs on 29 April 2016
 * @version 0.2
 * @since 17 June 2016
 */
public final class Generic
{
    /**
     * Private constructor to prevent this static-only class from being instantiated.
     */
    private Generic()
    {
    }

    /**
     * Checks whether the specified input is expressed as a valid number.
     * 
     * @param subject
     *        string to be checked
     * 
     * @return boolean true if the input denotes a number
     */
    public static boolean isNumeric(final String subject)
    {
        /*
         * for (char c : str.toCharArray())
         * {
         * if (!Character.isDigit(c)) return false;
         * }
         * 
         * return true;
         */
        return subject.matches("-?\\d+(\\.\\d+)?");
    }

    /**
     * Converts from the specified input indicating a possible date to a standard Australian date
     * format in the string form of DD/MM/YYYY.
     * 
     * @param subject
     *        Date in the form of either dd-mm-yyyy, dd/mm/yyyy, dd\mm\yyyy or dd mm yyyy
     * 
     * @return formatted string in the form of DD/MM/YYYY
     */
    public static String getRequiredDateFormat(final String subject)
    {
        return subject.replaceAll("(\\d+)[/|-|\\\\|\\s*](\\d+)[/|-|\\\\|\\s*](\\d+)", "$1/$2/$3");
    }

    /**
     * Generates a line of padded characters to n of times.
     * 
     * @param subject
     *        string to be padded
     * @param n
     *        number of times to pad in integer form
     * 
     * @return formatted string
     */
    public static String repeatPrint(String subject, int n)
    {
        if (n > 0)
        {
            subject = subject + repeatPrint(subject, n - 1);
        }

        return subject;
    }

    /**
     * Capitalises the first letter of the specified word input.
     * 
     * @param subject
     *        the word to be capitalised
     * 
     * @return updated word
     */
    public static String capitalize(final String subject)
    {
        return Character.toUpperCase(subject.charAt(0)) + subject.substring(1);
    }

    /**
     * Capitalises the first character of each word located in the specified sentence.
     * 
     * @param subject
     *        the sentence to be capitalised
     * 
     * @return updated sentence
     */
    public static String makeTitleCase(final String subject)
    {
        String words[] = subject.split("\\s");
        StringBuffer sb = new StringBuffer();

        for (int i = 0; i < words.length; i++)
        {
            sb.append(Character.toUpperCase(words[i].charAt(0))).append(words[i].substring(1)).append(" ");
        }

        return sb.toString().trim();
    }

    /**
     * Encodes the specified byte array to a string of hexadecimal digits separated by a whitespace
     * between them.
     * 
     * @param bytes
     *        array of bytes
     * 
     * @return hexadecimal digits in string form
     */
    public static String encodeHexString(byte[] bytes)
    {
        return encodeHexString(bytes, true);
    }

    /**
     * Encodes the specified byte array to a string of corresponding hexadecimal digits.
     * 
     * @param bytes
     *        array of bytes
     * @param space
     *        boolean true to insert one white space between hexadecimal digits, else no space is
     *        inserted
     * 
     * @return hexadecimal digits in upper-case string form
     */
    public static String encodeHexString(byte[] bytes, boolean space)
    {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < bytes.length; i++)
        {
            if (space && i > 0)
            {
                sb.append(" ");
            }

            sb.append(String.format("%02X", bytes[i]));
        }

        return sb.toString();
    }

    /**
     * Truncates the input text to the specified {@code length} to keep the text length to a
     * minimum. If the text contains excessive characters, the resultant string will be truncated
     * with an ellipsis {@code ...} at the end of the string.
     * 
     * @param subject
     *        text to be truncated
     * @param length
     *        is the maximum length before truncation kicks in
     * 
     * @return truncated string if the original length exceeds the specified {@code length},
     *         otherwise the original text remains unchanged
     */
    public static String truncate(String subject, final int length)
    {
        if (subject == null)
        {
            return subject;
        }

        if (subject.length() > length)
        {
            subject = subject.substring(0, length - 3) + "...";
        }

        return subject;
    }

    /**
     * Returns the JVM version at runtime.
     * 
     * @return JVM version as a primitive double type
     */
    public static double getVersionJVM()
    {
        String version = System.getProperty("java.version");

        int pos = version.indexOf('.');

        pos = version.indexOf('.', pos + 1);

        return Double.parseDouble(version.substring(0, pos));
    }

    /**
     * Checks whether the given entry is evaluated as an octal number or not.
     * 
     * @param subject
     *        given octal information in string form
     * 
     * @return boolean true if the number is octal
     */
    public static boolean isOctal(final String subject)
    {
        boolean isOctal = false;

        if (subject != null && isNumeric(subject))
        {
            int number = Integer.parseInt(subject);

            while (number > 0)
            {
                if (number % 10 < 8)
                {
                    isOctal = true;
                }

                else
                {
                    isOctal = false;
                    break;
                }

                number /= 10;
            }
        }

        return isOctal;
    }

    /**
     * Parses a raw CSV-formatted string and converts it into a list of distinct tokens.
     * 
     * Commas within quotation marks are preserved as they are, and not treated as token separators.
     * 
     * @param input
     *        a A raw string containing CSV data
     * 
     * @return a List comprising of the split tokens
     */
    public static List<String> parseCSVstring(final String input)
    {
        String endToken;
        int startIndex = 0;
        boolean isWithinQuotes = false;
        List<String> tokens = new ArrayList<String>();

        for (int position = 0; position < input.length(); position++)
        {
            if (input.charAt(position) == '\"')
            {
                isWithinQuotes = !isWithinQuotes;
            }

            else if (input.charAt(position) == ',' && !isWithinQuotes)
            {
                tokens.add(input.substring(startIndex, position));
                startIndex = position + 1;
            }
        }

        endToken = input.substring(startIndex);
        tokens.add(endToken.equals(",") ? "" : endToken);

        return tokens;
    }
}