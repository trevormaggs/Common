package common.clireader;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import common.PeekingIterator;

/**
 * The core orchestrator for command-line parsing.
 * 
 * <p>
 * This class manages the life-cycle of a command-line parse through three distinct phases:
 * </p>
 * 
 * <ol>
 * <li><b>Definition:</b> Registering {@link FlagRule objects via {@link #addDefinition}.</li>
 * <li><b>Tokenization:</b> Breaking the raw input into manageable components.</li>
 * <li><b>Execution:</b> Processing tokens using poly-morphic {@link FlagHandler} handlers.</li>
 * </ol>
 * 
 * <p>
 * The engine follows POSIX conventions for short flags (e.g., {@code -vh}) and GNU conventions for
 * long flags (e.g., {@code --verbose}). It supports clustered short flags, attached values, and
 * explicit value separators ({@code =}).
 * </p>
 *
 * @author Trevor Maggs
 * @version 2.0
 * @since 28 February 2026
 */
public class FlagEngine
{
    private final String[] rawArgs;
    private final FlagRegistry registry;
    private final FlagHandler longHandler;
    private final FlagHandler shortHandler;
    private final List<String> operands;
    private boolean debug;
    private int maxOperands = 1;

    private final FlagListener observer = new FlagListener()
    {
        /**
         * Using the Observer pattern to decouple token parsing from state management. This allows
         * handlers to report multiple discoveries, for example: clustered flags, during a single
         * invocation without managing internal collection state.
         */
        @Override
        public void onDiscovery(String name, String value) throws ParseException
        {
            if (value == null)
            {
                registry.acknowledgeFlag(name);
            }

            else
            {
                processRangeValues(registry.getRule(name), value);
            }
        }
    };

    public FlagEngine(String[] args)
    {
        if (args == null || args.length == 0)
        {
            throw new IllegalArgumentException("Command arguments cannot be null or empty");
        }

        this.rawArgs = args;
        this.operands = new ArrayList<>();
        this.registry = new FlagRegistry();
        this.longHandler = new LongFlagHandler();
        this.shortHandler = new ShortFlagHandler();
    }

    public void setDebug(boolean enabled)
    {
        this.debug = enabled;
    }

    /**
     * Registers a new flag rule with the engine.
     * 
     * <p>
     * Definitions must be added before calling {@link #execute()}. If a flag with the same name is
     * already registered, the new definition will overwrite the old one.
     * </p>
     * 
     * @param flag
     *        the flag identifier, including dashes (e.g., "-v" or "--portal")
     * @param type
     *        the {@link FlagType} defining how arguments and separators are handled
     * 
     * @throws ParseException
     *         if the flag format is invalid according to {@link FlagRule} constraints
     */
    public void addDefinition(String flag, FlagType type) throws ParseException
    {
        registry.addRule(new FlagRule(flag, type));
    }

    /**
     * Retrieves a handled rule for value extraction.
     * 
     * @param flagName
     *        the name of the flag
     * @return the associated {@link FlagRule}, or null if no such rule exists
     */
    public FlagRule getFlagRule(String flagName)
    {
        return registry.getRule(flagName);
    }

    /**
     * Sets the maximum number of arguments (operands) permitted. These are free-standing arguments
     * not associated with any specific flag or option. A maximum of 16 operands are permitted.
     *
     * @param count
     *        the maximum number of operands allowed. If exceeded during parsing, a ParseException
     *        will be thrown. Default is one argument allowed if not defined
     */
    public void setFreeArgumentLimit(int count)
    {
        if (count < 0 || count > 16)
        {
            throw new IllegalArgumentException("Command arguments cannot be null or empty");
        }

        maxOperands = count;
    }

    /**
     * Executes the parsing logic against the raw arguments provided at construction.
     * 
     * <p>
     * This method performs a single-pass parse. It identifies flags, delegates to specialised
     * handlers, collects operands, and finally validates:
     * </p>
     * 
     * <ul>
     * <li>The free-standing argument limit ({@link #setFreeArgumentLimit}).</li>
     * <li>The presence of all mandatory flags ({@link FlagType#ARG_REQUIRED}, etc).</li>
     * </ul>
     * 
     * @throws ParseException
     *         if an unrecognised flag is encountered, a required argument is missing, or validation
     *         limits are exceeded
     */
    public void execute() throws ParseException
    {
        CommandTokenizer tokenizer = new CommandTokenizer(rawArgs);
        PeekingIterator<String> it = new PeekingIterator<>(tokenizer.getTokens());

        while (it.hasNext())
        {
            String token = it.peek();

            if (isLongOption(token))
            {
                longHandler.handle(it, registry, observer);
            }

            else if (isShortOption(token))
            {
                shortHandler.handle(it, registry, observer);
            }

            else
            {
                // It's an operand, representing a list of free-standing arguments
                operands.add(it.next());
            }
        }

        if (debug)
        {
            System.out.println("--- Registry Definitions ---");
            System.out.println(registry);
            System.out.println("--- Tokenization ---");
            System.out.println(tokenizer);
            System.out.printf("Flattened: %s\n\n", tokenizer.flattenArguments());

            System.out.println("Execution completed. Operands captured: " + operands.size());
        }

        validateFreeArgumentLimit();
        registry.validateRequiredOptions();
    }

    /**
     * Collapses a comma-separated string into manageable individual values.
     * 
     * <p>
     * Each element is trimmed of leading and trailing whitespace. Empty elements (e.g.,
     * "val1,,val2") are ignored. Validated values are processed in {@link #processSingleValue()}.
     * </p>
     * 
     * @param rule
     *        the rule currently being processed
     * @param value
     *        the raw string containing potential multiple values
     */
    private void processRangeValues(FlagRule rule, String value)
    {
        if (value != null && !value.isEmpty())
        {
            String[] values = value.split(",");

            for (String data : values)
            {
                String trimmed = data.trim();

                if (!trimmed.isEmpty())
                {
                    processSingleValue(rule, trimmed);
                }
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
    private void processSingleValue(FlagRule rule, String value)
    {
        if (value != null && !value.isEmpty())
        {
            rule.addValue(value);
            rule.setFlagHandled();
        }
    }

    /**
     * Validates that the number of arguments (operands) does not exceed the defined limit.
     * 
     * @throws ParseException
     *         if the actual number of free-standing arguments exceeds the maximum allowed
     */
    private void validateFreeArgumentLimit() throws ParseException
    {
        int actualCount = operands.size();

        if (actualCount > maxOperands)
        {
            String msg = String.format("Free-standing arguments [%d] is too many (limit is %d). Found [%s]", actualCount, maxOperands, String.join(", ", operands));

            throw new ParseException(msg, 0);
        }
    }

    /**
     * Verifies the specified token indicates a long flag starting with double leading dashes
     * (--), based on the GNU convention. This flag can optionally have a value appended to it, such
     * as --LV or --L=V.
     *
     * @param token
     *        the command line token
     *
     * @return {@code true} if the token is a valid long flag or option
     */
    private static boolean isLongOption(String token)
    {
        return token.matches("\\--[A-Za-z].*$") && token.length() > 2;
    }

    /**
     * Verifies the specified token indicates a valid short option, based on the POSIX convention.
     *
     * A short option is a single character preceded by a single leading dash (-S). It can be
     * clustered or combined with other short options and may be followed by a value, such as -SV,
     * -S=V, -S1S2V, -S1S2=V, or -S1S2=V1,V2,V3.
     *
     * @param token
     *        the raw command line token
     *
     * @return {@code true} if the token is a valid short flag or option
     */
    private static boolean isShortOption(String token)
    {
        return (token.matches("^\\-[^\\-].*$") && token.length() > 1);
    }

    /**
     * Verifies the specified token represents a valid command flag or option.
     *
     * @param token
     *        the entry extracted from the command line
     *
     * @return {@code true} if the token is identified with a valid flag or option
     */
    private static boolean isOption(String token)
    {
        return (isLongOption(token) || isShortOption(token));
    }

    // TEST ONLY
    public static void testOne()
    {
        String[] dummy = {"--platform", "=", "rhel,win10,win2016,WIN2012R2,WIN2019,WIN2022,sles,ubn", "-o", "scopeos.txt", "MDAV Details Export.csv"};

        FlagEngine cli = new FlagEngine(dummy);

        cli.setDebug(true);

        try
        {
            cli.addDefinition("-u", FlagType.ARG_OPTIONAL);
            cli.addDefinition("-f", FlagType.ARG_OPTIONAL);
            cli.addDefinition("-o", FlagType.ARG_OPTIONAL);
            cli.addDefinition("-p", FlagType.SEP_OPTIONAL);
            cli.addDefinition("--platform", FlagType.SEP_OPTIONAL);
            cli.addDefinition("-s", FlagType.ARG_BLANK);
            cli.addDefinition("-q", FlagType.ARG_BLANK);

            cli.addDefinition("-d", FlagType.ARG_BLANK);
            cli.addDefinition("--debug", FlagType.ARG_BLANK);

            cli.addDefinition("-h", FlagType.ARG_BLANK);
            cli.addDefinition("--help", FlagType.ARG_BLANK);

            cli.execute();
        }

        catch (ParseException exc)
        {
            System.err.printf("%s\n", exc.getMessage());
            // exc.printStackTrace();
        }
    }

    public static void testTwo()
    {
        String[] dummy = {
                "-g", "vulfeed.dat", "-acp31", "33", "--gem99", "--csv", "-data", "val99", "--depth82",
                "-b=727", "-n", "/var/trigger.xlsx", "-b", "=", "747", "-vofile.xlsx", "-h", "-x",
                "nina", "-k707", "--range=12,24,36,48,60,72", ",84", ",", ",,,", "96,", ",", ",",
                "--query", "=", "80286", "--range=", ",,,108", "outcomes", "D:/KDR Project/Milestones/TestBatch"};

        FlagEngine cli = new FlagEngine(dummy);

        cli.setDebug(true);

        try
        {
            cli.addDefinition("-a", FlagType.ARG_BLANK);
            cli.addDefinition("-c", FlagType.ARG_REQUIRED);
            cli.addDefinition("--gem", FlagType.ARG_REQUIRED);
            cli.addDefinition("-x", FlagType.ARG_REQUIRED);
            cli.addDefinition("-g", FlagType.ARG_REQUIRED);
            cli.addDefinition("-n", FlagType.ARG_REQUIRED);
            cli.addDefinition("-o", FlagType.ARG_REQUIRED);
            cli.addDefinition("-v", FlagType.ARG_BLANK);
            cli.addDefinition("-h", FlagType.ARG_BLANK);
            cli.addDefinition("--help", FlagType.ARG_BLANK);
            cli.addDefinition("--csv", FlagType.ARG_BLANK);
            cli.addDefinition("-k", FlagType.ARG_OPTIONAL);
            cli.addDefinition("--depth", FlagType.ARG_REQUIRED);
            cli.addDefinition("-b", FlagType.SEP_OPTIONAL);
            cli.addDefinition("--query", FlagType.SEP_REQUIRED);
            cli.addDefinition("--range", FlagType.SEP_REQUIRED);

            cli.execute();
        }

        catch (ParseException exc)
        {
            System.err.printf("%s\n", exc.getMessage());
            // exc.printStackTrace();
        }
    }

    /**
     * @param args
     */
    public static void main(String[] args)
    {
        // testOne();
        testTwo();
    }
}