package common.clireader;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * This class serves as a registry for all defined flag rules.
 * 
 * It manages the collection of {@link FlagRule} instances, providing methods to register new rules
 * and retrieve them during the parsing process. It ensures that flag names remain unique within the
 * application context.
 *
 * @author Trevor Maggs
 * @version 1.0
 * @since 26 February 2026
 */
public final class FlagRegistry implements Iterable<FlagRule>
{
    private final Map<String, FlagRule> flagMap;
    private final List<FlagRule> requiredFlags = new ArrayList<>();

    /**
     * Core registry class used to initialise a new flag registry for compliance purposes.
     * 
     * Note, it is based on a TreeMap type to ensure that flags are sorted alphabetically for easier
     * debugging.
     */
    public FlagRegistry()
    {
        this.flagMap = new TreeMap<>();
    }

    public FlagRegistry(FlagRule rule)
    {
        this();
        addRule(rule);
    }

    @Override
    public java.util.Iterator<FlagRule> iterator()
    {
        return flagMap.values().iterator();
    }

    /**
     * Verifies that all required flags are present and ready for use. If the requirement is not
     * met, it throws an exception.
     *
     * @throws ParseException
     *         if any of the remaining flags have not been processed yet, the message displays a
     *         comma-separated string representation of any missing flag names in the console
     */
    public void validateRequiredOptions() throws ParseException
    {
        if (!requiredFlags.isEmpty())
        {
            StringBuilder sb = new StringBuilder();

            for (int k = 0; k < requiredFlags.size(); k++)
            {
                if (k > 1)
                {
                    sb.append(",");
                }

                sb.append(requiredFlags.get(k).getFlagName());
            }

            throw new ParseException("Missing required option" + (requiredFlags.size() > 1 ? "s" : "") + ": [" + sb.toString() + "]", 0);
        }
    }

    /**
     * Registers a new flag rule in the registry.
     *
     * @param rule
     *        the {@link FlagRule} to add
     * 
     * @throws IllegalArgumentException
     *         if a flag with the same name is already registered
     */
    public void addRule(FlagRule rule)
    {
        String name = rule.getFlagName();

        if (flagMap.containsKey(name))
        {
            throw new IllegalArgumentException("Flag [" + name + "] is already defined in this registry.");
        }

        flagMap.put(name, rule);        
        requiredFlags.add(rule);
        
        System.out.printf("flag: %-20s%s\n", rule.getFlagName(), rule.getFlagType());
    }

    /**
     * Retrieves a flag rule by its name.
     *
     * @param name
     *        the name of the flag (e.g., "-v" or "--verbose")
     * @return the associated {@link FlagRule}, or null if no such rule exists
     */
    public FlagRule getRule(String name)
    {
        return flagMap.get(name);
    }

    /**
     * Returns the number of registered flag rules.
     *
     * @return the size
     */
    public int getRuleSize()
    {
        return flagMap.size();
    }

    /**
     * Checks if a flag name has been set in the registry.
     *
     * @param name
     *        the name of the flag to check
     * @return true if the flag is registered, false otherwise
     */
    public boolean existsFlag(String name)
    {
        return flagMap.containsKey(name);
    }

    /**
     * Resets all registered flags to their initial state.
     * 
     * <p>
     * This is useful when the same registry is used for multiple parsing operations, ensuring that
     * the 'handled' status of each flag is cleared.
     * </p>
     */
    public void resetAll()
    {
        for (FlagRule rule : flagMap.values())
        {
            rule.resetFlag();
        }
    }
}