package common.clireader;

import java.text.ParseException;
import common.PeekingIterator;

/**
 * Strategy interface for processing different types of command-line flags.
 */
public interface FlagHandler
{
    /**
     * Processes a flag and its potential arguments.
     *
     * @param tokens
     *        the stream of normalised tokens
     * @param registry
     *        the registry to look up rules
     * @return String array where [0] is name and [1] is value (can be null)
     *
     * @throws ParseException
     *         if the flag or its arguments violate the rules
     */
    String[] handle(PeekingIterator<String> tokens, FlagRegistry registry) throws ParseException;
}