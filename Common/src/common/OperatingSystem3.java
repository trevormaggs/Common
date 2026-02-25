
package common;

/**
 * Defines the mapping of enumerated Operating System constants. Each named constant corresponds to
 * a human-readable Operating System name and its OS version.
 * 
 * Note, the constructor for this enumeration is implicitly invoked when the Java program is
 * executed.
 * 
 * <p>
 * <b>Change logs:</b>
 * </p>
 * 
 * <ul>
 * <li>Added on 10 February 2021</li>
 * </ul>
 * 
 * @author Trevor Maggs
 * @version 0.1
 * @since 10 February 2021
 * @see <a target="_top" href=
 *      "http://hg.openjdk.java.net/jdk8u/jdk8u/jdk/file/tip/src/windows/native/java/lang/java_props_md.c">See
 *      source code java_props_md.c for actual Windows OS name strings</a>
 */
public enum OperatingSystem3
{
    /* Also see https://www.techthoughts.info/windows-version-numbers */

    LNX("Linux", 0.0),
    AIX("AIX", 0.0),
    CENTOS("CentOS", 0.0),
    DEBIAN("Debian", 0.0),
    FEDORA("Fedora", 0.0),
    FREEBSD("FreeBSD", 0.0),
    HPUX("HP-UX", 0.0),
    OVM("Oracle VM", 0.0),
    RHEL("Red Hat", 0.0),
    SOLARIS("Solaris", 0.0),
    SUNOS("Sun OS", 0.0),
    SUSE("SuSE", 0.0),
    SLES("Sles", 0.0),
    UBN("Ubuntu", 0.0),
    WIN2022("Windows Server 2022", 10.0),
    WIN2019("Windows Server 2019", 10.0),
    WIN2016("Windows Server 2016", 10.0),
    WIN11("Windows 11", 10.0),
    WIN10("Windows 10", 10.0),
    WIN2012R2("Windows Server 2012 R2", 6.3),
    WIN81("Windows 8.1", 6.3),
    WIN2012("Windows Server 2012", 6.2),
    WIN8("Windows 8", 6.2),
    WIN2008R2("Windows Server 2008 R2", 6.1),
    WIN7("Windows 7", 6.1),
    WIN2008("Windows Server 2008", 6.0),
    WINVISTA("Windows Vista", 6.0),
    WIN2003("Windows 2003", 5.2),
    WINXP64("Windows XP", 5.2), /* Windows XP Professional x64 Edition */
    WINXP("Windows XP", 5.1),
    WIN2000("Windows 2000", 5.0),
    WINME("Windows Me", 4.90),
    WIN98("Windows 98", 4.10),
    WINNT4("Windows NT", 4.0),
    WIN95("Windows 95", 4.0),
    NT351("Windows NT", 3.51),
    NT35("Windows NT", 3.5),
    NT31("Windows NT", 3.1);
    /*
     * Note, Windows Server 2003 R2 also exists. Use native code to check.
     * Refer to MSDN for coding details.
     */

    private String realname;
    private double version;

    OperatingSystem3(String name, double ver)
    {
        realname = name;
        version = ver;
    }

    /**
     * Returns the name of the Operating System mapped to by this named constant.
     * 
     * @return real name of the Operating System
     */
    public String getRealName()
    {
        return realname;
    }

    /**
     * Returns the version of the Operating System mapped to by this named constant.
     * 
     * @return the Operating System version. Note, if the Operating System is not Windows, a value
     *         of zero will be returned. To get the current OS version for other OS types, use the
     *         {@code SystemInfo} static class
     */
    public double getVersion()
    {
        return version;
    }

    /**
     * Searches for a matching Operating System enumeration value based on the specified abbreviated
     * name string, such as {@code win2012r2} or {@code WIN10} etc. The input can be a string
     * literal or a simple regular expression.
     *
     * @param osName
     *        The Operating System name in its short or abbreviated name format
     * @return An enumeration value if a match is found, otherwise null
     */
    public static OperatingSystem3 getName(String osName)
    {
        for (OperatingSystem3 os : OperatingSystem3.values())
        {
            if (PatternMatch.matches(osName, os.name(), true))
            {
                return os;
            }
        }

        return null;
    }

    /**
     * Searches for a matching Operating System enumeration value based on the specified full real
     * name string, such as {@code Windows Server 2008 R2}. The input can be a string literal or a
     * simple regular expression.
     *
     * @param osName
     *        the Operating System name in its full real name format
     * @return An enumeration value if a match is found, otherwise null
     */
    public static OperatingSystem3 getNameExtended(String osName)
    {
        for (OperatingSystem3 os : OperatingSystem3.values())
        {
            if (PatternMatch.matches(osName, os.getRealName(), true))
            {
                return os;
            }
        }

        return null;
    }
}