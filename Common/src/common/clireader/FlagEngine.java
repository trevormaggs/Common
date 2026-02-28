package common.clireader;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import common.PeekingIterator;

/**
 * The core orchestrator for command-line parsing. This class manages the life-cycle of a parse:
 * definition, tokenization, and execution.
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
     * Registers a new flag rule.
     * 
     * @param flag
     *        the new flag to add
     * @param type
     *        the flag rule type, defining how arguments and separators are handled
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
     * Executes the parsing logic.
     */
    public void execute() throws ParseException
    {
        CommandTokenizer tokenizer = new CommandTokenizer(rawArgs);
        PeekingIterator<String> it = new PeekingIterator<>(tokenizer.getTokens());

        while (it.hasNext())
        {
            String token = it.peek();

            if (token.startsWith("--"))
            {
                String[] result = longHandler.handle(it, registry);

                if (result[0] == null)
                {
                    throw new ParseException("Unrecognised long flag [--" + token + "] detected", 0);
                }

                if (result[1] == null)
                {
                    registry.acknowledgeFlag(result[0]);
                }

                else
                {
                    processRangeValues(registry.getRule(result[0]), result[1]);
                }
            }

            else if (token.startsWith("-") && token.length() > 1)
            {
                shortHandler.handle(it, registry);
                it.next();
            }

            else
            {
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

    public void execute2() throws ParseException
    {
        CommandTokenizer tokenizer = new CommandTokenizer(rawArgs);
        PeekingIterator<String> it = new PeekingIterator<>(tokenizer.getTokens());

        while (it.hasNext())
        {
            String token = it.peek();
            String[] result = null;

            if (token.startsWith("--"))
            {
                result = longHandler.handle(it, registry);
            }

            else if (token.startsWith("-") && token.length() > 1)
            {
                result = shortHandler.handle(it, registry);
                it.next();
            }

            else
            {
                operands.add(it.next());
                continue;
            }

            if (result == null || result[0] == null)
            {
                throw new ParseException("Unrecognised flag [" + token + "] detected", 0);
            }

            else if (result[1] == null)
            {
                registry.acknowledgeFlag(result[0]);
            }

            else
            {
                processRangeValues(registry.getRule(result[0]), result[1]);
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
     * Processes a comma-separated string and adds each individual value to the list of values for
     * the current flag.
     * 
     * @param value
     *        the string with multiple values attached to it
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