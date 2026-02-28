package common.clireader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 */
public class CommandTokenizer
{
    private final List<String> tokens;

    /**
     * Constructs a new instance designed to sanitise and normalise the specified command line
     * arguments.
     *
     * @param args
     *        the command line arguments
     */
    public CommandTokenizer(String[] args)
    {
        tokens = (args != null && args.length > 0 ? normalise(args) : Collections.emptyList());
    }

    /**
     * Performs sanitisation of the specified array of arguments and returns a list of cleaned
     * tokens to provide normalisation. It also takes care of merging tokens ending/starting with
     * '=' or ',' into single logical units.
     *
     * @param args
     *        the command line arguments
     * @return a list of normalised tokens
     */
    private List<String> normalise(String[] args)
    {
        StringBuilder sb = new StringBuilder();
        List<String> result = new ArrayList<>();

        for (int i = 0; i < args.length; i++)
        {
            boolean joinNext = false;
            String current = args[i];

            // Although unlikely, it defensively protects
            // integrity in case empty strings do happen
            if (current.isEmpty())
            {
                continue;
            }

            sb.append(current);

            if (i + 1 < args.length)
            {
                String next = args[i + 1];

                // Make sure the next token is not a negative number
                if (!next.matches("^\\-{1,2}[a-zA-Z].*"))
                {
                    if (current.endsWith("=") || next.startsWith("=") || current.endsWith(",") || next.startsWith(","))
                    {
                        joinNext = true;
                    }
                }
            }

            if (!joinNext)
            {
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
     * Provides the normalised tokens for further processing.
     *
     * @return the list of normalised tokens
     */
    public List<String> getTokens()
    {
        return tokens;
    }

    /**
     * Flattens the command line arguments into a single string.
     *
     * @return the flattened string with individual tokens separated by a single whitespace
     */
    public String flattenArguments()
    {
        return "[" + String.join(" ", tokens) + "]";
    }

    /**
     * Using regex to collapse multiple commas into one and removes leading/trailing commas from the
     * merged argument.
     */
    private static String cleanCommas(String input)
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
        cleaned = cleaned.replaceAll("^,|,$", "");

        // Removes any commas or spaces immediately following an equals sign
        cleaned = cleaned.replaceAll("=[\\s,]+", "=");

        return cleaned;
    }

    @Override
    public String toString()
    {
        if (tokens.isEmpty())
        {
            return "No tokens";
        }

        StringBuilder sb = new StringBuilder(256);

        for (String token : tokens)
        {
            sb.append("Token: ");
            sb.append(token);
            sb.append(System.lineSeparator());
        }

        return sb.toString();
    }
}