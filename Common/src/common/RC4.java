package common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;

/**
 * RC4 is a symmetric key encryption algorithm. Developed in 1987 by Ronald Rivest, it is used in
 * SSL and many applications.
 * 
 * RC4 is a stream cipher, meaning that it encrypts one byte at a time. With RC4, the key is
 * variable, from 1 to 2048 bits. RC4 is about 10 times as fast as DES. The algorithm is small and
 * simple to implement.
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
 * <li>Trevor Maggs added on 13 March 2017</li>
 * </ul>
 * 
 * @author Trevor Maggs
 * @version 0.1
 * @since 13 March 2017
 * @see <a href="http://www.codeforge.com/read/237414/RC4.java__html">Example</a>
 */
public class RC4
{
    private int[] sbox;
    private char[] keyCode;
    private static final int SBOX_LENGTH = 256;
    private static final int KEY_MIN_LENGTH = 5;

    /**
     * Constructor to set the Cipher Key Code.
     * 
     * @param key
     *        is the cipher key. Its length must be within the range KEY_MIN_LENGTH and SBOX_LENGTH
     * @throws InvalidKeyException
     *         if the key is empty or not within the required range of length
     */
    public RC4(String key) throws InvalidKeyException
    {
        if (key.trim().isEmpty())
        {
            throw new InvalidKeyException("Please supply the RC4 crypt key");
        }

        else if (!(key.length() >= KEY_MIN_LENGTH && key.length() < SBOX_LENGTH))
        {
            throw new InvalidKeyException("Key length has to be between " + KEY_MIN_LENGTH + " and " + (SBOX_LENGTH - 1));
        }

        this.keyCode = key.toCharArray();
    }

    /**
     * Required for internal use, it computes results algorithmically.
     * 
     * @param i
     *        is the position of the array
     * @param j
     *        is the other position of the array
     * @param sbox
     *        is the array of integers
     */
    private void swap(int i, int j, int[] sbox)
    {
        int temp = sbox[i];
        sbox[i] = sbox[j];
        sbox[j] = temp;
    }

    /**
     * This first stage algorithm is based on the well known key-scheduling algorithm (KSA).
     * 
     * @return array of integer values
     * @see <a href="https://en.wikipedia.org/wiki/RC4">www.en.wikipedia.org/wiki/RC4</a>
     */
    private int[] initSBox()
    {
        int j = 0;
        int[] sbox = new int[SBOX_LENGTH];

        for (int i = 0; i < SBOX_LENGTH; i++)
        {
            sbox[i] = i;
        }

        for (int i = 0; i < SBOX_LENGTH; i++)
        {
            j = (j + sbox[i] + keyCode[i % keyCode.length]) % SBOX_LENGTH;
            swap(i, j, sbox);
        }

        return sbox;
    }

    /**
     * Encrypts the text. This method uses a second stage algorithm based on the Pseudo-random
     * generation algorithm (PRGA).
     * 
     * @param ciphertext
     *        is the array of bytes representing a sequence of characters
     * 
     * @return array of bytes representing characters
     * @see <a href="https://en.wikipedia.org/wiki/RC4">www.en.wikipedia.org/wiki/RC4</a>
     */
    private byte[] encrypt(final byte[] ciphertext)
    {
        int i = 0;
        int j = 0;
        byte[] code = new byte[ciphertext.length];

        sbox = initSBox();

        for (int n = 0; n < ciphertext.length; n++)
        {
            i = (i + 1) % SBOX_LENGTH;
            j = (j + sbox[i]) % SBOX_LENGTH;
            swap(i, j, sbox);
            int rand = sbox[(sbox[i] + sbox[j]) % SBOX_LENGTH];
            code[n] = (byte) (rand ^ (int) ciphertext[n]);
        }

        return code;
    }

    /**
     * Decrypts to restore to the original text.
     * 
     * @param charBytes
     *        is the array of bytes containing the encrypted bytes
     * 
     * @return array of bytes that represent characters
     */
    private byte[] decrypt(final byte[] charBytes)
    {
        return encrypt(charBytes);
    }

    /**
     * Converts the source file to a destination file by encryption.
     * 
     * @param source
     *        original file to encrypt
     * @param destination
     *        the final encrypted file
     * 
     * @return length of characters read
     */
    public int encryptFile(String source, String destination)
    {
        StringBuilder buffer = new StringBuilder(16384);

        try (FileInputStream fpin = new FileInputStream(source); FileOutputStream fpout = new FileOutputStream(destination))
        {
            int c;

            while ((c = fpin.read()) != -1)
            {
                buffer.append((char) c);
            }

            // Write an array of bytes to the destination file after encryption
            fpout.write(encrypt(buffer.toString().getBytes()));
        }

        catch (IOException exc)
        {
            System.err.println("There was a problem with encryption. Maybe check for file existence?");
            System.exit(1);
        }

        return buffer.length();
    }

    /**
     * Decrypts the encrypted file and restores to the original file.
     * 
     * @param secretFile
     *        encrypted file to decrypt
     * @param finalFile
     *        the restored file
     * 
     * @return length of characters read
     */
    public int decryptFile(String secretFile, String finalFile)
    {
        int length = 0;

        try(FileInputStream fpin = new FileInputStream(secretFile); FileOutputStream fpout = new FileOutputStream(finalFile))
        {
            length = (int) new File(secretFile).length();

            // get an array of bytes from the encrypted file
            byte[] bytechar = new byte[length];            
            fpin.read(bytechar);

            byte[] decrypted = decrypt(bytechar);            
            fpout.write(decrypted);
        }

        catch (IOException exc)
        {
            System.err.println("There was a problem with decryption. Maybe check for file existence?");
            System.exit(1);
        }

        return length;
    }
}