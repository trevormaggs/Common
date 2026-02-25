package common;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * <p>
 * This is a stand-alone command-line parser designed to retrieve flags or options and arguments
 * from the command line, process them, and feed them to any Java applications as data inputs. Short
 * (-S), extended short (-E), and long (--L) option types are supported.
 * </p>
 *
 * <p>
 * The operation involves parsing raw tokens from the command line and splitting them into
 * constituent parts to represent options and arguments. Each option being handled is checked to see
 * if it is identified with the {@link optionRule} object for compliance. If the option is not
 * present in the rule, it will be treated as unknown.
 * </p>
 *
 * <p>
 * You must define the rules for the options using the {@link addRule} method before the parsing can
 * begin. When defining the rules for the options, there are 5 possibilities.
 * </p>
 *
 * <table>
 * <caption>Rule Definitions</caption>
 * <tr>
 * <th>Rule</th>
 * <th>Meaning</th>
 * </tr>
 * <tr>
 * <td>ARG_BLANK</td>
 * <td>Stand-alone option. This is optional</td>
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
 * <p>
 * Note that the last 2 rules will also recognise comma-separated arguments and the parser will
 * build a list of values that can be retrieved. For example, -value=12,24,36 etc.
 * </p>
 *
 * <p>
 * <b>Some examples include the following:</b>
 * </p>
 *
 * <ul>
 * <li>cmd -g vulfeed.dat</li>
 * <li>cmd --csv</li>
 * <li>cmd -path /var/log/value.log</li>
 * <li>cmd --depth=7</li>
 * <li>cmd -b=3</li>
 * <li>cmd -ofile.txt (concatenated token -o = option, file.txt = argument)</li>
 * <li>cmd -hv (concatenated stand-alone short options)</li>
 * </ul>
 *
 * <p>
 * This parser will recognise concatenated tokens, for example {@code -d787} where -d is the short
 * option and 787 is the value. {@code -avb=747} where -a and -v are two stand-alone options and -b
 * option is assigned with the value of 747.
 * </p>
 *
 * <p>
 * <b>Change logs:</b>
 * </p>
 *
 * <ul>
 * <li>Developed by Trevor Maggs - 6 February 2018</li>
 * <li>Improved logic in the handling of short options - 2 July 2018</li>
 * <li>Class renamed from CommandParser and added support to enforce value separator for options and
 * improved logic in several methods - 23 August 2021</li>
 * </ul>
 *
 * @author Trevor Maggs
 * @version 0.3
 * @since 23 August 2021
 */
public final class CommandLineParser
{
    // Public constants to define the rule types
    public static final int ARG_BLANK = 1;
    public static final int ARG_REQUIRED = 2;
    public static final int ARG_OPTIONAL = 3;
    public static final int SEP_REQUIRED = 4;
    public static final int SEP_OPTIONAL = 5;

    private final Map<String, optionRule> optionRuleMap;
    private final List<String> lefoverArgList;
    private final List<String> requiredOpt;
    private optionRule currentOpt;
    private String[] arguments;
    private int maxArgCount;

    /**
     * <p>
     * An instance of this class is created internally to define an option rule, which describes a
     * single command-line flag only.
     * </p>
     *
     * <p>
     * It stores information regarding the option name and type, i.e., short option, long option, or
     * extended short option, as well as providing an ability to query whether a flag expects an
     * argument or value.
     * </p>
     *
     * <p>
     * <b>Note:</b> once an {@link optionRule} instance is created, the required flag cannot be
     * modified. Each instance is actually created by the parent class {@link CommandLineParser}
     * only and must have at least a short or a long option name and the option type.
     * </p>
     */
    private static class optionRule
    {
        final private int optType;
        final private boolean longOpt;
        final private String optName;
        final private List<String> values;
        private boolean handled;
        private boolean separator;
        private boolean valueRange;

        /**
         * Internal constructor to create an option instance and register the option name and option
         * rule type.
         * 
         * @param f
         *        the command option name (flag) prefixed by either a single-dash character for a
         *        short option (e.g., -v or -debug) or a double-dash character for a long option
         *        (e.g., --csv)
         * @param t
         *        the option rule type. You must use one of the public constants defined in the
         *        enclosing parent class
         * 
         * @throws ParseException
         *         if the specified option contains any non-valid characters
         */
        private optionRule(String f, int t) throws ParseException
        {
            optName = f;
            optType = t;
            longOpt = f.startsWith("--");
            values = new ArrayList<>();

            if (optName.matches("\\-{1,2}[^\\-].*$") == false)
            {
                throw new ParseException("Command option [" + optName + "] is unrecognised.", 0);
            }

            char[] ch = optName.toCharArray();

            for (int i = 1; i < ch.length; i++)
            {
                if (longOpt && i == 1)
                {
                    continue;
                }

                if ((Character.isJavaIdentifierPart(ch[i]) || ch[i] == '?' || ch[i] == '@') == false)
                {
                    throw new ParseException("Command option [" + optName + "] contains an illegal character [" + ch[i] + "].", 0);
                }
            }
        }

        /**
         * Returns the name of this option.
         * 
         * @return the name of this option
         */

        private String getOptionName()
        {
            return optName;
        }

        /**
         * Returns the type of this option.
         * 
         * @return the type of this option as an integer value
         */
        private int getOptionType()
        {
            return optType;
        }

        /**
         * Adds the argument or value to this command option.
         * 
         * @param val
         *        the value to be associated with this command option
         */
        private void addValue(String val)
        {
            values.add(val);
        }

        /**
         * Sets a flag indicating that this option has been handled, including any associated
         * arguments, if required.
         */
        private void setOptionDone()
        {
            handled = true;
        }

        /**
         * Resets this option for processing. When the program captures an option in the command
         * line, it is instructed to either expect an argument to be added if it exists or be marked
         * as a stand-alone flag only, which does not require an argument. This method must be
         * called before the option can be handled.
         */
        private void resetOption()
        {
            handled = false;
            separator = false;
        }

        /**
         * Queries whether this option has been handled or not.
         * 
         * @return true if the handling of this option has been completed, false otherwise
         */
        private boolean isOptionDone()
        {
            return handled;
        }

        /**
         * Queries whether this option requires at least one argument.
         * 
         * @return true if an argument is expected, false otherwise
         */
        private boolean expectsArgument()
        {
            return (optType == ARG_REQUIRED || optType == ARG_OPTIONAL || optType == SEP_REQUIRED || optType == SEP_OPTIONAL);
        }

        /**
         * Queries whether this option is a mandatory flag.
         * 
         * @return true if this option is required, false otherwise
         */
        private boolean isRequired()
        {
            return (optType == ARG_REQUIRED || optType == SEP_REQUIRED);
        }

        /**
         * Queries whether this option is bound by the value separator type rule.
         * 
         * @return true if the value separator is required, false otherwise
         */
        private boolean expectsValueSeparator()
        {
            return (optType == SEP_REQUIRED || optType == SEP_OPTIONAL);
        }

        /**
         * Configures this option to expect a value after encountering a {@code SEP_REQUIRED} or
         * {@code SEP_OPTIONAL} flag.
         */
        private void configureSeparator()
        {
            separator = true;
        }

        /**
         * Queries whether a value separator is assigned to this option.
         * 
         * @return true if a value separator is currently assigned to this option, false otherwise
         */
        private boolean existsSeparator()
        {
            return separator;
        }

        /**
         * Asserts that this option has encountered a comma-separated range of values on the command
         * line.
         */
        private void setValueRange()
        {
            valueRange = true;
        }

        /**
         * Queries to determine whether this option binds with a range of comma-separated arguments.
         *
         * @return boolean true if a range of arguments or values has been assigned to this option
         */
        private boolean hasValueRange()
        {
            return valueRange;
        }

        /**
         * Returns true if the specified flag is a short option type.
         * 
         * @return boolean true if the flag is a short option
         */
        private boolean isShortOption()
        {
            return (longOpt == false);
        }

        /**
         * Returns true if the specified flag is an extended short option type.
         * 
         * @return boolean true if the flag is an extended short option
         */
        private boolean isExtendedShortOption()
        {
            return (longOpt == false && optName.length() > 2);
        }

        /**
         * Returns true if the specified flag is a long option type.
         * 
         * @return boolean true if the flag is a long option
         */
        private boolean isLongOption()
        {
            return longOpt;
        }

        /**
         * Returns information pertaining to this option, useful for debugging.
         * 
         * @return dump information in string form
         */
        @Override
        public String toString()
        {
            return String.format("  Type: %-3s %-8s Option: %-10s Values: %s", optType, (handled ? "Set" : "Not set"), optName, StringJoiner.join(", ", values));
        }
    }

    /**
     * A simple comparator class designed to sort the option names stored in the
     * {@link optionRuleMap} instance. The ordering is important to achieve the desired read
     * sequence in a descending order.
     */
    private static class KeyComparator implements Comparator<String>
    {
        /**
         * Compares 2 string objects for equality.
         * 
         * @param o1
         *        the first string item
         * @param o2
         *        the second string item
         */
        public int compare(String o1, String o2)
        {
            return o2.compareTo(o1);
        }
    }

    /**
     * Creates a new default instance and initialises member collection variables.
     */
    public CommandLineParser()
    {
        maxArgCount = 0;
        requiredOpt = new ArrayList<>();
        lefoverArgList = new ArrayList<>();
        optionRuleMap = new TreeMap<>(new KeyComparator());
    }

    /**
     * Main constructor used to create a new instance and read the command line arguments.
     * 
     * @param arguments
     *        the command line arguments
     */
    public CommandLineParser(String[] arguments)
    {
        this();
        setArguments(arguments);
    }

    /**
     * Checks if the token is a valid long option.
     * 
     * @param token
     *        the entry extracted from the command line
     * 
     * @return true if the long option is identified
     */
    private static boolean isLongOption(final String token)
    {
        return (token.matches("\\--[^\\-].*$") && token.length() > 2);
    }

    /**
     * Queries whether the token is seen as an extended short option. Basically, it is the same as
     * the short option, except for that the option name can be more than one character that begins
     * with a single leading dash, i.e. -E. Any value that is present at the end of the option name
     * is supported, i.e. -EV or -E=V.
     * 
     * @param token
     *        the entry extracted from the command line
     * 
     * @return true if the extended short option is identified
     */
    private static boolean isExtendedShortOption(final String token)
    {
        return (token.matches("\\-[^\\-].*$") && token.length() > 2);
    }

    /**
     * Checks the token is a valid short option. Essentially, the short option name is a single
     * character prefixed with a single leading dash, i.e, -S. However, it is also possible to have
     * several concatenated short options. In other words, multiple short options may be present in
     * a single token string i.e. -S1S2S3. Also, any value that is present at the end of the token
     * that follows the last option will be processed, i.e. -SV, -S=V, -S1S2V or -S1S2=V.
     * 
     * @param token
     *        the entry extracted from the command line
     * 
     * @return true if the short option is identified
     */
    private static boolean isShortOption(final String token)
    {
        return (token.matches("\\-[^\\-].*$") && token.length() > 1);
    }

    /**
     * Checks if the token is a valid command option.
     *
     * @param token
     *        the entry extracted from the command line
     * 
     * @return true if the command option is identified
     */
    private static boolean isOption(final String token)
    {
        return (isLongOption(token) || isShortOption(token) || isExtendedShortOption(token));
    }

    /**
     * Checks if the specified token is a valid argument.
     *
     * @param token
     *        the entry extracted from the command line
     * 
     * @return true if the argument is valid
     */
    private static boolean isArgument(final String token)
    {
        return (isOption(token) == false || isNegativeNumber(token));
    }

    /**
     * Checks if the specified token is identified as a negative number.
     *
     * @param token
     *        the entry extracted from the command line
     * 
     * @return true if the token is a valid negative number, otherwise false
     */
    private static boolean isNegativeNumber(final String token)
    {
        try
        {
            Double.parseDouble(token);

            return true;
        }

        catch (final NumberFormatException e)
        {
            return false;
        }
    }

    /**
     * Trims the dashes from the beginning of the specified token.
     *
     * @param token
     *        the entry extracted from the command line
     * 
     * @return the string from which the dashes are removed
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
     * An exception is thrown if the value separator is present for the current option being seen,
     * whose entry is not permitted according to the rule that is preset to disallow it.
     * 
     * @param rule
     *        is the optionRule object
     * 
     * @throws ParseException
     *         if the illegal value separator is present in the specified flag
     */
    private static void checkIllegalValueSeparator(final optionRule rule) throws ParseException
    {
        if ((rule.getOptionType() == SEP_REQUIRED | rule.getOptionType() == SEP_OPTIONAL) == false)
        {
            throw new ParseException("The value separator ('=') is not permitted in flag [" + rule.getOptionName() + "]. Check the command line arguments.", 0);
        }
    }

    /**
     * An exception is thrown if the value separator is missing in the specified token. The
     * user-defined option rule is checked to determine whether the value separator is required.
     * 
     * @param rule
     *        is the optionRule object
     * 
     * @throws ParseException
     *         if the value separator is missing from the specified flag
     */
    private static void checkMissingValueSeparator(final optionRule rule) throws ParseException
    {
        if (rule.getOptionType() == SEP_REQUIRED | rule.getOptionType() == SEP_OPTIONAL)
        {
            throw new ParseException("The value separator ('=') is missing in flag [" + rule.getOptionName() + "]. Check the command line arguments.", 0);
        }
    }

    /**
     * Queries whether the specified token is identified with the {@link optionRule} instance as a
     * valid long command option.
     *
     * Possible long option combinations with or without concatenations as listed below are
     * supported.
     * 
     * <ul>
     * <li>--L</li>
     * <li>--LV</li>
     * <li>--L=V</li>
     * </ul>
     * 
     * @param token
     *        the entry extracted from the command line
     * 
     * @return true if the {@link optionRule} instance is present
     */
    private boolean hasLongOption(final String token)
    {
        String opt = (token.contains("=")) ? token.substring(0, token.indexOf("=")) : token;

        if (isLongOption(opt))
        {
            for (optionRule map : optionRuleMap.values())
            {
                if (map.isLongOption() && opt.startsWith(map.getOptionName()))
                {
                    // Make sure the exact option descriptor is captured.
                    // ie --var=99 is not the same as --vars=99
                    if (opt.equals(token) || opt.equals(map.getOptionName()))
                    {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * Queries whether the specified token is identified with the {@link optionRule} instance as a
     * valid extended short option. Possible extended short option combinations with or without
     * concatenations as listed below are supported.
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
     *        the entry extracted from the command line
     * 
     * @return true if the {@link optionRule} instance is present
     */
    private boolean hasExtendedShortOption(final String token)
    {
        String opt = (token.contains("=")) ? token.substring(0, token.indexOf("=")) : token;

        /*
         * -E, -EV
         */
        if (isExtendedShortOption(opt))
        {
            for (optionRule map : optionRuleMap.values())
            {
                if (map.isExtendedShortOption() && opt.startsWith(map.getOptionName()))
                {
                    if (opt.equals(token) || opt.equals(map.getOptionName()))
                    {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * Queries whether the specified token is identified with the {@link optionRule} instance as a
     * valid short option. Possible short option combinations with or without concatenations as
     * listed below are supported.
     *
     * <ul>
     * <li>-S</li>
     * <li>-SV</li>
     * <li>-S=V</li>
     * <li>-S1S2</li>
     * <li>-S1S2V</li>
     * <li>-S1S2=V</li>
     * </ul>
     *
     * @param token
     *        the entry extracted from the command line
     * 
     * @return true if the {@link optionRule} instance is present
     */
    private boolean hasShortOption(final String token)
    {
        String opt = (token.contains("=")) ? token.substring(0, token.indexOf("=")) : token;

        if (isShortOption(opt))
        {
            String key = opt.substring(1);

            // For a perfect match (-S)
            if (optionRuleMap.containsKey(key))
            {
                return (optionRuleMap.get(key).isShortOption());
            }

            // For a match with concatenations (-SV or -S1S2 or -S1S2V)
            for (optionRule map : optionRuleMap.values())
            {
                if (map.isShortOption() && opt.startsWith(map.getOptionName()))
                {
                    if (opt.equals(token) || opt.equals(map.getOptionName()))
                    {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * Processes possible token combinations listed below representing a type of long option. A
     * value may be concatenated or delimited by a separator in the given token. It handles long
     * options with or without a separator.
     * 
     * <ul>
     * <li>--L</li>
     * <li>--LV</li>
     * <li>--L=V</li>
     * <li>--L=V1,V2,V3</li>
     * </ul>
     * 
     * <p>
     * <b>Examples:</b> {@code --csv, --value747, --fleet=A380}
     * </p>
     * 
     * @param token
     *        the entry extracted from the command line
     * 
     * @throws ParseException
     *         if there are any problems encountered while attempting to parse the command line
     *         token
     */
    private void processLongOption(final String token) throws ParseException
    {
        if (token.contains("="))
        {
            processOptionWithSeparator(token);
        }

        else
        {
            processLongOptionWithoutSeparator(token);
        }
    }

    /**
     * Processes possible token combinations listed below representing a type of extended short
     * option. A value may be concatenated or delimited by a separator in the string. It can handle
     * both with or without a separator.
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
     *        the entry extracted from the command line
     * 
     * @throws ParseException
     *         if there are any problems encountered while attempting to parse the command line
     *         token
     */
    private void processExtendedShortOption(final String token) throws ParseException
    {
        if (token.contains("="))
        {
            processOptionWithSeparator(token);
        }

        else
        {
            processExtendedShortOptionWithoutSeparator(token);
        }
    }

    /**
     * Processes possible token combinations listed below representing a type of short option. A
     * value may be concatenated or delimited by a separator in the string. It can handle both with
     * or without a separator.
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
     * <p>
     * <b>Examples:</b> {@code -b, -v7, -v=53, -ab, -nv8, -nv=5, -range=12,24,36,48}
     * </p>
     *
     * @param token
     *        the entry extracted from the command line
     * 
     * @throws ParseException
     *         if there are any problems encountered while attempting to parse the command line
     *         token
     */
    private void processShortOption(final String token) throws ParseException
    {
        String key = stripLeadingDashes(token);

        // For a perfect match (-S)
        if (optionRuleMap.containsKey(key))
        {
            setCurrentOption(optionRuleMap.get(key));
        }

        // For a match with concatenations, (-SV, -S=V, -S1S2S3, -S1S2V, -S1S2=V or -S=V1,V2,V3)
        else
        {
            // Start by skipping the leading single dash "-"
            for (int i = 1; i < token.length(); i++)
            {
                key = String.valueOf(token.charAt(i));

                if (optionRuleMap.containsKey(key))
                {
                    setCurrentOption(optionRuleMap.get(key));

                    if (currentOpt.expectsArgument())
                    {
                        if ((i + 1) < token.length())
                        {
                            String value = token.substring(i + 1);

                            if (value.charAt(0) == '=')
                            {
                                checkIllegalValueSeparator(currentOpt);
                                currentOpt.configureSeparator();

                                value = value.substring(1);

                                if (value.length() > 0)
                                {
                                    if (value.contains(","))
                                    {
                                        // -S=V1,V2,V3
                                        splitValues(value);
                                    }

                                    else
                                    {
                                        // -S=V
                                        updateOpt(value);
                                    }
                                }
                            }

                            else
                            {
                                // -SV
                                checkMissingValueSeparator(currentOpt);
                                updateOpt(value);
                            }
                        }

                        break;
                    }

                    else
                    {
                        updateOpt();
                    }
                }

                else
                {
                    processUnknownArgument("-" + key);
                }
            }
        }
    }

    /**
     * Processes the extended short and long options listed below with a separator. A value is
     * expected to be present following the separator.
     * 
     * <ul>
     * <li>-E=V</li>
     * <li>-E=V1,V2,V3</li>
     * <li>--L=V</li>
     * <li>--L=V1,V2,V3</li>
     * </ul>
     * 
     * @param token
     *        the entry extracted from the command line
     * 
     * @throws ParseException
     *         if there are any problems encountered while attempting to parse the command line
     *         token
     */
    private void processOptionWithSeparator(final String token) throws ParseException
    {
        String[] items = token.split("=", 2);
        String opt = items[0];
        String value = items[1];

        setCurrentOption(optionRuleMap.get(stripLeadingDashes(opt)));

        checkIllegalValueSeparator(currentOpt);
        currentOpt.configureSeparator();

        if (currentOpt.expectsArgument())
        {
            if (value.length() > 0)
            {
                if (value.contains(","))
                {
                    splitValues(value);
                }

                else
                {
                    updateOpt(value);
                }
            }
        }

        else if (value.isEmpty() == false)
        {
            processUnknownArgument(value);
        }
    }

    /**
     * Processes the long options without a separator. A value may be concatenated in the input
     * string. The following examples are supported.
     *
     * <ul>
     * <li>--L</li>
     * <li>--LV</li>
     * </ul>
     * 
     * @param token
     *        the entry extracted from the command line
     * 
     * @throws ParseException
     *         if there are any problems encountered while attempting to parse the command line
     *         token
     */
    private void processLongOptionWithoutSeparator(final String token) throws ParseException
    {
        for (optionRule map : optionRuleMap.values())
        {
            if (map.isLongOption() && token.startsWith(map.getOptionName()))
            {
                String value = token.substring(token.indexOf(map.getOptionName()) + map.getOptionName().length());

                // --L
                setCurrentOption(map);

                // --LV
                if (currentOpt.expectsArgument())
                {
                    if (value.length() > 0)
                    {
                        if (value.contains(","))
                        {
                            splitValues(value);
                        }

                        else
                        {
                            updateOpt(value);
                        }
                    }
                }

                else if (value.isEmpty() == false)
                {
                    processUnknownArgument(value);
                }

                break;
            }
        }
    }

    /**
     * Processes the extended short options without a separator. A value may be concatenated in the
     * input string. The following examples are supported.
     * 
     * <ul>
     * <li>-E</li>
     * <li>-EV</li>
     * </ul>
     * 
     * @param token
     *        the entry extracted from the command line
     * 
     * @throws ParseException
     *         if there are any problems encountered while attempting to parse the command line
     *         token
     */
    private void processExtendedShortOptionWithoutSeparator(final String token) throws ParseException
    {
        for (optionRule map : optionRuleMap.values())
        {
            if (map.isExtendedShortOption() && token.startsWith(map.getOptionName()))
            {
                String value = token.substring(token.indexOf(map.getOptionName()) + map.getOptionName().length());

                // -E
                setCurrentOption(map);

                // -EV
                if (currentOpt.expectsArgument())
                {
                    if (value.length() > 0)
                    {
                        if (value.contains(","))
                        {
                            splitValues(value);
                        }

                        else
                        {
                            updateOpt(value);
                        }
                    }
                }

                else if (value.isEmpty() == false)
                {
                    processUnknownArgument(value);
                }

                break;
            }
        }
    }

    /**
     * Processes an unknown token. An exception is thrown if the unrecognised token starts with a
     * dash. Otherwise the token is added to the list of unknown arguments.
     * 
     * @param token
     *        the entry extracted from the command line
     * 
     * @throws ParseException
     *         if the token is seen as unrecognised
     */
    private void processUnknownArgument(final String token) throws ParseException
    {
        if (token.length() > 0)
        {
            if (token.startsWith("-") && token.length() > 1)
            {
                throw new ParseException("Option [" + token + "] is unrecognisesd.", 0);
            }

            lefoverArgList.add(token);
        }
    }

    /**
     * Splits the comma-separated string into constituent parts and adds to the list of values for
     * the current option.
     * 
     * @param str
     *        the comma-separated string to be split
     */
    private void splitValues(final String str)
    {
        currentOpt.setValueRange();

        for (String val : str.split(","))
        {
            if (val.length() > 0)
            {
                updateOpt(val);
            }
        }
    }

    /**
     * <p>
     * Refreshes the current option being seen for processing. When the program parses an option, it
     * is instructed to either expect an argument to be retrieved or get marked as being completed
     * without arguments, depending on the state of the {@link optionRule} object, which it is
     * defined for.
     * </p>
     * 
     * <p>
     * The argument is required only if the rule expects the option to receive it. An exception will
     * be thrown if the argument is absent. In contrast, if the rule states that no argument is
     * required, the option will be treated as a stand-alone flag. Either way, it must be called
     * before the option is handled.
     * </p>
     * 
     * @param currentOption
     *        the optionRule instance to indicate the current option
     */
    private void setCurrentOption(final optionRule currentOption)
    {
        currentOpt = currentOption;
        currentOpt.resetOption();
    }

    /**
     * Updates the {@link optionRule} object, indicating the specified option has been handled.
     */
    private void updateOpt()
    {
        currentOpt.setOptionDone();

        if (currentOpt.isRequired())
        {
            requiredOpt.remove(currentOpt.getOptionName());
        }
    }

    /**
     * Updates the {@link optionRule} object to process the specified value being read from the
     * command line.
     * 
     * @param value
     *        the argument being added to the current option
     */
    private void updateOpt(String value)
    {
        updateOpt();
        currentOpt.addValue(value);
    }

    /**
     * Throws an exception if the number of left-over arguments has exceeded the maximum allowable
     * number of arguments if defined by the client.
     * 
     * If the required maximum number is not set, no exception will be thrown.
     *
     * @throws ParseException
     *         if the actual number of arguments that are not associated with any options exceeds
     *         the required number of arguments
     */
    private void checkArgumentCount() throws ParseException
    {
        if (maxArgCount > 0 && lefoverArgList.size() > maxArgCount)
        {
            throw new ParseException("You can only have " + maxArgCount + " argument" + (maxArgCount > 1 ? "s" : "") + ". Found " + lefoverArgList + ".", 0);
        }
    }

    /*
     * PUBLIC API METHODS FOR EXPORTING.
     */

    /**
     * Sets the command line arguments.
     * 
     * @param args
     *        the array of options and arguments
     */
    public void setArguments(final String[] args)
    {
        arguments = args;
    }

    /**
     * Joins the entire tokens and flattens them to create a single long string representation of
     * the command line arguments.
     * 
     * @return the flattened string with individual tokens separated by one whitespace
     */
    public String flattenArguments()
    {
        return StringJoiner.join(" ", arguments, "[", "]");
    }

    /**
     * Defines command option rules to control how each option should be expected to respond and
     * behave, depending on the requirement of the client Java applications. It is best to learn by
     * examples below.
     * 
     * <pre>
     * Define -x flag accompanied by a value or argument. This option is mandatory.
     *     addRule("-x", CommandLineParser.ARG_REQUIRED);
     * 
     * Define standalone flag --verbose without a value. This option is optional.   
     *     addRule("--verbose", CommandLineParser.ARG_BLANK);
     * 
     * Define optional long flag that expects an argument. This option is optional.
     *     addRule("--depth", CommandLineParser.ARG_OPTIONAL);
     *     
     * Define extended short flag with an argument delimited by a separator. This option is mandatory. 
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
    public void addRule(final String opt, final int type) throws ParseException
    {
        optionRuleMap.put(stripLeadingDashes(opt), new optionRule(opt, type));

        if (type == ARG_REQUIRED || type == SEP_REQUIRED)
        {
            requiredOpt.add(opt);
        }
    }

    /**
     * Sets the maximum number of arguments that the client application can have.
     *
     * @param count
     *        the number of arguments
     */
    public void setMaximumArgumentCount(final int count)
    {
        maxArgCount = count;
    }

    /**
     * Queries whether all of the required options are present and ready for use.
     * 
     * @return boolean true if the required options are present
     * 
     * @throws ParseException
     *         if any of the remaining options have not been processed yet. The message prints a
     *         comma-separated string representation of any missing flag names in the console
     */
    public boolean hasRequiredOptions() throws ParseException
    {
        if (requiredOpt.isEmpty() == false)
        {
            Separator sep = new Separator(", ");
            StringBuilder sb = new StringBuilder();

            sb.append("Missing required option");
            sb.append(requiredOpt.size() > 1 ? "s" : "");
            sb.append(": [");

            for (String opt : requiredOpt)
            {
                sb.append(sep).append(opt);
            }

            sb.append("]");

            throw new ParseException(sb.toString(), 0);
        }

        return true;
    }

    /**
     * Queries to find out whether the specified option has been seen in the command line.
     * 
     * <p>
     * Example: the following snippet checks if the {@code -f} option exists
     * </p>
     * 
     * <pre>
     * <tt>
     * if (cli.existsOption("-f"))
     * {
     *     // Do something here   
     * }
     * </tt>
     * </pre>
     * 
     * @param optName
     *        the command option name
     * 
     * @return boolean true if the option exists, otherwise false
     */
    public boolean existsOption(final String optName)
    {
        String key = stripLeadingDashes(optName);

        if (optionRuleMap.containsKey(key))
        {
            return optionRuleMap.get(key).isOptionDone();
        }

        return false;
    }

    /**
     * Returns the number of values or items held by the specified option.
     * 
     * @param optName
     *        the command option name
     * 
     * @return the number of items or zero if the specified option is unknown
     */
    public int getValueSize(final String optName)
    {
        if (existsOption(optName))
        {
            return optionRuleMap.get(stripLeadingDashes(optName)).values.size();
        }

        return 0;
    }

    /**
     * Returns the value of the specified option descriptor if it exists, otherwise an empty string
     * is returned.
     * 
     * <p>
     * Example: the following snippet retrieves the value of the {@code -f} option.
     * </p>
     * 
     * <pre>
     * <tt>
     *   System.out.println(cli.getValueByOption("-f"));
     * </tt>
     * </pre>
     * 
     * @param optName
     *        the command option name
     * 
     * @return the value of the specified option name or a blank string if there is no value or
     *         argument
     */
    public String getValueByOption(final String optName)
    {
        return getValueByOption(optName, 0);
    }

    /**
     * Returns the value of the specified option descriptor by its position in the list, where the
     * information is held, otherwise an empty string is returned.
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
    public String getValueByOption(final String optName, final int index) throws IndexOutOfBoundsException
    {
        return (existsOption(optName) ? optionRuleMap.get(stripLeadingDashes(optName)).values.get(index) : "");
    }

    /**
     * Returns the number of left-over arguments without being associated with flags in the command
     * line.
     *
     * @return number of left-over arguments being passed on to the application
     */
    public int getStandaloneArgumentCount()
    {
        return lefoverArgList.size();
    }

    /**
     * Returns the first instance of the left-over arguments from the list.
     *
     * @return the first left-over argument
     */
    public String getFirstStandaloneArgument()
    {
        return getStandaloneArgumentByIndex(0);
    }

    /**
     * Returns the last instance of the left-over arguments from the list.
     *
     * @return the last left-over argument
     */
    public String getLastStandaloneArgument()
    {
        return getStandaloneArgumentByIndex(lefoverArgList.size() - 1);
    }

    /**
     * Returns the left-over argument from the specified position of the list.
     * 
     * @param k
     *        position number or index of the list to be retrieved
     * 
     * @return the left-over argument extracted from the specified position
     */
    public String getStandaloneArgumentByIndex(final int k)
    {
        return (k < 0 && k > lefoverArgList.size() ? "" : lefoverArgList.get(k));
    }

    /**
     * Parses the command line flags and arguments and checks against the user-defined option rules
     * for compliance. Any unknown tokens that are encountered will be captured in a simple list and
     * can be passed on to the client application for handling if necessary.
     * 
     * @throws ParseException
     *         if there are problems while parsing command line tokens
     */
    public void parse() throws ParseException
    {
        PeekingIterator<String> iter;

        if (optionRuleMap.size() == 0)
        {
            throw new ParseException("You must define rules for the command line options before the parsing can begin", 0);
        }

        if (arguments == null || arguments.length == 0)
        {
            throw new ParseException("Command arguments are empty", 0);
        }

        iter = new PeekingIterator<String>(arguments);

        while (iter.hasNext())
        {
            String token = iter.next();

            if (currentOpt != null && currentOpt.expectsArgument())
            {
                if (isArgument(token))
                {
                    if (token.contains("="))
                    {
                        checkIllegalValueSeparator(currentOpt);
                        currentOpt.configureSeparator();

                        if (token.length() > 1)
                        {
                            token = token.substring(1);
                        }

                        else
                        {
                            continue;
                        }
                    }

                    if (currentOpt.existsSeparator() == false)
                    {
                        checkMissingValueSeparator(currentOpt);
                    }

                    if (token.contains(","))
                    {
                        splitValues(token);
                    }

                    else
                    {
                        updateOpt(token);
                    }
                }

                else
                {
                    throw new ParseException("Option [" + currentOpt.getOptionName() + "] expects an argument.", 0);
                }
            }

            // --L, --LV, --L=V
            else if (hasLongOption(token))
            {
                processLongOption(token);
            }

            // -E, -EV, -E=V
            else if (hasExtendedShortOption(token))
            {
                processExtendedShortOption(token);
            }

            // -S, -SV, -S=V -S1S2, -S1S2V, or S1S2=V
            else if (hasShortOption(token))
            {
                processShortOption(token);
            }

            else
            {
                processUnknownArgument(token);
            }

            if (currentOpt != null)
            {
                // Handles when current option does not require any arguments
                if (currentOpt.expectsArgument() == false)
                {
                    updateOpt();
                    currentOpt = null;
                }

                // Expects more comma-separated values to be added
                else if (currentOpt.hasValueRange() && iter.peek().length() > 0 && isArgument(iter.peek()))
                {
                    continue;
                }

                // When current option has been fully handled, except for unresolved errors
                else if (currentOpt.isOptionDone())
                {
                    if (currentOpt.expectsValueSeparator() && currentOpt.existsSeparator() == false)
                    {
                        throw new ParseException("Option [" + token + "] should contain a value separator ('=').", 0);
                    }

                    else if (currentOpt.hasValueRange() && currentOpt.expectsValueSeparator() == false)
                    {
                        throw new ParseException("Option [" + currentOpt.getOptionName() + "] cannot have multiple values separated by a comma.", 0);
                    }

                    currentOpt = null;
                }

                // Handles last option with missing arguments
                else if (isOption(token) && token.equals(arguments[arguments.length - 1]))
                {
                    throw new ParseException("Last option [" + currentOpt.getOptionName() + "] is missing an argument.", 0);
                }
            }
        }

        checkArgumentCount();
    }

    /**
     * Returns status information regarding all options that have been received and handled,
     * including any values that are set in the command line. This is good for debugging purposes.
     * 
     * @return dump information in string form
     */
    @Override
    public String toString()
    {
        StringBuilder line = new StringBuilder(String.format("%-12s%n", "[Flag mapping list]"));

        for (optionRule rule : optionRuleMap.values())
        {
            if (rule.handled || rule.values.size() > 0)
            {
                line.append(rule);
                line.append(System.lineSeparator());
            }
        }

        if (lefoverArgList.size() > 0)
        {
            line.append(String.format("%n%-12s%n", "[Standalone arguments]"));

            for (String str : lefoverArgList)
            {
                line.append(String.format("  Argument:   %s%n", str));
            }
        }

        line.append(Generic.repeatPrint("-", 80));

        return line.toString();
    }
}