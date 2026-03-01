package common.clireader;

import java.text.ParseException;
import common.PeekingIterator;

public class ShortFlagHandler implements FlagHandler
{
    @Override
    public void handle(PeekingIterator<String> tokens, FlagRegistry registry, FlagListener observer) throws ParseException
    {
        tokens.next();
    }
}
