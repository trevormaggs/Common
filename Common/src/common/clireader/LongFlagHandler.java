package common.clireader;

import java.text.ParseException;
import common.PeekingIterator;

public class LongFlagHandler implements FlagHandler
{
    @Override
    public void handle(PeekingIterator<String> tokens, FlagRegistry registry, FlagListener observer) throws ParseException
    {
        String name = null;
        String value = null;
        String token = FlagRegistry.stripLeadingDashes(tokens.next());

        if (token.contains("="))
        {
            // --L=V and --L=V1,V2,V3 scenarios
            String[] parts = token.split("=", 2);

            name = parts[0];
            value = parts[1];
        }

        else if (registry.getRule(token) != null)
        {
            // --L scenario
            name = token;

            if (registry.getRule(token).expectsArgument() && tokens.hasNext() && !tokens.peek().startsWith("-"))
            {
                value = tokens.next();
            }
        }

        else
        {
            // --LV scenario
            int longestMatch = -1;

            for (FlagRule rule : registry)
            {
                String realRuleName = rule.getFlagName();

                if (rule.isLongFlag() && token.startsWith(realRuleName) && rule.expectsArgument())
                {
                    // Make sure the longest name match, for example: 'portal' against over 'port'
                    if (realRuleName.length() > longestMatch)
                    {
                        name = realRuleName;
                        value = token.substring(realRuleName.length());
                        longestMatch = realRuleName.length();
                    }
                }
            }
        }

        if (registry.getRule(name) == null)
        {
            throw new ParseException("Unrecognised long flag [--" + token + "] detected", 0);
        }

        observer.onDiscovery(name, value);
    }
}