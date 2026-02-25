package common;

import java.util.List;
import java.util.Set;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Executes the specified command, binary, or executable to generate an array of output lines.
 * Additional arguments can be supplied to the command line prior to execution.
 *
 * <p>
 * <b>Important note:</b> For Windows environment execution, incorporating {@code cmd.exe /c} at the
 * beginning of the command line may be required.
 * </p>
 *
 * <p>
 * <b>Platform:</b> Unix, Linux and Windows operating systems.
 * </p>
 *
 * <p>
 * Change logs:
 * </p>
 *
 * <ul>
 * <li>Trevor Maggs modified on 5 February 2017</li>
 * <li>Modified on 24 March 2020. Instantiation is now accessible through public static methods</li>
 * </ul>
 *
 * @author Trevor Maggs
 * @version 0.3
 * @since 24 March 2020
 */
public final class RunCommand
{
    private Set<String> flagSet;
    private List<String> processResults;

    /**
     * This private constructor restricts direct instantiation of this object, enforcing the use of
     * the public static factory methods only for implicit construction.
     */
    private RunCommand()
    {
        flagSet = new LinkedHashSet<>();
    }

    /**
     * This private overloaded constructor restricts direct instantiation of this object, enforcing
     * the use of the public static factory methods only for implicit construction.
     * 
     * @param cmd
     *        the command to be executed
     */
    private RunCommand(String cmd)
    {
        this();
        addCommand(cmd);
    }

    /**
     * This private overloaded constructor prevents direct instantiation of this object. You must
     * use the public static factory methods provided for implicit construction. This method handles
     * the specified arguments in relation to the specified command.
     *
     * @param cmd
     *        The command to be executed.
     * @param arg
     *        The argument fed to the command.
     */
    private RunCommand(String cmd, String arg)
    {
        this();

        addCommand(cmd);
        addArgument(arg);
    }

    /**
     * Sets the command or executable. If the input string contains multiple arguments appended to
     * the command, they will be split into constituent parts and treat them as separate arguments.
     *
     * @param cmd
     *        the command to be executed
     * @return the count of constituent parts extracted from the original input string
     */
    private int addCommand(final String cmd)
    {
        String str = cmd.trim();

        if (str.length() > 0)
        {
            String[] parts = str.split("\\s+");

            for (int i = 0; i < parts.length; i++)
            {
                if (i > 0)
                {
                    addArgument(parts[i]);
                }

                else
                {
                    flagSet.add(parts[i]);
                }
            }

            return parts.length;
        }

        return 0;
    }

    /**
     * Appends an argument used to feed to the command.
     * 
     * @param arg
     *        the argument in integer form
     * 
     * @return this object to allow chaining
     */
    public RunCommand addArgument(int arg)
    {
        return addArgument(String.valueOf(arg));
    }

    /**
     * Appends an argument used to feed to the command.
     *
     * @param arg
     *        the argument in string form
     * 
     * @return this object to allow chaining
     * @throws IllegalStateException
     *         if the command is empty
     */
    public RunCommand addArgument(String arg)
    {
        arg = arg.trim();

        if (arg.isEmpty())
        {
            return this;
        }

        else if (flagSet.size() > 0)
        {
            flagSet.add(arg);
        }

        else
        {
            throw new IllegalStateException("Cannot add arguments without the command in place");
        }

        return this;
    }

    /**
     * Executes the command with optional arguments and returns the exit code. A zero value
     * signifies successful execution, while non-zero values indicate an error.
     *
     * @return the exit code, where zero denotes success and non-zero values denote possible errors
     * 
     * @throws IllegalStateException
     *         if the command is missing
     * @throws IOException
     *         if an I/O error has occurred
     */
    public int execute() throws IOException
    {
        int retCode;

        if (flagSet.size() == 0)
        {
            throw new IllegalStateException("Cannot find run command. Please check.");
        }

        processResults = new ArrayList<>();

        try
        {
            String line;
            ProcessBuilder pb = new ProcessBuilder(new LinkedList<>(flagSet));
            Process proc = pb.start();

            try (BufferedReader br = new BufferedReader(new InputStreamReader(proc.getInputStream())))
            {
                while ((line = br.readLine()) != null)
                {
                    processResults.add(line);
                }
            }

            retCode = proc.waitFor();
        }

        catch (InterruptedException exc)
        {
            throw new IOException("Unable to execute command [" + flagSet.toArray()[0] + "]", exc);
        }

        return retCode;
    }

    /**
     * Retrieves the last used command. Any any associated arguments may be included if provided.
     *
     * @return the last command as a string, possibly showing its arguments
     */
    public String getLastCommand()
    {
        StringBuilder out = new StringBuilder(256);

        if (flagSet.isEmpty())
        {
            out.append("");
        }

        else for (String str : flagSet)
        {
            if (out.length() > 0)
            {
                out.append(" ").append(str);
            }

            else
            {
                out.append(str);
            }
        }

        return out.toString();
    }

    /**
     * Retrieves an array of output results.
     *
     * @return an array of strings, reflecting the output results
     */
    public String[] getResults()
    {
        return processResults.toArray(new String[processResults.size()]);
    }

    /**
     * Retrieves output information from the specified position within the result array.
     *
     * @param index
     *        the position of the array
     *
     * @return the output information at the specified index, formatted as a string
     */
    public String getResult(int index)
    {
        return getResults()[index];
    }

    /**
     * Public static factory method to obtain an instance of this object for invoking other public
     * methods.
     *
     * @param cmd
     *        the command to be executed. If it has multiple arguments appended to the command
     *        itself, they will be split into constituent parts, serving as command arguments
     * 
     * @return the instance of this object
     */
    public static RunCommand newInstance(final String cmd)
    {
        return new RunCommand(cmd);
    }

    /**
     * Public static factory method to obtain an instance of this object for invoking other public
     * methods.
     *
     * @param cmd
     *        the command to be executed. If it has multiple arguments appended to the command
     *        itself, they will be split into constituent parts, serving as command arguments
     * @param arg
     *        the argument used to feed the command
     *
     * @return the instance of this object
     */
    public static RunCommand newInstance(final String cmd, final String arg)
    {
        return new RunCommand(cmd, arg);
    }
}