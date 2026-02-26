package common.clireader;

/**
 *
 */
public class CommandTokenizer
{
    /**
     * Constructs a new instance of the FlagType class, using the command line arguments
     * provided and offering the option to enable debugging.
     *
     * @param args
     *        the command line arguments
     * @param dbg
     *        a boolean flag specifying whether debugging should be enabled
     */
    public CommandTokenizer(String[] args, boolean dbg)
    {
        if (args == null || args.length == 0)
        {
            throw new IllegalArgumentException("Command arguments are empty.");
        }
    }

    /**
     * Constructs a new instance of the CommandTokenizer class using the command line arguments
     * provided.
     *
     * @param args
     *        the command line arguments
     */
    public CommandTokenizer(String[] args)
    {
        this(args, false);
    }

    /**
     * Removes the leading dashes from the specified token.
     *
     * @param token
     *        the entry extracted from the command line
     *
     * @return the string with the dashes removed
     */
    private static String stripLeadingDashes(final String token)
    {
        // double dash
        if (token.startsWith("--"))
        {
            return token.substring(2);
        }

        // single dash
        else if (token.startsWith("-"))
        {
            return token.substring(1);
        }

        else
        {
            return token;
        }
    }
}