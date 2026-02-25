package common.cli;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import common.Generic;
import common.PeekingIterator;
import common.StringJoiner;

/**
 * A stand-alone command-line parser for Java applications.
 *
 * This parser extracts flags, options, and arguments from the command line, processes them, and
 * feeds them to any Java applications as data inputs. It supports short (-S), extended short (-E),
 * and long (--L) option types.
 *
 * The process involves parsing raw tokens from the command line and dividing them into constituent
 * parts to represent options and arguments. Each option being handled is checked against the
 * {@link FlagOptionRule} object for compliance. If the option is not defined in the rule, it will
 * be considered as unknown.
 *
 * You must establish the rules for the options using the {@link addRule} method prior to parsing.
 * When defining the rules for the options, there are 5 possibilities:
 *
 * <table>
 * <caption>Rule Definitions</caption>
 * <tr>
 * <th>Rule</th>
 * <th>Meaning</th>
 * </tr>
 * <tr>
 * <td>ARG_BLANK</td>
 * <td>Standalone option. This is optional</td>
 * </tr>
 * <tr>
 * <td>ARG_REQUIRED</td>
 * <td>Mandatory option that must be followed by a value</td>
 * </tr>
 * <tr>
 * <td>ARG_OPTIONAL</td>
 * <td>Optional option that must be followed by a value</td>
 * </tr>
 * <tr>
 * <td>SEP_REQUIRED</td>
 * <td>Mandatory option that must have a value separator ('=')</td>
 * </tr>
 * <tr>
 * <td>SEP_OPTIONAL</td>
 * <td>Optional option that must have a value separator ('=')</td>
 * </tr>
 * </table>
 *
 * Note that the last 2 rules will also recognise comma-separated arguments, and the parser will
 * construct a list of values that can be retrieved. For example, -value=12,24,36 etc.
 *
 * <p>
 * Some examples include the following:
 * </p>
 *
 * <ul>
 * <li>cmd -g vulfeed.dat</li>
 * <li>cmd --csv</li>
 * <li>cmd -path /var/log/value.log</li>
 * <li>cmd --depth=7</li>
 * <li>cmd -b=3</li>
 * <li>cmd -ofile.txt (concatenated token -o = option, file.txt = argument)</li>
 * <li>cmd -hv (2 concatenated stand-alone short options)</li>
 * </ul>
 *
 * <p>
 * This parser will identify concatenated tokens, for example {@code -d787} where -d is the short
 * option and 787 is the value. {@code -avb=747} where -a and -v are two stand-alone options and -b
 * option is assigned with the value of 747.
 * </p>
 *
 * <p>
 * Change logs:
 * </p>
 *
 * <ul>
 * <li>Developed by Trevor Maggs - 6 February 2018</li>
 * <li>Improved logic in the handling of short options - 2 July 2018</li>
 * <li>Class renamed from CommandParser and added support to enforce value separator for options and
 * improved logic in several methods - 23 August 2021</li>
 * <li>Code overhauled after finding subtle bugs in the previous version. - 1 August 2024</li>
 * <li>Further bugs identified and introduced the sanitise method to organise the arguments into
 * groups and remove other complications. - 9 December 2024</li>
 * </ul>
 *
 * @author Trevor Maggs
 * @version 0.5
 * @since 9 December 2024
 */
public final class CommandLineReaderOrig
{
    // Public constants to define the rule types
    public static final int ARG_BLANK = 1;
    public static final int ARG_REQUIRED = 2;
    public static final int ARG_OPTIONAL = 3;
    public static final int SEP_REQUIRED = 4;
    public static final int SEP_OPTIONAL = 5;

    private int maxArgumentCount;
    private boolean debug;
    private boolean valueRange;
    private List<String> arguments;
    private FlagOptionRule activeFlagView;
    private final List<String> requiredOptions;
    private final List<String> standaloneTokens;
    private final Map<String, FlagOptionRule> flagRuleMap;

    /**
     * Constructs a new instance of the CommandLineReader class with the given command line
     * arguments.
     * 
     * @param args
     *        the command line arguments
     */

    /**
     * Constructs a new instance of the CommandLineReader class using the command line arguments
     * provided.
     *
     * @param args
     *        the command line arguments
     */
    public CommandLineReaderOrig(String[] args)
    {
        this(args, false);
    }

    /**
     * Constructs a new instance of the CommandLineReader class, using the command line arguments
     * provided and offering the option to enable debugging.
     *
     * @param args
     *        the command line arguments
     * @param dbg
     *        a boolean flag specifying whether debugging should be enabled
     */
    public CommandLineReaderOrig(String[] args, boolean dbg)
    {
        if (args == null || args.length == 0)
        {
            throw new IllegalArgumentException("Command arguments are empty.");
        }

        debug = dbg;
        maxArgumentCount = 0;
        arguments = sanitise(args);
        requiredOptions = new ArrayList<>();
        standaloneTokens = new ArrayList<>();
        flagRuleMap = new TreeMap<>(new KeyComparator());
    }

    /**
     * Sanitises the input arguments by re-arranging them into groups and shifting them. This
     * ensures that they are easily parsed without causing complications.
     *
     * @param args
     *        The command line arguments
     *
     * @return A list of organised arguments
     */
    private static List<String> sanitise(String[] args)
    {
        List<String> token = new ArrayList<>();
        StringBuilder sb = new StringBuilder().append(args[0]);

        for (int k = 1; k < args.length; k++)
        {
            String k1 = args[k - 1];
            String k2 = args[k];

            if (k1.endsWith("=") || k2.startsWith("="))
            {
                sb.append(k2);
            }

            else if (!k2.startsWith("-") && (k1.endsWith(",") || k2.startsWith(",")))
            {
                sb.append(k2);
            }

            else
            {
                token.add(sb.toString());
                sb.setLength(0);
                sb.append(k2);
            }
        }

        /* Insert the last argument */
        token.add(args[args.length - 1]);

        return token;
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

    /**
     * Verifies if the specified token represents a long option starting with double leading dashes
     * (--). The option can optionally have a value appended, such as --LV or --L=V.
     *
     * @param token
     *        the command line token
     *
     * @return true if the token is a valid long option
     */
    private static boolean isLongOption(final String token)
    {
        return token.matches("\\--[A-Za-z].*$") && token.length() > 3;
    }

    /**
     * Verifies if the specified token is an extended short option. Similar to a short option, but
     * the option name can be more than one character and starts with a single leading dash (-E).
     * It can also have a value appended, such as -EV or -E=V.
     *
     * @param token
     *        the command line token
     *
     * @return true if the token is an extended short option
     */
    private static boolean isExtendedShortOption(final String token)
    {
        return (token.matches("\\-[A-Za-z]+.*$") && token.length() > 2);
    }

    /**
     * Verifies if the specified token represents a valid short option.
     *
     * A short option is a single character preceded by a single leading dash (-S). It can be
     * combined with other short options and may be followed by a value, such as -SV, -S=V,
     * -S1S2V, -S1S2=V, or -S1S2=V1,V2,V3.
     *
     * @param token
     *        the raw command line token
     *
     * @return true if the token is a valid short option
     */
    private static boolean isShortOption(final String token)
    {
        return (token.matches("^\\-[^\\-].*$") && token.length() > 1);
    }

    /**
     * Determines whether the specified token represents a valid argument.
     *
     * @param token
     *        the entry extracted from the command line
     *
     * @return true if the argument is valid, false otherwise
     */
    private static boolean isValidArgument(String token)
    {
        return (!isOption(token) || isNegativeNumber(token));
    }

    /**
     * Determines whether the specified token represents a valid command option.
     *
     * @param token
     *        the entry extracted from the command line
     *
     * @return true if the command option is identified, false otherwise
     */
    private static boolean isOption(String token)
    {
        return (isLongOption(token) || isShortOption(token) || isExtendedShortOption(token));
    }

    /**
     * Determines whether the specified token represents a valid negative number.
     * 
     * @param token
     *        the entry extracted from the command line
     * 
     * @return true if the token is a valid negative number, otherwise false
     */
    private static boolean isNegativeNumber(String token)
    {
        try
        {
            return (Double.parseDouble(token) < 0);
        }

        catch (NumberFormatException e)
        {
            return false;
        }
    }

    /*
     * Private methods for internal operations.
     */

    /**
     * Verifies that all required options are present and ready for use. If the requirement is not
     * met, it throws an exception.
     *
     * @throws ParseException
     *         If any of the remaining options have not been processed yet, the message displays a
     *         comma-separated string representation of any missing flag names in the console
     */
    private void validateRequiredOptions() throws ParseException
    {
        if (!requiredOptions.isEmpty())
        {
            String missingOptions = String.join(", ", requiredOptions);

            throw new ParseException("Missing required option" + (requiredOptions.size() > 1 ? "s" : "") + ": [" + missingOptions + "]", 0);
        }
    }

    /**
     * Determines if the specified token is a valid long command option based on the
     * {@link FlagOptionRule} rule.
     *
     * The following long option combinations are supported:
     *
     * <ul>
     * <li>--L</li>
     * <li>--LV</li>
     * <li>--L=V</li>
     * </ul>
     *
     * @param token
     *        The command line token
     *
     * @return true if the token is a valid long command option
     */
    private boolean hasLongOption(final String token)
    {
        String opt = (token.contains("=") ? token.substring(0, token.indexOf("=")) : token);

        for (FlagOptionRule rule : flagRuleMap.values())
        {
            if (rule.isLongOption() && opt.startsWith(rule.getOptionName()))
            {
                // Make sure the exact option descriptor is captured.
                // ie --var=99 is not the same as --vars=99
                if (opt.equals(token) || opt.equals(rule.getOptionName()))
                {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Processes a long option token, handling both with and without a separator.
     * 
     * @param token
     *        the command line token to process
     * 
     * @throws ParseException
     *         if there are any problems encountered while attempting to parse the command line
     *         token
     */
    private void handleLongOption(final String token) throws ParseException
    {
        if (token.contains("="))
        {
            handleOptionWithSeparator(token);
        }

        else
        {
            handleLongOptionWithoutSeparator(token);
        }
    }

    /**
     * Processes long options without a value separator. A value may be concatenated in the input
     * string. The following combinations are supported:
     * 
     * <ul>
     * <li>--L</li>
     * <li>--LV</li>
     * </ul>
     * 
     * @param token
     *        the command line token to process
     * 
     * @throws ParseException
     *         if there are any problems encountered while attempting to parse the command line
     *         token
     */
    private void handleLongOptionWithoutSeparator(String token) throws ParseException
    {
        for (FlagOptionRule rule : flagRuleMap.values())
        {
            if (rule.isLongOption() && token.startsWith(rule.getOptionName()))
            {
                String value = token.substring(rule.getOptionName().length());

                // --L
                setOptionFound(rule);

                // --LV
                if (activeFlagView.expectsArgument())
                {
                    if (value.length() > 0)
                    {
                        activeFlagView.setOptionDone();

                        if (value.contains(","))
                        {
                            processRangeValues(value);
                        }

                        else
                        {
                            processSingleValue(value);
                        }
                    }
                }

                else if (!value.isEmpty())
                {
                    processRemainingArgument(value);
                }

                else
                {
                    activeFlagView.setOptionDone();
                    activeFlagView = null;
                }

                break;
            }
        }
    }

    /**
     * Processes extended short and long options with a value separator.
     * 
     * Options processed:
     * 
     * <ul>
     * <li>-E=V</li>
     * <li>-E=V1,V2,V3</li>
     * <li>--L=V</li>
     * <li>--L=V1,V2,V3</li>
     * </ul>
     * 
     * @param token
     *        the command line token to process
     * 
     * @throws ParseException
     *         if an error occurs while parsing the command line token
     */
    private void handleOptionWithSeparator(String token) throws ParseException
    {
        String[] parts = token.split("=", 2);
        FlagOptionRule rule = flagRuleMap.get(stripLeadingDashes(parts[0]));
        String value = parts[1];

        setOptionFound(rule);
        activeFlagView.setSeparator();

        if (value.length() > 0)
        {
            activeFlagView.setOptionDone();

            if (value.contains(","))
            {
                processRangeValues(value);
            }

            else
            {
                processSingleValue(value);
            }
        }

        else if (!value.isEmpty())
        {
            processRemainingArgument(value);
        }
    }

    /**
     * Checks if the specified token is a valid short option according to the {@link FlagOptionRule}
     * rule.
     * 
     * @param token
     *        the command line token
     * 
     * @return true if the token is a valid short option
     */
    private boolean hasShortOption(String token)
    {
        String opt = (token.contains("=")) ? token.substring(0, token.indexOf("=")) : token;

        /*
         * To find a match with stand-alone flag (-S) or
         * with concatenations (-SV or -S1S2 or -S1S2V)
         */
        for (FlagOptionRule rule : flagRuleMap.values())
        {
            if (rule.isShortOption() && opt.startsWith(rule.getOptionName()))
            {
                if (opt.equals(token) || opt.equals(rule.getOptionName()))
                {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Handles short option token combinations and processes them. A value may be concatenated or
     * delimited by a separator in the specified token. It can handle both with or without a
     * separator.
     *
     * <p>
     * Supported short option combinations as listed below:
     * </p>
     *
     * <ul>
     * <li>-S</li>
     * <li>-SV</li>
     * <li>-S=V</li>
     * <li>-S=V1,V2,V3</li>
     * <li>-S1S2</li>
     * <li>-S1S2V</li>
     * <li>-S1S2=V</li>
     * <li>-S1S2=V1,V2,V3</li>
     * </ul>
     *
     * -S, -SV, -S=V -S1S2, -S1S2V, or S1S2=V
     * <p>
     * <b>Examples:</b> {@code -b, -v7, -v=53, -ab, -nv8, -nv=5, -range=12,24,36,48}
     * </p>
     *
     * @param token
     *        the command line token to process
     *
     * @throws ParseException
     *         if there are any problems encountered while attempting to parse the command line
     *         token
     */
    private void handleShortOption(String token) throws ParseException
    {
        // Start by skipping the leading single dash "-"
        for (int i = 1; i < token.length(); i++)
        {
            String key = String.valueOf(token.charAt(i));

            if (flagRuleMap.containsKey(key))
            {
                setOptionFound(flagRuleMap.get(key));

                if (activeFlagView.expectsArgument())
                {
                    if ((i + 1) < token.length())
                    {
                        String value = token.substring(i + 1);

                        activeFlagView.setOptionDone();

                        // -SV, -S1S2V
                        if (value.charAt(0) != '=')
                        {
                            processSingleValue(value);
                            activeFlagView = null;
                        }

                        // -S=V, -S1S2=V or -S=V1,V2,V3
                        else
                        {
                            activeFlagView.setSeparator();
                            value = value.substring(1);

                            if (value.length() > 0)
                            {
                                // -S=V1,V2,V3
                                if (value.contains(","))
                                {
                                    processRangeValues(value);
                                }

                                // -S=V
                                else
                                {
                                    processSingleValue(value);
                                }
                            }
                        }
                    }

                    break;
                }

                // -S1, -S1S2S3
                else
                {
                    activeFlagView.setOptionDone();
                    activeFlagView = null;
                }
            }

            else if (i == 1 && key.matches("^[A-Za-z]"))
            {
                processRemainingArgument("-" + key);
            }

            else
            {
                processRemainingArgument(token.substring(i));
            }
        }
    }

    /**
     * Checks if the specified command line option is a valid extended short option according to the
     * {@link FlagOptionRule} rule.
     * 
     * Supported extended short option combinations:
     * 
     * <ul>
     * <li>-E</li>
     * <li>-EV</li>
     * <li>-E=V</li>
     * </ul>
     * 
     * <p>
     * <b>Examples:</b> {@code -file, -value7, -value=53}
     * </p>
     * 
     * @param token
     *        the command line option
     * 
     * @return true if the command line option is a valid extended short option
     */
    private boolean hasExtendedShortOption(String token)
    {
        String opt = (token.contains("=") ? token.substring(0, token.indexOf("=")) : token);

        for (FlagOptionRule rule : flagRuleMap.values())
        {
            /*
             * -E, -EV
             */
            if (rule.isExtendedShortOption() && opt.startsWith(rule.getOptionName()))
            {
                if (opt.equals(token) || opt.equals(rule.getOptionName()))
                {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Handles extended short option combinations, including values with or without separators.
     * 
     * Supported options:
     * 
     * <ul>
     * <li>-E</li>
     * <li>-EV</li>
     * <li>-E=V</li>
     * <li>-E=V1,V2,V3</li>
     * </ul>
     * 
     * <p>
     * <b>Examples:</b> {@code -file, -value7, -value=53}
     * </p>
     * 
     * @param token
     *        the command line token to process
     * 
     * @throws ParseException
     *         if an error occurs while parsing the command line token
     */
    private void handleExtendedShortOption(String token) throws ParseException
    {
        if (token.contains("="))
        {
            handleOptionWithSeparator(token);
        }

        else
        {
            handleExtendedShortOptionWithoutSeparator(token);
        }
    }

    /**
     * Processes the extended short options without a separator. A value may be concatenated in the
     * input string. The following combinations are supported:
     * 
     * <ul>
     * <li>-E</li>
     * <li>-EV</li>
     * </ul>
     * 
     * @param token
     *        the command line token
     * 
     * @throws ParseException
     *         if an error occurs while parsing the command line token
     */
    private void handleExtendedShortOptionWithoutSeparator(String token) throws ParseException
    {
        for (FlagOptionRule rule : flagRuleMap.values())
        {
            if (rule.isExtendedShortOption() && token.startsWith(rule.getOptionName()))
            {
                String value = token.substring(rule.getOptionName().length());

                // -E
                setOptionFound(rule);

                // -EV
                if (activeFlagView.expectsArgument())
                {
                    if (value.length() > 0)
                    {
                        activeFlagView.setOptionDone();

                        if (value.contains(","))
                        {
                            processRangeValues(value);
                        }

                        else
                        {
                            processSingleValue(value);
                        }
                    }
                }

                else if (!value.isEmpty())
                {
                    processRemainingArgument(value);
                }

                else
                {
                    activeFlagView.setOptionDone();
                    activeFlagView = null;
                }

                break;
            }
        }
    }

    /**
     * Handles remaining or unknown tokens.
     * 
     * If the token starts with a dash, a {@link ParseException} is thrown. Otherwise, it is added
     * to the list of unknown arguments.
     * 
     * @param token
     *        the command line token
     * 
     * @throws ParseException
     *         if the token is unrecognised
     */
    private void processRemainingArgument(String token) throws ParseException
    {
        if (token.length() > 0)
        {
            if (token.startsWith("-") && token.length() > 1)
            {
                String opt = token.replaceAll("^((\\-)+\\D+).*$", "$1");

                throw new ParseException("The option [" + opt + "] is unrecognisesd.", 0);
            }

            standaloneTokens.add(token.replace(",", ""));
        }
    }

    /**
     * Sets the current option being processed. This method updates the option being processed based
     * on the state of the {@link FlagOptionRule} object.
     * 
     * If the option requires an argument, it will be checked for presence. If absent, an exception
     * will be thrown.
     * 
     * If the option does not require an argument, it will be treated as a stand-alone flag.
     * 
     * @param opt
     *        the {@link FlagOptionRule} instance to indicate the current option
     */
    private void setOptionFound(FlagOptionRule opt)
    {
        activeFlagView = opt;
        activeFlagView.resetOption();
    }

    /**
     * Processes a comma-separated string and adds each individual value to the list of values for
     * the current option.
     * 
     * @param value
     *        the individual data to be added
     */
    private void processRangeValues(String value)
    {
        if (value != null && !value.isEmpty())
        {
            String[] values = value.split(",");

            valueRange = true;

            for (String data : values)
            {
                processSingleValue(data);
            }
        }
    }

    /**
     * Processes the specified value associated with the option being handled, if it is available.
     *
     * @param value
     *        the single data associated with the option being processed. A value of null is
     *        accepted if the value is not available
     */
    private void processSingleValue(String value)
    {
        if (value != null && !value.isEmpty())
        {
            activeFlagView.addValue(value.replace(",", ""));

            if (activeFlagView.isRequired() && requiredOptions.contains(activeFlagView.getOptionName()))
            {
                requiredOptions.remove(activeFlagView.getOptionName());
            }
        }
    }

    /**
     * Validates the number of left-over arguments that are allowed.
     * 
     * @throws ParseException
     *         if the actual number of arguments exceeds the maximum allowed
     */
    private void validateArgumentCount() throws ParseException
    {
        if (maxArgumentCount > 0)
        {
            if (standaloneTokens.size() > maxArgumentCount)
            {
                String msg = String.format("You can only have %d argument%s. Found %s.", maxArgumentCount, (maxArgumentCount > 1 ? "s" : ""), standaloneTokens);
                throw new ParseException(msg, 0);
            }
        }
    }

    /*
     * Public exported API methods.
     */

    /**
     * Flattens the command line arguments into a single string.
     *
     * @return the flattened string with individual tokens separated by a single whitespace
     */
    public String flattenArguments()
    {
        return StringJoiner.join(" ", arguments, "[", "]");
    }

    /**
     * Sets the maximum number of stand-alone arguments that the client application can have.
     * These arguments are not associated with any options or flags in the command line.
     *
     * @param count
     *        the number of arguments
     */
    public void setMaximumStandaloneArgumentCount(int count)
    {
        maxArgumentCount = count;
    }

    /**
     * Retrieves the count of stand-alone arguments that are not associated with flags in the
     * command line.
     * 
     * @return the number of stand-alone arguments being passed to the application for processing
     */
    public int getStandaloneArgumentCount()
    {
        return standaloneTokens.size();
    }

    /**
     * Retrieves the first stand-alone argument from the list.
     * 
     * @return the first stand-alone argument
     */
    public String getFirstStandaloneArgument()
    {
        return getStandaloneArgumentByIndex(0);
    }

    /**
     * Retrieves the last stand-alone argument from the list.
     * 
     * @return the last stand-alone argument
     */
    public String getLastStandaloneArgument()
    {
        return getStandaloneArgumentByIndex(standaloneTokens.size() - 1);
    }

    /**
     * Retrieves the stand-alone argument at the specified position in the list.
     * 
     * @param k
     *        the position number or index of the list to retrieve
     * 
     * @return the stand-alone argument at the specified position, or an empty string if out of
     *         bounds
     */
    public String getStandaloneArgumentByIndex(int k)
    {
        return (k < 0 && k > standaloneTokens.size() ? "" : standaloneTokens.get(k));
    }

    /**
     * Determines if the specified option has been handled in the command line.
     * 
     * <p>
     * As an example, the following snippet checks if a particular {@code -f} option has been
     * defined.
     * </p>
     * 
     * <pre>
     * <code>
     * if (cli.existsOption("-f"))
     * {
     *     // Do something here
     * }
     * </code>
     * </pre>
     * 
     * @param optName
     *        the name of the command option with leading dashes provided
     * 
     * @return true if the option exists, false otherwise
     */
    public boolean existsOption(String optName)
    {
        String key = stripLeadingDashes(optName);

        if (flagRuleMap.containsKey(key))
        {
            return flagRuleMap.get(key).isOptionDone();
        }

        return false;
    }

    /**
     * Retrieves the number of values or items associated with the specified option.
     * 
     * @param optName
     *        the name of the command option
     * 
     * @return the number of items if available or zero if the specified option is unknown
     */
    public int getValueLength(String optName)
    {
        if (existsOption(optName))
        {
            return flagRuleMap.get(stripLeadingDashes(optName)).getSize();
        }

        return 0;
    }

    /**
     * Retrieves the value of the specified option if it is defined, otherwise an empty string is
     * returned.
     * 
     * <p>
     * Example: the following snippet retrieves the value of the {@code -f} option.
     * </p>
     * 
     * <pre>
     * <code>
     *   System.out.println(cli.getValueByOption("-f"));
     * </code>
     * </pre>
     * 
     * @param optName
     *        the command option name
     * 
     * @return the value of the specified option name or a blank string if there is no value or
     *         argument
     */
    public String getValueByOption(String optName)
    {
        return getValueByOption(optName, 0);
    }

    /**
     * Retrieves the value of the specified option by position in the list, where the information is
     * held. If the option does not exist or the index is out of bounds, an empty string is
     * returned.
     * 
     * @param optName
     *        the command option name
     * @param index
     *        the index or position of the value to be returned
     * 
     * @return the value of the specified option or a blank string if there is no value
     * 
     * @throws IndexOutOfBoundsException
     *         if index is less than 0 or greater than the size of the values for the specified
     *         option
     */
    public String getValueByOption(String optName, int index) throws IndexOutOfBoundsException
    {
        FlagOptionRule rule = flagRuleMap.get(stripLeadingDashes(optName));

        return (existsOption(optName) ? rule.getValue(index) : "");
    }

    /**
     * Defines command option rules to control how each option should be expected to respond and
     * behave, depending on the requirement of the client Java applications. This method is best
     * understood by examining the examples below.
     * 
     * <pre>
     * Define a mandatory option -x with a required value or argument.
     *     addRule("-x", CommandLineParser.ARG_REQUIRED);
    
     * Define an optional standalone flag --verbose without a value.
     *     addRule("--verbose", CommandLineParser.ARG_BLANK);
    
     * Define an optional long flag --depth that expects an argument.
     *     addRule("--depth", CommandLineParser.ARG_OPTIONAL);
    
     * Define an extended short flag -value with an argument delimited by a separator.
     *     addRule("-value", CommandLineParser.SEP_REQUIRED);
     * </pre>
     * 
     * @param opt
     *        the new option descriptor
     * @param type
     *        the option rule type - see constant variables for details
     * 
     * @throws ParseException
     *         if the option name contains non-valid characters
     */
    public void addRule(String opt, int type) throws ParseException
    {
        FlagOptionRule rule = new FlagOptionRule(opt, type);
        flagRuleMap.put(stripLeadingDashes(opt), rule);

        if (type == ARG_REQUIRED || type == SEP_REQUIRED)
        {
            requiredOptions.add(opt);
        }
    }

    /**
     * Parses the command line flags and verifies their compliance with the user-defined option
     * rules. Any unrecognised tokens encountered during the process will stored in a simple list
     * and can be passed on to the client application for handling if necessary.
     *
     * @throws ParseException
     *         if there are problems while parsing command line tokens
     */
    public void parse() throws ParseException
    {
        if (flagRuleMap.size() == 0)
        {
            throw new ParseException("Rules for command line options must be defined prior to parsing.", 0);
        }

        PeekingIterator<String> iter = new PeekingIterator<String>(arguments);

        while (iter.hasNext())
        {
            String token = iter.next();

            if (activeFlagView != null && activeFlagView.expectsArgument() && isValidArgument(token))
            {
                activeFlagView.setOptionDone();

                if (token.startsWith("=") || (!iter.peek().isEmpty() && iter.peek().startsWith("=")))
                {
                    activeFlagView.setSeparator();

                    // Example: "=image.jpg"
                    if (token.length() > 1)
                    {
                        token = token.substring(1);
                    }

                    else if (!iter.peek().isEmpty() && isOption(iter.peek()))
                    {
                        throw new ParseException("Option [" + activeFlagView.getOptionName() + "] expects an argument.", 0);
                    }

                    // Example: "="
                    else
                    {
                        continue;
                    }
                }

                if (valueRange && token.contains(",") && !iter.peek().isEmpty() && !iter.peek().contains(","))
                {
                    processRangeValues(token);
                    valueRange = false;
                    activeFlagView = null;
                }

                else if (valueRange && token.contains(",") || (!iter.peek().isEmpty() && iter.peek().startsWith(",")))
                {
                    processRangeValues(token);
                }

                else
                {
                    processSingleValue(token);
                    activeFlagView = null;
                }
            }

            // System.out.format("Token: %s%n", token);
            else if (hasLongOption(token))
            {
                handleLongOption(token);
            }

            // -E, -EV, -E=V
            else if (hasExtendedShortOption(token))
            {
                handleExtendedShortOption(token);
            }

            // -S, -SV, -S=V -S1S2, -S1S2V, or S1S2=V
            else if (hasShortOption(token))
            {
                handleShortOption(token);
            }

            else
            {
                // System.out.format("Token1: %s\tToken2: %s%n", token, iter.peek());
                processRemainingArgument(token);
            }

            /*
             * Integrity checks
             */
            if (activeFlagView != null && activeFlagView.expectsArgument())
            {
                // Handles last option with a missing argument
                if (isOption(token) && token.equals(arguments.get(arguments.size() - 1)))
                {
                    throw new ParseException("Last option [" + activeFlagView.getOptionName() + "] is missing an argument.", 0);
                }

                else if (activeFlagView.expectsValueSeparator() && !activeFlagView.existsSeparator() && !iter.peek().isEmpty() && !iter.peek().contains("="))
                {
                    throw new ParseException("Option [" + activeFlagView.getOptionName() + "] is missing a value separator ('=')", 0);
                }

                else if (activeFlagView.hasMultipleValues() && !activeFlagView.expectsValueSeparator())
                {
                    throw new ParseException("Option [" + activeFlagView.getOptionName() + "] does not support multiple values separated by commas", 0);
                }

                else if (!activeFlagView.hasValueAssigned() && !iter.peek().isEmpty() && isOption(iter.peek()))
                {
                    if (activeFlagView.expectsValueSeparator())
                    {
                        throw new ParseException("Option [" + activeFlagView.getOptionName() + "] is missing at least one argument.", 0);
                    }

                    else
                    {
                        throw new ParseException("Option [" + activeFlagView.getOptionName() + "] is missing an argument.", 0);
                    }
                }

                else if (valueRange && !iter.peek().isEmpty() && !iter.peek().contains(","))
                {
                    activeFlagView.setOptionDone();
                    activeFlagView = null;
                    valueRange = false;
                }
            }
        }

        if (debug)
        {
            System.out.println(this);
        }

        validateRequiredOptions();
        validateArgumentCount();
    }

    /**
     * Returns a detailed status report of all options that have been processed, including any
     * values set from the command line.
     * 
     * This method is useful for debugging purposes, providing a comprehensive overview of the
     * options and their values.
     *
     * @return A human-readable string representation of the status information
     */
    @Override
    public String toString()
    {
        StringBuilder dumpInfo = new StringBuilder();

        dumpInfo.append(String.format("%-12s%n", "[Flattened]"));
        dumpInfo.append(String.format("   %s%n%n", flattenArguments()));

        // Print flag mapping list
        dumpInfo.append(String.format("%-12s%n", "[Flag mapping list]"));

        for (FlagOptionRule rule : flagRuleMap.values())
        {
            if (rule.isOptionDone() || rule.hasValueAssigned())
            {
                dumpInfo.append(rule);
                dumpInfo.append(System.lineSeparator());
            }
        }

        // Print standalone arguments
        if (standaloneTokens.size() > 0)
        {
            dumpInfo.append(String.format("%n%-12s%n", "[Stand-alone arguments]"));

            for (String str : standaloneTokens)
            {
                dumpInfo.append(String.format("  Argument:   %s%n", str));
            }
        }

        // Print a separator line
        dumpInfo.append(Generic.repeatPrint("-", 80));

        return dumpInfo.toString();
    }
}