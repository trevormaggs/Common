package common.clireader;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class CommandTokenizer
{
    /**
     * Constructs a new instance of the FlagType class, using the command line arguments
     * provided and offering the option to enable debugging.
     *
     * @param rawArgs
     *        the command line arguments
     * @param debug
     *        a boolean flag specifying whether debugging should be enabled
     */
    public CommandTokenizer(String[] rawArgs, boolean debug)
    {
        List<String> list = sanitise(rawArgs);

        if (debug)
        {
            for (String token : list)
            {
                System.out.printf("Token: %s\n", token);
            }
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
     * Replaces the old 'sanitise' method logic to merge tokens ending/starting
     * with '=' or ',' into single logical units.
     */
    private List<String> sanitise(String[] args)
    {
        StringBuilder sb = new StringBuilder();
        List<String> result = new ArrayList<>();

        if (args == null || args.length == 0)
        {
            return result;
        }

        for (int i = 0; i < args.length; i++)
        {
            boolean glueNext = false;
            String current = args[i];

            // We skip empty strings only if they exist in the raw array (rare for OS args)
            if (current.isEmpty())
            {
                continue;
            }

            sb.append(current);

            if (i + 1 < args.length)
            {
                String next = args[i + 1];

                // Logic: Glue if we are mid-assignment (=) or mid-list (,)
                // BUT: Don't glue if the next token is clearly a new flag starting with -
                if (current.endsWith("=") || next.startsWith("="))
                {
                    glueNext = true;
                }

                else if (current.endsWith(",") || next.startsWith(","))
                {
                    // Peek ahead: Is the next token a new flag?
                    if (!next.matches("^\\-{1,2}[a-zA-Z].*"))
                    {
                        glueNext = true;
                    }
                }
            }

            if (!glueNext)
            {
                // Clean up multiple commas/trailing commas before storing
                String token = cleanCommas(sb.toString());

                if (!token.isEmpty())
                {
                    result.add(token);
                }

                sb.setLength(0);
            }
        }

        if (sb.length() > 0)
        {
            String last = cleanCommas(sb.toString());

            if (!last.isEmpty())
            {
                result.add(last);
            }
        }

        return result;
    }

    /**
     * Uses regex to collapse multiple commas into one and removes leading/trailing commas from the
     * merged argument.
     */
    private String cleanCommas(String input)
    {
        if (input == null)
        {
            return "";
        }

        // 1. Collapse multiple commas into a single comma
        String cleaned = input.replaceAll(",+", ",");

        // 2. Remove comma if it sits right after an equals sign (e.g., --range=,108 -> --range=108)
        cleaned = cleaned.replaceAll("=,", "=");

        // 3. Strip leading/trailing commas
        cleaned = cleaned.replaceAll("^,", "").replaceAll(",$", "");

        return cleaned;
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