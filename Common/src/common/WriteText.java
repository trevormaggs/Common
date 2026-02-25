package common;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

/**
 * This simple class creates a plain text file for writing from scratch. Optionally, you can elect
 * to append entries to the existing file. A delimiter character is supplied by default. You can
 * override it using the {@code setDelimiter()} method.
 * 
 * <p>
 * <b>Change logs:</b>
 * </p>
 * 
 * <ul>
 * <li>Released by Trevor Maggs - 11 April 2016</li>
 * <li>Modified to support for JDK1.7 NIO.2 functionality - 1 May 2017</li>
 * <li>Modified to make it more generic - 4 May 2018</li>
 * </ul>
 * 
 * @author Trevor Maggs
 * @version 0.4
 * @since 4 May 2018
 */
public class WriteText
{
    // Constants
    public static final char DELIMITER = ',';
    private boolean appendFlag;
    protected Path fileOut;
    protected char delimiter;
    protected BufferedWriter writer;

    // Constructors
    /**
     * Creates an instance of this class and initialises the default output file named as {@code out.txt}.
     * 
     * @throws IOException
     *         if there are problems opening file
     */
    public WriteText() throws IOException
    {
        this(Paths.get("out.txt"));
    }

    /**
     * Creates an instance of this class and sets the output file.
     * 
     * @param file
     *        relative or absolute path to the file
     * 
     * @throws IOException
     *         if there are problems opening file
     */
    public WriteText(String file) throws IOException
    {
        this(Paths.get(file));
    }

    /**
     * Creates an instance of this class and sets the output file.
     * 
     * @param pfile
     *        Path object representing the output file
     * 
     * @throws IOException
     *         if there are problems opening file
     */
    public WriteText(Path pfile) throws IOException
    {
        this(pfile, false);
    }

    /**
     * Creates an instance of this class and sets the output file for writing or appending.
     * 
     * @param file
     *        relative or absolute path to the file
     * @param append
     *        true if file append is required
     * 
     * @throws IOException
     *         if there are problems opening file
     */
    public WriteText(String file, boolean append) throws IOException
    {
        this(Paths.get(file), append);
    }

    /**
     * Creates an instance of this class and sets the output file for writing or appending.
     * 
     * @param pfile
     *        Path object representing the output file
     * @param append
     *        true if file append is required
     * 
     * @throws IOException
     *         if there are problems opening file
     */
    public WriteText(Path pfile, boolean append) throws IOException
    {
        fileOut = pfile;
        appendFlag = append;
        delimiter = DELIMITER;

        open();
    }

    /**
     * Opens the file session. File append will be enabled, depending on the setting of {@code appendFlag} fields.
     * 
     * @throws IOException
     *         if there are problems opening file
     */
    private void open() throws IOException
    {
        try
        {
            if (appendFlag)
            {
                writer = Files.newBufferedWriter(fileOut, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            }

            else
            {
                writer = Files.newBufferedWriter(fileOut, StandardCharsets.UTF_8);
            }
        }

        catch (IOException exc)
        {
            throw new IOException("Cannot open file [" + fileOut + "]. Check permissions.", exc);
        }
    }

    /**
     * Reads entries from the array of strings and writes to the current row.
     * 
     * @param arr array of strings
     *        
     * @return number of columns that have been written to the current row
     * @throws IOException if there is a problem writing to the file
     */
    private int write(String[] arr) throws IOException
    {
        int col = 0;

        if (writer != null)
        {
            try
            {
                for (String text : arr)
                {
                    if (col > 0) writer.append(delimiter);

                    writer.append(text);

                    col++;
                }

                writer.append(System.lineSeparator());
                writer.flush();
            }

            catch (IOException exc)
            {
                close();

                throw new IOException("Unable to write to file [" + fileOut + "]", exc);
            }
        }

        else
        {
            throw new IOException("Unable to obtain a resource to write to file [" + fileOut + "].");
        }

        return col;
    }

    /**
     * Writes entries from the list of strings to the current row.
     * 
     * @param list
     *        is the list of entries
     * @return number of columns that have been written to the current row
     * 
     * @throws IOException
     *         if there is a problem writing to the file
     */
    private int write(List<String> list) throws IOException
    {
        int col = 0;

        if (writer != null)
        {
            try
            {
                for (String text : list)
                {
                    if (col > 0) writer.append(delimiter);

                    writer.append(text);

                    col++;
                }

                writer.append(System.lineSeparator());
                writer.flush();
            }

            catch (IOException exc)
            {
                close();

                throw new IOException("Unable to write to file [" + fileOut + "]", exc);
            }
        }

        else
        {
            throw new IOException("Unable to obtain a resource to write to file [" + fileOut + "].");
        }

        return col;
    }

    /**
     * Writes plain text to the current row.
     * 
     * @param str
     *        text to be written
     * 
     * @throws IOException
     *         if there is a problem writing to the file
     */
    public void writeLine(String str) throws IOException
    {
        String line = (str == null) ? "" : str;

        if (writer != null)
        {
            try
            {
                writer.write(line);
            }

            catch (IOException exc)
            {
                close();

                throw new IOException("Unable to write to file [" + fileOut + "]", exc);
            }
        }

        else
        {
            throw new IOException("Unable to obtain a resource to write to file [" + fileOut + "].");
        }
    }

    /**
     * Writes plain text to the current row with a newline added at the end of the line.
     * 
     * @param str
     *        text to be written
     * 
     * @throws IOException
     *         if there is a problem writing to the file
     */
    public void writeLineLn(String str) throws IOException
    {
        writeLine(str);
        writeLine(System.lineSeparator());
    }

    /**
     * Writes text entries from the array of strings to the current row.
     * 
     * @param entry
     *        list of entries
     * 
     * @return number of columns being written to the current row
     * @throws IOException
     *         if there is a problem writing to the file
     */
    public int writeContent(String[] entry) throws IOException
    {
        return write(entry);
    }

    /**
     * Writes text entries from the list of strings to the current row.
     * 
     * @param list
     *        the list of entries
     * 
     * @return number of columns being written to the current row
     * @throws IOException
     *         if there is a problem writing to the file
     */
    public int writeContent(List<String> list) throws IOException
    {
        return write(list);
    }

    /**
     * Writes headers from the array of strings to the current row.
     * 
     * @param header
     *        list of headers
     * 
     * @return number of columns that have been written to the current row
     * @throws IOException
     *         if there is a problem writing to the file
     */
    public int writeHeader(String[] header) throws IOException
    {
        return writeContent(header);
    }

    /**
     * Writes headers from the list of strings to the current row.
     * 
     * @param header
     *        list of headers
     * 
     * @return number of columns that have been written to the current row
     * @throws IOException
     *         if there is a problem writing to the file
     */
    public int writeHeader(List<String> header) throws IOException
    {
        return writeContent(header);
    }

    /**
     * Gets the name of the file currently being used for writing contents to.
     * 
     * @return active file name
     */
    public String getFileName()
    {
        return fileOut.getFileName().toString();
    }

    /**
     * Replaces the default delimiter character with a different delimiter. The new delimiter will
     * be inserted to separate entries in each row.
     * 
     * @param sep
     *        new delimiter character
     */
    public void setDelimiter(char sep)
    {
        delimiter = sep;
    }

    /**
     * Closes the file writing session and releases from memory.
     * 
     * @throws IOException
     *         if there is a problem closing the file
     */
    public void close() throws IOException
    {
        try
        {
            if (writer != null)
            {
                writer.close();
            }
        }

        catch (IOException exc)
        {
            throw new IOException("Unable to close file [" + fileOut + "]", exc);
        }
    }
}