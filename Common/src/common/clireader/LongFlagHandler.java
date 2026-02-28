package common.clireader;

import java.text.ParseException;
import common.PeekingIterator;

public class LongFlagHandler implements FlagHandler
{
    @Override
    public String[] handle(PeekingIterator<String> tokens, FlagRegistry registry) throws ParseException
    {        
        String name = null;
        String value = null;
        String content = FlagRegistry.stripLeadingDashes(tokens.next());

        if (content.contains("="))
        {
            // --L=V
            // --L=V1,V2,V3
            String[] parts = content.split("=", 2);

            name = parts[0];
            value = parts[1];
        }

        else if (registry.getRule(content) != null)
        {
            // --L
            name = content;

            if (registry.getRule(content).expectsArgument() && tokens.hasNext() && !tokens.peek().startsWith("--"))
            {
                value = tokens.next();
            }
        }

        else
        {
            // --LV
            for (FlagRule rule : registry)
            {
                String definedName = FlagRegistry.stripLeadingDashes(rule.getFlagName());

                // We check if the content starts with the flag name AND
                // that the flag name isn't just a single dash (handled by ShortFlagHandler)
                if (definedName.length() > 1 && content.startsWith(definedName))
                {
                    name = definedName;
                    value = content.substring(definedName.length());
                    break;
                }
            }
        }

        // System.out.printf("Data: %s\t%s\n", name, value);
        return new String[]{name, value};
    }
    
    public void handleAlternative(PeekingIterator<String> tokens, FlagRegistry registry) throws ParseException
    {
        String name = null;
        String value = null;
        String rawContent = tokens.next();
        String content = FlagRegistry.stripLeadingDashes(rawContent);

        if (content.contains("="))
        {
            // --L=V
            // --L=V1,V2,V3
            String[] parts = content.split("=", 2);

            name = parts[0];
            value = parts[1];
        }

        else if (registry.getRule(content) != null)
        {
            // --L
            name = content;

            if (registry.getRule(content).expectsArgument() && tokens.hasNext() && !tokens.peek().startsWith("--"))
            {
                value = tokens.next();
            }
        }

        else
        {
            // --LV
            for (FlagRule rule : registry)
            {
                String definedName = FlagRegistry.stripLeadingDashes(rule.getFlagName());

                // We check if the content starts with the flag name AND
                // that the flag name isn't just a single dash (handled by ShortFlagHandler)
                if (definedName.length() > 1 && content.startsWith(definedName))
                {
                    name = definedName;
                    value = content.substring(definedName.length());
                    break;
                }
            }
        }

        if (name == null)
        {
            throw new ParseException("Unrecognised long flag [--" + rawContent + "] detected", 0);
        }

        // Final Assignment
        if (value == null)
        {
            registry.acknowledgeFlag(name);
        }

        else
        {
            registry.assignValue(name, value);
        }
    }

}