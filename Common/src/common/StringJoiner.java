package common;

import java.util.Arrays;
import java.util.List;

/**
 * <p>
 * StringJoiner is designed to construct a sequence of characters separated by a delimiter and
 * optionally you can start with a leading prefix and end with a trailing suffix in the resultant
 * string.
 * </p>
 * 
 * <p>
 * Because there is no standard Java {@code join} functionality commonly found in most popular
 * programming languages such as JavaScript and PHP, this class will nicely emulate this
 * functionality. The algorithm used by the actual method is Divide and Conquer, which is faster
 * than sequential concatenations.
 * </p>
 * 
 * <p>
 * <b>Note</b>, all methods covered in this class are accessed in a static way and the class itself
 * cannot be instantiated.
 * </p>
 * 
 * @author Created on 14 February 2020 by Trevor Maggs
 * @version 0.1
 * @since 14 February 2020
 */
public final class StringJoiner
{
    /**
     * Private constructor to prevent this object from being instantiated.
     */
    private StringJoiner()
    {
    }

    /**
     * Reads elements from the supplied array and returns a string concatenated with individual
     * words separated by a delimiter.
     * 
     * @param <T>
     *        generic array used to join values
     * @param delimiter
     *        used to separate word entries in the resultant string
     * @param arr
     *        the array of elements to be joined
     * 
     * @return joined string
     */
    public static <T> String join(String delimiter, T[] arr)
    {
        int len = arr.length;

        if (len == 0)
        {
            return "";
        }

        else if (len == 1)
        {
            return arr[0].toString();
        }

        else if (len == 2)
        {
            return arr[0].toString().concat(delimiter).concat(arr[1].toString());
        }

        else
        {
            int divide = len / 2;
            T[] s1 = Arrays.copyOfRange(arr, 0, divide);
            T[] s2 = Arrays.copyOfRange(arr, divide, len);

            return join(delimiter, s1).concat(delimiter).concat(join(delimiter, s2));
        }
    }

    /**
     * Reads elements from the supplied list and returns a string concatenated with individual words
     * separated by a delimiter.
     * 
     * @param <T>
     *        List instance used to join values
     * @param delimiter
     *        used to separate word entries in the resultant string
     * @param ls
     *        the list of elements to be joined
     * 
     * @return concatenated string
     */
    public static <T> String join(String delimiter, List<T> ls)
    {
        return join(delimiter, ls.toArray(new String[ls.size()]));
    }

    /**
     * Reads elements from the supplied array starting from the offset position and returns a string
     * concatenated with individual words separated by a delimiter.
     * 
     * @param <T>
     *        generic array used to join values
     * @param delimiter
     *        used to separate word entries in the resultant string
     * @param arr
     *        the array of elements to be joined
     * @param prefix
     *        the sequence of characters to be used at the beginning
     * @param suffix
     *        the sequence of characters to be used at the end
     * @param offset
     *        the number to denote the offset position of the array
     * 
     * @return concatenated string
     */
    public static <T> String join(String delimiter, T[] arr, String prefix, String suffix, int offset)
    {
        if (arr.length < offset)
        {
            return "";
        }

        return join(delimiter, Arrays.copyOfRange(arr, offset, arr.length), prefix, suffix);
    }

    /**
     * Reads elements from the supplied list starting from the offset position and returns a string
     * concatenated with individual words separated by a delimiter.
     * 
     * @param <T>
     *        List instance used to join values
     * @param delimiter
     *        used to separate word entries in the resultant string
     * @param ls
     *        the list of elements to be joined
     * @param prefix
     *        the sequence of characters to be used at the beginning
     * @param suffix
     *        the sequence of characters to be used at the end
     * @param offset
     *        the number to denote the offset position of the list
     * 
     * @return concatenated string
     */
    public static <T> String join(String delimiter, List<T> ls, String prefix, String suffix, int offset)
    {
        return join(delimiter, ls.toArray(new String[ls.size()]), prefix, suffix, offset);
    }

    /**
     * Reads elements from the supplied array and returns a string concatenated with individual
     * words separated by a delimiter.
     * 
     * @param <T>
     *        generic array used to join values
     * @param delimiter
     *        used to separate word entries in the resultant string
     * @param arr
     *        the array of elements to be joined
     * @param prefix
     *        the sequence of characters to be used at the beginning
     * @param suffix
     *        the sequence of characters to be used at the end
     * 
     * @return concatenated string
     */
    public static <T> String join(String delimiter, T[] arr, String prefix, String suffix)
    {
        return prefix + join(delimiter, arr) + suffix;
    }

    /**
     * Reads elements from the supplied list and returns a string concatenated with individual words
     * separated by a delimiter.
     * 
     * @param <T>
     *        List instance used to join values
     * @param delimiter
     *        used to separate word entries in the resultant string
     * @param ls
     *        the list of elements to be joined
     * @param prefix
     *        the sequence of characters to be used at the beginning
     * @param suffix
     *        the sequence of characters to be used at the end
     * 
     * @return concatenated string
     */
    public static <T> String join(String delimiter, List<T> ls, String prefix, String suffix)
    {
        return join(delimiter, ls, prefix, suffix, 0);
    }

    /**
     * Reads elements from the supplied array starting from the offset position and returns a string
     * concatenated with individual words separated by a delimiter.
     * 
     * @param <T>
     *        generic array used to join values
     * @param delimiter
     *        used to separate word entries in the resultant string
     * @param arr
     *        the array of elements to be joined
     * @param offset
     *        the number to denote the offset position of the array
     * 
     * @return concatenated string
     */
    public static <T> String join(String delimiter, T[] arr, int offset)
    {
        return join(delimiter, arr, "", "", offset);
    }

    /**
     * Reads elements from the supplied list starting from the offset position and returns a string
     * concatenated with individual words separated by a delimiter.
     * 
     * @param <T>
     *        List instance used to join values
     * @param delimiter
     *        used to separate word entries in the resultant string
     * @param ls
     *        the list of elements to be joined
     * @param offset
     *        the number to denote the offset position of the list
     * 
     * @return concatenated string
     */
    public static <T> String join(String delimiter, List<T> ls, int offset)
    {
        return join(delimiter, ls, "", "", offset);
    }
}