package common;

import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.regex.PatternSyntaxException;

/**
 * <p>
 * This program acts as a wrapper to implement the following two standard Java Regular Expression
 * libraries.
 * </p>
 * 
 * <ul>
 * <li>{@code java.util.regex.Pattern}</li>
 * <li>{@code java.util.regex.Matcher}</li>
 * </ul>
 * 
 * <p>
 * The idea is to facilitate the one stop shop in an attempt to match the given input against the
 * pattern in the form of a regular expression. It is more sophisticated and powerful than the
 * static {@code String.matches} method. You have the ability to fetch a number of sub-strings from
 * the most recent successful match by specifying the desired capturing group number.
 * </p>
 * 
 * <p>
 * As this program may be frequently invoked in certain programs, a careful design effort had been
 * made in an attempt to lower the memory footprint. Every time, you evaluate any type of regular
 * expressions that you specify, this class can only be instantiated through a public static factory
 * method, providing an opportunity for this program to repeatedly cache the same instance, thereby
 * helping improve the performance.
 * </p>
 * 
 * <p>
 * Always invoke the public static {@code matches} method first before using one of the available
 * {@code execute} methods to extract the required sub-strings. Please note, this is not
 * thread-safe.
 * </p>
 * 
 * <p>
 * <b>Platform:</b> Windows and *NIX operating systems
 * </p>
 * 
 * <p>
 * <b>Change logs:</b>
 * </p>
 * 
 * <ul>
 * <li>Trevor Maggs created on 18 December 2019</li>
 * </ul>
 * 
 * @author Trevor Maggs
 * @since 18 December 2019
 */
public final class PatternMatch
{
    private static Matcher matcher;
    private static boolean matched;

    /**
     * This constructor is made private by design to prevent this object from being directly
     * constructed. Only the public static factory methods provided are used to invoke this
     * constructor indirectly.
     * 
     * @param regex
     *        the form of regular expression
     * @param input
     *        the entire input sequence to be evaluated
     * @param caseInsensitive
     *        boolean true to enable case-insensitive matching
     */
    private PatternMatch(final String regex, final String input, final boolean caseInsensitive)
    {
        doMatchOperation(regex, input, caseInsensitive);
    }

    /**
     * Makes an attempt to match the entire input sequence against the pattern.
     * 
     * @param regex
     *        the form of regular expression
     * @param input
     *        the entire input sequence to be evaluated
     * @param caseInsensitive
     *        boolean true to enable case-insensitive matching
     * 
     * @throws IllegalArgumentException
     *         if the regular expression has a syntax error
     */
    private void doMatchOperation(String regex, String input, boolean caseInsensitive)
    {
        Pattern pattern;

        if (regex.length() > 0)
        {
            try
            {
                matched = false;

                if (caseInsensitive)
                {
                    pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
                }

                else
                {
                    pattern = Pattern.compile(regex);
                }

                matcher = pattern.matcher(input);

                matched = matcher.find();
            }

            catch (PatternSyntaxException exc)
            {
                throw new IllegalArgumentException("Pattern syntax error in [" + regex + "] regular expression", exc);
            }
        }
    }

    /**
     * Indicates whether a match has occurred.
     * 
     * @return boolean true if a match is found, otherwise false
     */
    private boolean matchFound()
    {
        return matched;
    }

    /**
     * Fetches a sub-string based on the capturing group number of the last successful match.
     * 
     * @param group
     *        capturing group number formatted in the form of <em>1</em>, <em>2</em> ... <em>n</em>
     *        to select the desired input subsequence
     * 
     * @return Extracted portion from the matched input. If there is no match, an empty string is
     *         assumed
     * @throws IllegalArgumentException
     *         if the capturing group number is not between 0 and the maximum number of capturing
     *         groups from the last successful match
     */
    public static String extract(final int group)
    {
        if (matched)
        {
            if (matcher.groupCount() == 0 && group > 0)
            {
                throw new IllegalArgumentException("No capturing group found in pattern [" + getLastPatternUsed() + "]");
            }

            else if ((group < 0 | group > matcher.groupCount()))
            {
                throw new IllegalArgumentException("Capturing group number for pattern [" + getLastPatternUsed() + "] should be between 0 and " + (matcher.groupCount()));
            }

            else
            {
                return matcher.group(group);
            }
        }

        return "";
    }

    /**
     * Fetches an entire input subsequence from the last successful match. It is equivalent to the
     * default group number 0.
     * 
     * @return Extracted portion from the matched input. If there is no match, an empty string is
     *         assumed
     */
    public static String extract()
    {
        return extract(0);
    }

    /**
     * Fetches a sub-string based on the capturing group in string form of the last successful
     * match.
     * 
     * @param group
     *        capturing group number formatted in the form of <em>$1</em>, <em>$2</em>...<em>$n</em>
     *        to select the desired input subsequence
     * 
     * @return Extracted portion from the matched input. If there is no match, an empty string is
     *         assumed
     * @throws IllegalArgumentException
     *         if the capturing group number is not between 0 and the maximum number of capturing
     *         groups from the last successful match
     */
    public static String extract(final String group)
    {
        String str = group;

        try
        {
            if (str.contains("$"))
            {
                str = str.substring(str.indexOf("$") + 1);
            }

            return extract(Integer.parseInt(str, 10));
        }

        catch (NumberFormatException exc)
        {
            throw new IllegalArgumentException("Capturing group number must be in the form of [$0, $1...$n]", exc);
        }
    }

    /**
     * Returns the pattern that was last used.
     * 
     * @return pattern in string form
     */
    public static String getLastPatternUsed()
    {
        return matcher.pattern().pattern();
    }

    /**
     * Public static factory method to check whether the entire input sequence contains a standalone
     * word using the word boundary test. Note, it is advisable not to use a regular expression in
     * the input needle word.
     * 
     * @param word
     *        the standalone literal word to search for
     * @param subject
     *        the original input to be evaluated
     * 
     * @return boolean true if a match is found
     * @throws IllegalArgumentException
     *         if there is a syntax error
     */
    public static boolean hasWordBoundary(final String word, final String subject)
    {
        return matches(".*\\b" + word + "\\b.*", subject, false);
    }

    /**
     * Public static factory method to check whether the entire input sequence contains a standalone
     * word using the word boundary test.
     * 
     * It is useful for solving some difficult regular expression evaluations, where a word to be
     * searched is not part of some other word. It inserts word boundary meta-characters in the
     * subject to make it more easier for the regular expression to match with the standalone word.
     * This can be a powerful solution to the problem often experienced by the limited
     * {@code String.contains} method.
     * 
     * Note, it is advisable not to use a regular expression in the input needle word.
     * 
     * @param needle
     *        the standalone literal word to search for
     * @param subject
     *        the original input to be evaluated
     * @param caseInsensitive
     *        boolean true to enable case-insensitive matching
     * 
     * @return boolean true if a match is found
     * @throws IllegalArgumentException
     *         if there is a syntax error
     */
    public static boolean hasWordBoundary(final String needle, final String subject, boolean caseInsensitive)
    {
        return matches(".*\\b" + needle + "\\b.*", subject, caseInsensitive);
    }

    /**
     * Public static factory method to attempt to match the entire input sequence (subject) against
     * the pattern (regular expression).
     * 
     * @param pattern
     *        the form of regular expression
     * @param subject
     *        the actual input to be evaluated
     * @param caseInsensitive
     *        boolean true to enable case-insensitive matching
     * 
     * @return boolean true if a match is found, otherwise false
     * @throws IllegalArgumentException
     *         if the regular expression has a syntax error
     */
    public static boolean matches(String pattern, String subject, boolean caseInsensitive)
    {
        PatternMatch obj = new PatternMatch(pattern, subject, caseInsensitive);

        return obj.matchFound();
    }

    /**
     * Public static factory method to attempt to match the entire input sequence (subject) against
     * the pattern (regular expression).
     * 
     * @param regex
     *        the form of regular expression
     * @param subject
     *        the actual input to be evaluated
     * 
     * @return boolean true if a match is found, otherwise false
     * @throws IllegalArgumentException
     *         if the regular expression has a syntax error
     */
    public static boolean matches(final String regex, final String subject)
    {
        return matches(regex, subject, false);
    }
}