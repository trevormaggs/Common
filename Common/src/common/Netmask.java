package common;

import java.util.Map;
import java.util.HashMap;

/**
 * This static class enables the client to retrieve the equivalent subnet
 * address in its full notational network format by querying by its
 * corresponding CIDR number (classless inter-domain routing).
 * 
 * <p>
 * Platform: *NIX and Windows operating systems.
 * </p>
 * 
 * <p>
 * <b>Change logs:</b>
 * </p>
 * 
 * <ul>
 * <li>Trevor Maggs created on 23 August 2017</li>
 * </ul>
 * 
 * @author Trevor Maggs
 * @version 0.1
 * @since 23 August 2017
 */
public final class Netmask
{
    private static final Map<Integer, String> netmaskMap = new HashMap<>();

    static
    {
        netmaskMap.put(32, "255.255.255.255");
        netmaskMap.put(31, "255.255.255.254");
        netmaskMap.put(30, "255.255.255.252");
        netmaskMap.put(29, "255.255.255.248");
        netmaskMap.put(28, "255.255.255.240");
        netmaskMap.put(27, "255.255.255.224");
        netmaskMap.put(26, "255.255.255.192");
        netmaskMap.put(25, "255.255.255.128");
        netmaskMap.put(24, "255.255.255.0");
        netmaskMap.put(23, "255.255.254.0");
        netmaskMap.put(22, "255.255.252.0");
        netmaskMap.put(21, "255.255.248.0");
        netmaskMap.put(20, "255.255.240.0");
        netmaskMap.put(19, "255.255.224.0");
        netmaskMap.put(18, "255.255.192.0");
        netmaskMap.put(17, "255.255.128.0");
        netmaskMap.put(16, "255.255.0.0");
    }

    /**
     * Private constructor to prevent this static-only class from being
     * instantiated.
     */
    private Netmask()
    {
    }

    /**
     * Retrieves the corresponding subnet address in the IP notation based on
     * its CIDR notation.
     * 
     * @param cidr represents the classless inter-domain routing number. It must
     *        be between 16 and 30
     * @return full subnet address
     */
    public static String getSubnetAddress(final int cidr)
    {
        return (netmaskMap.containsKey(cidr)) ? netmaskMap.get(cidr) : "0.0.0.0";
    }
}