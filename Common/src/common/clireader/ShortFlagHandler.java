package common.clireader;

import java.text.ParseException;
import common.PeekingIterator;

public class ShortFlagHandler implements FlagHandler
{
    @Override
    public String[] handle(PeekingIterator<String> tokens, FlagRegistry registry) throws ParseException
    {
        return null;
    }
}
