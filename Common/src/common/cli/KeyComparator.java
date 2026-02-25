package common.cli;

import java.util.Comparator;

/**
 * This comparator class is designed to sort the option names stored in a {@link FlagOptionRule}
 * instance. The ordering is important to ensure a consistent read sequence in ascending order.
 * 
 * @author Trevor Maggs
 * @version 1.0
 * @since 21 August 2024
 */
public class KeyComparator implements Comparator<String>
{
    /**
     * Compares two string objects for ordering purposes.
     * 
     * @param o1
     *        the first string item
     * @param o2
     *        the second string item
     * 
     * @return a negative integer, zero, or a positive integer as the first string is less than,
     *         equal to, or greater than the second string
     */
    public int compare(String o1, String o2)
    {
        return o1.compareTo(o2);
    }
}