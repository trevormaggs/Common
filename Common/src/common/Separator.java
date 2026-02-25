package common;

/**
 * <p>
 * Provides a lightweight solution for joining textual elements into a single character-delimited
 * String line.
 * </p>
 * 
 * <p>
 * This class works best in conjunction with the {@code StringBuilder} object, producing a single
 * character-delimited String line without adding a delimiter before or after the line.
 * </p>
 * 
 * <p>
 * The default delimiter is a comma, but this can be changed by the user. Below is a code example.
 * </p>
 * 
 * <pre>
 * Separator sep = new Separator(", ");
 * StringBuilder buf = new StringBuilder();
 * 
 * for (String line : arr)
 * {
 *     buf.append(sep).append(line);
 * }
 * 
 * System.out.println(buf.toString());
 * </pre>
 * 
 * <p>
 * <b>Change Log:</b>
 * </p>
 * 
 * <ul>
 * <li>Trevor Maggs created on 1 June 2017 (initial version)</li>
 * </ul>
 * 
 * @author Trevor Maggs
 * @version 0.1
 * @since 1 June 2017
 */
public class Separator
{
    private String delimiter;
    private boolean skipFirst;

    /**
     * Constructs a new Separator object with a default delimiter set to a comma (",").
     */
    public Separator()
    {
        this(", ");
    }

    /**
     * Constructs a new Separator object, allowing the user to specify a custom delimiter as
     * required.
     * 
     * @param delim
     *        the custom delimiter to be used
     */
    public Separator(final String delim)
    {
        this.delimiter = delim;
        this.skipFirst = true;
    }

    /**
     * Resets the delimiter.
     */
    public void reset()
    {
        skipFirst = true;
    }

    /**
     * Resets the separator with the specified delimiter.
     * 
     * @param delim
     *        the new delimiter to use
     */
    public void reset(final String delim)
    {
        reset();
        this.delimiter = delim;
    }

    /**
     * Changes the delimiter and takes effect immediately.
     * 
     * @param delim
     *        the new delimiter
     */
    public void change(final String delim)
    {
        this.delimiter = delim;
    }

    /**
     * Returns the current delimiter.
     * 
     * @return the current delimiter character
     */
    public String toString()
    {
        String sep = skipFirst ? "" : delimiter;
        skipFirst = false;

        return sep;
    }
}