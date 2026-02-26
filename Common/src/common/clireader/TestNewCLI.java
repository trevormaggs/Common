package common.clireader;

import java.text.ParseException;
import common.clireader.FlagRule.FlagType;

public class TestNewCLI
{
    private final boolean debug;
    private final String[] rawArgs;
    private final FlagRegistry registry;

    /**
     * Constructs a new instance of the FlagType class, using the command line arguments
     * provided and offering the option to enable debugging.
     *
     * @param args
     *        the command line arguments
     * @param dbg
     *        a boolean flag specifying whether debugging should be enabled
     */
    public TestNewCLI(String[] args, boolean debug)
    {
        if (args == null || args.length == 0)
        {
            throw new IllegalArgumentException("Command arguments are empty.");
        }

        this.rawArgs = args;
        this.debug = debug;
        this.registry = new FlagRegistry();
    }

    public TestNewCLI(String[] args)
    {
        this(args, false);
    }

    public void addDefinition(String flag, FlagType type) throws ParseException
    {
        registry.addRule(new FlagRule(flag, type));
    }

    /**
     * This is the "Engine" trigger.
     * It should only be called after all addDefinition calls are done.
     */
    public void execute()
    {
        try
        {
            // 1. Clean the input
            CommandTokenizer tokenizer = new CommandTokenizer(rawArgs);

            // 2. The Strategy-based Engine would be invoked here
            // FlagEngine engine = new FlagEngine(registry, tokenizer);
            // engine.parse();

            // 3. Post-parse logic (just like your old engine's debug output)
            if (registry.existsFlag("-v") && registry.getRule("-v").isFlagHandled())
            {
                System.out.println("Verbose mode enabled.");
            }
        }

        // catch (ParseException exc)
        catch (Exception exc)
        {
            System.err.printf("%s\n", exc.getMessage());
            // System.exit(1);
        }
    }

    /**
     * @param args
     */
    public static void main(String[] args)
    {
        String[] dummy = {
                "-g", "vulfeed.dat", "-acp31", "33", "--gem99", "--csv", "-data", "val99", "--depth82",
                "-b=727", "-n", "/var/trigger.xlsx", "-b", "=", "747", "-vofile.xlsx", "-h", "-x",
                "nina", "-k707", "--range=12,24,36,48,60,72", ",84", ",", ",,,", "96,", ",", ",",
                "-query", "=", "80286", "--range=", ",,,108", "outcomes", "D:/KDR Project/Milestones/TestBatch"};

        String[] dummy2 = {"--platform", "=", "rhel,win10,win2016,WIN2012R2,WIN2019,WIN2022,sles,ubn", "-o", "scopeos.txt", "MDAV Details Export.csv"};

        TestNewCLI cli = new TestNewCLI(dummy2, true);

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
}