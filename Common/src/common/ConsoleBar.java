package common;

/**
 * Displays a simple progress bar in the console with progress percentage sign, which advances
 * progressively in response to new updates.
 * 
 * @author Created on 19 February 2020 by Trevor Maggs
 * @version 0.1
 * @since 19 February 2020
 */
public final class ConsoleBar
{
    /**
     * Private constructor to prevent this static-only class from being
     * instantiated.
     */
    private ConsoleBar()
    {
    }

    /**
     * Displays the progress bar with a progress percentage updated iteratively.
     * 
     * @param current
     *        is the current number in progress
     * @param total
     *        is the total number of items
     */
    public static void updateProgressBar(int current, int total)
    {
        StringBuilder sb = new StringBuilder("\r[");

        int width = 50;
        int percent = (current * 100 / total);

        for (int i = 0; i < width; i++)
        {
            if (i < (percent / 2)) sb.append("=");
            else if (i == (percent / 2)) sb.append(">");
            else sb.append(" ");
        }

        sb.append("] %s  ");

        if (percent >= 100) sb.append("%n");

        System.out.printf(sb.toString(), percent + "%");
    }
}