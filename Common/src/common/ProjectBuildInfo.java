package common;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * The {@code ProjectBuildDate} utility class determines the active JAR library file from which the
 * current class is running. It identifies the JAR file name and retrieves the last compilation date
 * and time, effectively determining the build date of the JAR library.
 * 
 * This class captures the JAR name accurately at runtime, and if the JAR is not used, it assumes
 * the name of the current running class instead.
 * 
 * <p>
 * Change Log:
 * </p>
 * 
 * <ul>
 * <li>Initial creation on September 4, 2023</li>
 * </ul>
 * 
 * @author Trevor Maggs
 * @since 4 September 2023
 */
public class ProjectBuildInfo
{
    private Path fpath;
    private Date timestamp;

    /**
     * Constructs a private {@code ProjectBuildDate} object to analyse the active resource, which
     * can be either a JAR library or the current running class resource. This object retrieves the
     * last successful compilation or build date of the resource.
     * 
     * Note that this constructor is not intended for direct use. Instead, use the public static
     * factory method to indirectly invoke this constructor.
     * 
     * @param runningClass
     *        the current running class resource being analysed
     */
    private ProjectBuildInfo(Class<?> runningClass)
    {
        URL resource = runningClass.getResource(runningClass.getSimpleName() + ".class");

        try
        {
            readBuildInfo(resource);
        }

        catch (URISyntaxException | IOException exc)
        {
            throw new IllegalStateException("Problem found while attempting to access URL: [" + resource.getPath() + "].", exc);
        }
    }

    /**
     * Retrieves the date-time property from the manifest file (META-INF/MANIFEST.MF) within the
     * specified JAR resource.
     * 
     * @param url
     *        the URL path to the JAR resource
     * 
     * @return an instance of Date, representing the time-stamp of the manifest file
     * @throws IOException
     *         if an I/O error occurs during the retrieval process
     */
    private static Date getJarDatetime(String url) throws IOException
    {
        try (JarFile jarFile = new JarFile(url))
        {
            Enumeration<JarEntry> entries = jarFile.entries();

            while (entries.hasMoreElements())
            {
                JarEntry entry = entries.nextElement();

                if (entry.getName().equals("META-INF/MANIFEST.MF"))
                {
                    return new Date(entry.getTime());
                }
            }
        }

        return null;
    }

    /**
     * Retrieves the resource name and build time-stamp of the last successful compilation
     * associated with the specified URL resource. If the resource is a JAR library, it will be
     * identified and its build information obtained. Otherwise, the current running class resource
     * is assumed.
     * 
     * @param resource
     *        the URL instance of the active class resource
     * 
     * @throws URISyntaxException
     *         if the URI for the specified resource cannot be obtained
     * @throws IOException
     *         if an I/O error occurs during the retrieval process
     */
    private void readBuildInfo(URL resource) throws URISyntaxException, IOException
    {
        if (resource.getProtocol().equals("file"))
        {
            fpath = Paths.get(resource.toURI());;
            timestamp = new Date(Files.getLastModifiedTime(fpath).toInstant().toEpochMilli());
        }

        else if (resource.getProtocol().equals("jar"))
        {
            String path = resource.getPath();

            /*
             * Extract the path only with the "file:" prefix removed. For example on Windows:
             * 
             * Before -> file:/E:/download/ProxyFilterGUI.jar!/proxy/ProxyFilterFrame.class
             * After -> /E:/download/ProxyFilterGUI.jar
             */
            path = path.substring(path.indexOf(":") + 1, path.indexOf("!"));

            /*
             * Trims off leading slash for Windows only and
             * then decode to give valid URL strings.
             */
            String url = URLDecoder.decode(path.replaceAll("^/(\\w:/.*)$", "$1"), "UTF-8");

            fpath = Paths.get(url);
            timestamp = getJarDatetime(url);
        }

        else if (resource.getProtocol().equals("rsrc"))
        {
            /*
             * It is difficult to obtain the running JAR name when the given protocol is "rsrc". A
             * quick and dirty workaround solution is needed by obtaining from the system property.
             */
            String jarfile = System.getProperty("sun.java.command").replaceAll("\\\\", "/");
            jarfile = jarfile.replaceFirst("(\\S+/)*(\\S+\\.\\bjar\\b).*$", "$2");

            String url = URLDecoder.decode(jarfile.replaceAll("^/(\\w:/.*)$", "$1"), "UTF-8");

            fpath = Paths.get(url);
            timestamp = getJarDatetime(url);
        }

        else
        {
            throw new IllegalStateException(String.format("Unhandled URL protocol: %s for class: %s, resource: %s%n",
                    resource.getProtocol(),
                    resource.getClass().getName(),
                    resource.toString()));
        }
    }

    /**
     * Returns the full path, including the location of the actual resource.
     * 
     * @return the full path of the resource as a {@link Path} object
     */
    public Path getFullPath()
    {
        return fpath;
    }

    /**
     * Returns the name of the JAR library or the current running class resource, including any
     * extension that may be part of the full path, without modification.
     * 
     * @return the unmodified name of the resource as a {@link Path} object
     */
    public Path getFileName()
    {
        return fpath.getFileName();
    }

    /**
     * Returns the short name of the JAR library or the current running class resource, with the
     * {@code .class} extension name removed, providing a concise identifier for the resource.
     * 
     * @return the short name of the resource as a {@link Path} object, without the {@code .class}
     *         extension
     */
    public Path getShortFileName()
    {
        String ext = ".class";
        String str = fpath.getFileName().toString();

        if (str.endsWith(ext))
        {
            str = str.substring(0, str.length() - ext.length());
        }

        return Paths.get(str);
    }

    /**
     * Returns the date stamp of the resource that was captured, providing a time-stamp of when the
     * resource was last modified or compiled.
     * 
     * @return the date stamp of the resource as a Date object, representing the last modification
     *         or compilation time
     */
    public Date getFullBuildDate()
    {
        return timestamp;
    }

    /**
     * Returns the concise build date stamp of the last successful code compilation, providing a
     * more readable time-stamp of when the code was last built or compiled.
     * 
     * @return the concise build date stamp as a string in a format suitable for display
     */

    public String getBuildDate()
    {
        return new SimpleDateFormat("dd/MM/yyyy @ hh.mm a").format(timestamp);
    }

    /**
     * Returns a string representation that specifies all relevant values collected by the
     * constructor, providing a comprehensive summary of the resource's details.
     * 
     * @return a formatted textual representation of details of this resource, including all
     *         relevant values
     */
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(String.format("[%s]%n", getClass().getSimpleName()));

        sb.append(String.format("   %-20s : %s%n", "Latest Build Date", getBuildDate()));
        sb.append(String.format("   %-20s : %s%n", "Full Path", getFullPath()));
        sb.append(String.format("   %-20s : %s%n", "File Name only", getFileName()));
        sb.append(String.format("   %-20s : %s%n", "Short File Name", getShortFileName()));
        sb.append(Generic.repeatPrint("-", 60));

        return sb.toString();
    }

    /**
     * Retrieves a resource that specifies the build date stamp of the last successful compilation
     * for your Java development project. This public static factory method provides a convenient
     * way to access the build date information.
     * 
     * @param currentClass
     *        the current running class resource
     * 
     * @return an instance of ProjectBuildDate containing the resource name and build date
     *         information
     */
    public static ProjectBuildInfo getInstance(Class<?> currentClass)
    {
        return new ProjectBuildInfo(currentClass);
    }
}