package common;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Performs the hash computation to generate a checksum of the specified file using one of the three
 * possible standard message digest or hashing algorithms: MD5, SHA-1 or SHA-256.
 * 
 * All available methods are accessed in a static way and the class itself cannot be instantiated.
 * 
 * <p>
 * Platform: Windows and *NIX operating system
 * </p>
 * 
 * <p>
 * <b>Change logs:</b>
 * </p>
 * 
 * <ul>
 * <li>Trevor Maggs created on 28 April 2017</li>
 * </ul>
 * 
 * @author Trevor Maggs
 * @version 0.1
 * @since 28 April 2017
 */
public final class FileChecksum
{
    /**
     * Private constructor to prevent this static-only class from being instantiated.
     */
    private FileChecksum()
    {
    }

    /**
     * Performs the hash computation to obtain the checksum based on the specified hashing
     * algorithm.
     * 
     * @param digest
     *        the MessageDigest object representing the specified hashing algorithm, either MD5,
     *        SHA1 or SHA256
     * @param pfile
     *        the Path object representing the file to be check-summed
     * 
     * @return the computed checksum in a hexadecimal format
     * @throws IOException
     *         if an I/O error is caused by {@code Files.readAllBytes()}
     */
    private static String generateChecksum(MessageDigest digest, Path pfile) throws IOException
    {
        digest.update(Files.readAllBytes(pfile));

        // return DatatypeConverter.printHexBinary(digest.digest()).toLowerCase();
        return Generic.encodeHexString(digest.digest(), false);
    }

    /**
     * Gets the MD5 checksum of the specified file.
     * 
     * @param pfile
     *        the Path representing the file to be checked for integrity
     * 
     * @return the computed checksum in a hexadecimal format
     * @throws NoSuchAlgorithmException
     *         if there is a problem when attempting to compute with the hashing algorithm
     * @throws IOException
     *         if there is an I/O error
     */
    public static String getFileMD5checksum(Path pfile) throws NoSuchAlgorithmException, IOException
    {
        MessageDigest md5 = MessageDigest.getInstance("MD5");

        return generateChecksum(md5, pfile);
    }

    /**
     * Gets the MD5 checksum of the specified file.
     * 
     * @param file
     *        the file to be checked for integrity
     * 
     * @return the computed checksum in a hexadecimal format
     * @throws NoSuchAlgorithmException
     *         if there is a problem when attempting to compute with the hashing algorithm
     * @throws IOException
     *         if there is an I/O error
     */
    public static String getFileMD5checksum(String file) throws NoSuchAlgorithmException, IOException
    {
        return getFileMD5checksum(Paths.get(file));
    }

    /**
     * Gets the SHA1 checksum of the specified file.
     * 
     * @param pfile
     *        the Path representing the file to be checked for integrity
     * 
     * @return the computed checksum in a hexadecimal format
     * @throws NoSuchAlgorithmException
     *         if there is a problem when attempting to compute with the hashing algorithm
     * @throws IOException
     *         if there is an I/O error
     * 
     */
    public static String getFileSHA1checksum(Path pfile) throws NoSuchAlgorithmException, IOException
    {
        MessageDigest sha1 = MessageDigest.getInstance("SHA-1");

        return generateChecksum(sha1, pfile);
    }

    /**
     * Gets the SHA1 checksum of the specified file.
     * 
     * @param file
     *        the file to be checked for integrity
     * 
     * @return the computed checksum in a hexadecimal format
     * @throws NoSuchAlgorithmException
     *         if there is a problem when attempting to compute with the hashing algorithm
     * @throws IOException
     *         if there is an I/O error
     */
    public static String getFileSHA1checksum(String file) throws NoSuchAlgorithmException, IOException
    {
        return getFileSHA1checksum(Paths.get(file));
    }

    /**
     * Gets the SHA256 checksum of the specified file.
     *
     * @param pfile
     *        the Path representing the file to be checked for integrity
     * 
     * @return the computed checksum in a hexadecimal format
     * @throws NoSuchAlgorithmException
     *         if there is a problem when attempting to compute with the hashing algorithm
     * @throws IOException
     *         if there is an I/O error
     * 
     */
    public static String getFileSHA256checksum(Path pfile) throws NoSuchAlgorithmException, IOException
    {
        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");

        return generateChecksum(sha256, pfile);
    }

    /**
     * Gets the SHA256 checksum of the specified file.
     * 
     * @param file
     *        the file to be checked for integrity
     * 
     * @return the computed checksum in a hexadecimal format
     * @throws NoSuchAlgorithmException
     *         if there is a problem when attempting to compute with the hashing algorithm
     * @throws IOException
     *         if there is an I/O error
     * 
     */
    public static String getFileSHA256checksum(String file) throws NoSuchAlgorithmException, IOException
    {
        return getFileSHA256checksum(Paths.get(file));
    }
}