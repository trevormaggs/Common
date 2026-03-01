package common.clireader;

import java.text.ParseException;

public interface FlagListener
{
    void onDiscovery(String name, String value) throws ParseException;
}