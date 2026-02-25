package common;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import common.cli.CommandLineReader;

public class CommonTest
{
    public static void testPatternMatchFactory()
    {
        // boolean ok = PatternMatch.matches("^\\s*(hello)\\s+(.*)$", " Hello Trev!", true);
        // boolean ok = PatternMatch.matches("^\\s*hello\\s+.*$", " Hello Trev!", true);
        boolean ok = PatternMatch.matches("rw-r--$", "rw-rw-r--");

        System.out.println(ok);
        System.out.println(PatternMatch.extract(0));
    }

    public static void testStringJoiner()
    {
        String[] arr = {"zoo", "museum", "departments", "opera", "restaurants", "parliament", "clubs", "hospital"};

        Integer[] intarr = {56, 789, 345, 808, 747};

        List<String> list = new ArrayList<String>()
        {
            {
                add("BMW");
                add("Holden");
                add("Ford");
                add("Porsche");
                add("VW");
                add("Mercdes Benz");
                add("Skoda");
                add("Toyota");
            }
        };

        System.out.println(StringJoiner.join(",", arr, "(", ")"));
        System.out.println(StringJoiner.join("|", list, "[", "]"));
        System.out.println(StringJoiner.join("<-->", list));
        System.out.println(StringJoiner.join(" - ", intarr, "{", "}"));
        System.out.println(StringJoiner.join(" - ", intarr, "{", "}", 2));
    }

    public static void testCommandLineParser(String[] arr)
    {
        String[] dummy = {
                "-g", "vulfeed.dat", "-ac33", "--gem99", "--csv", "-data", "val99", "--depth82",
                "-b=727", "-n", "/var/trigger.xlsx", "-b", "=", "747", "-vofile.xlsx", "-h", "-x",
                "nina", "-k707", "--range=12,24,36,48,60,72", ",84", ",", ",,,", "96,", ",", ",",
                "-query", "=", "80286", "--range=", ",,,108", "outcomes"};

        CommandLineParser cli = new CommandLineParser(dummy);

        try
        {
            cli.addRule("-a", CommandLineParser.ARG_BLANK);
            cli.addRule("-c", CommandLineParser.ARG_REQUIRED);
            cli.addRule("--gem", CommandLineParser.ARG_REQUIRED);
            cli.addRule("-x", CommandLineParser.ARG_REQUIRED);
            cli.addRule("-g", CommandLineParser.ARG_REQUIRED);
            cli.addRule("-n", CommandLineParser.ARG_REQUIRED);
            cli.addRule("-o", CommandLineParser.ARG_REQUIRED);
            cli.addRule("-v", CommandLineParser.ARG_BLANK);
            cli.addRule("-h", CommandLineParser.ARG_BLANK);
            cli.addRule("--help", CommandLineParser.ARG_BLANK);
            cli.addRule("--csv", CommandLineParser.ARG_BLANK);
            cli.addRule("-data", CommandLineParser.ARG_OPTIONAL);
            cli.addRule("-k", CommandLineParser.ARG_OPTIONAL);
            cli.addRule("--depth", CommandLineParser.ARG_REQUIRED);
            cli.addRule("-b", CommandLineParser.SEP_OPTIONAL);
            cli.addRule("-query", CommandLineParser.SEP_REQUIRED);
            cli.addRule("--range", CommandLineParser.SEP_REQUIRED);
            cli.setMaximumArgumentCount(3);

            System.out.printf("%s%n%n", cli.flattenArguments());

            cli.parse();

            System.out.println(cli);
        }

        catch (ParseException exc)
        {
            System.err.println(exc.getLocalizedMessage());
        }
    }

    public static void testNewCommandLineParser(String[] arr)
    {
        String[] dummy = {
                "-g", "vulfeed.dat", "-acp31", "33", "--gem99", "--csv", "-data", "val99", "--depth82",
                "-b=727", "-n", "/var/trigger.xlsx", "-b", "=", "747", "-vofile.xlsx", "-h", "-x",
                "nina", "-k707", "--range=12,24,36,48,60,72", ",84", ",", ",,,", "96,", ",", ",",
                "-query", "=", "80286", "--range=", ",,,108", "outcomes", "D:/KDR Project/Milestones/TestBatch"};

        String[] dummy2 = {"--platform", "=", "rhel,win10,win2016,WIN2012R2,WIN2019,WIN2022,sles,ubn", "-o", "scopeos.txt", "MDAV Details Export.csv"};

        CommandLineReader cli = new CommandLineReader(dummy2, true);

        cli.setMaximumStandaloneArgumentCount(1);

        try
        {
            // Define command argument rules
            /*
             * cli.addRule("-a", CommandLineParser.ARG_BLANK);
             * cli.addRule("-c", CommandLineParser.ARG_REQUIRED);
             * cli.addRule("--gem", CommandLineParser.ARG_REQUIRED);
             * cli.addRule("-x", CommandLineParser.ARG_REQUIRED);
             * cli.addRule("-g", CommandLineParser.ARG_REQUIRED);
             * cli.addRule("-n", CommandLineParser.ARG_REQUIRED);
             * cli.addRule("-o", CommandLineParser.ARG_REQUIRED);
             * cli.addRule("-v", CommandLineParser.ARG_BLANK);
             * cli.addRule("-h", CommandLineParser.ARG_BLANK);
             * cli.addRule("--help", CommandLineParser.ARG_BLANK);
             * cli.addRule("--csv", CommandLineParser.ARG_BLANK);
             * cli.addRule("-data", CommandLineParser.ARG_OPTIONAL);
             * cli.addRule("-k", CommandLineParser.ARG_OPTIONAL);
             * cli.addRule("--depth", CommandLineParser.ARG_REQUIRED);
             * cli.addRule("-b", CommandLineParser.SEP_OPTIONAL);
             * cli.addRule("-query", CommandLineParser.SEP_REQUIRED);
             * cli.addRule("--range", CommandLineParser.SEP_REQUIRED);
             */

            cli.addRule("-u", CommandLineReader.ARG_OPTIONAL);
            cli.addRule("-f", CommandLineReader.ARG_OPTIONAL);
            cli.addRule("-o", CommandLineReader.ARG_OPTIONAL);
            cli.addRule("-p", CommandLineReader.SEP_OPTIONAL);
            cli.addRule("--platform", CommandLineReader.SEP_OPTIONAL);
            cli.addRule("-s", CommandLineReader.ARG_BLANK);
            cli.addRule("-q", CommandLineReader.ARG_BLANK);

            cli.addRule("-d", CommandLineReader.ARG_BLANK);
            cli.addRule("--debug", CommandLineReader.ARG_BLANK);

            cli.addRule("-h", CommandLineReader.ARG_BLANK);
            cli.addRule("--help", CommandLineReader.ARG_BLANK);

            cli.setMaximumStandaloneArgumentCount(1);
            cli.parse();
        }

        catch (ParseException exc)
        {
            System.err.println(exc.getMessage());
        }
    }

    public static void main(String[] args)
    {
        // testStringJoiner();
        // testPatternMatchFactory();
        // testCommandParser();
        // testCommandLineParser(args);
        testNewCommandLineParser(args);
    }
}