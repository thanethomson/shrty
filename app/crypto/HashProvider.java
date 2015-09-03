package crypto;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import org.apache.commons.codec.binary.Hex;

/**
 * The default provider for performing hashes on strings.
 */
public class HashProvider {
  
  public final static String HASH_ALGORITHM = "SHA-256";
  
  /**
   * Creates a MessageDigest object from the given input string.
   * @param input The string for which a message digest is to be created.
   * @return The newly created message digest.
   * @throws NoSuchAlgorithmException When the desired hash algorithm is not supported by the platform.
   */
  public static MessageDigest hashOf(String input) throws NoSuchAlgorithmException {
    MessageDigest digest;
    digest = MessageDigest.getInstance(HASH_ALGORITHM);
    digest.update(input.getBytes());
    return digest;
  }
  
  /**
   * Performs the relevant hash of the given input string and returns the lowercase, hexadecimal representation
   * of the hash of the input string.
   * @param input The string from which the hash is to be generated.
   * @return The hexadecimal string.
   * @throws NoSuchAlgorithmException When the desired hash algorithm is not supported by the platform.
   */
  public static String hexHashOf(String input) throws NoSuchAlgorithmException {
    return Hex.encodeHexString(hashOf(input).digest());
  }
  
  /**
   * Returns the Base64-encoded version of the hash of the given input string.
   * @param input
   * @return
   * @throws NoSuchAlgorithmException
   */
  public static String base64HashOf(String input) throws NoSuchAlgorithmException {
    return Base64.getEncoder().encodeToString(hashOf(input).digest());
  }
  
}
