import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;
import java.util.Arrays;
import java.nio.charset.StandardCharsets;

/**
 * handles SHA1 hashing
 */

public class SHA1 {
    private MessageDigest md;

    public SHA1() {
        try {
            md = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("You're missing SHA1",e);
        }
    }

    public byte[] digest(String s) {
        return digest(s.getBytes(StandardCharsets.ISO_8859_1));
    }

    public byte[] digest(byte[] bytes) {
        byte[] digest = md.digest(bytes);
        md.reset();
        return digest;
    }

    /** converts an SHA1 digest as a String of hex bytes to byte[] */
    public static byte[] hexToBytes(String hex) {
        hex = hex.toUpperCase();
        byte[] out = new byte[hex.length() / 2];
        for (int i = 0; i+1< hex.length(); i+=2) {
            out[i/2] = hexTwo(hex.substring(i,i+2));
        }
        return out;
    }

    /** converts a String of a 2 digit hex to a byte */
    private static byte hexTwo(String hex) {
        return (byte)(hex(hex.charAt(0))*16 + hex(hex.charAt(1)));
    }

    /** converts a single hex digit to a byte */
    private static byte hex(char hex) {
        char[] digits = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
        int i = Arrays.binarySearch(digits, hex);
        if (i >= 0) {
            return (byte)(i);
        } else {
            throw new IllegalArgumentException(""+hex+" is not a hex digit");
        }
    }

    public static String bytesToHex(byte[] bytes) {
        Formatter formatter = new Formatter();
        for (byte b : bytes) {
            formatter.format("%%%02x", b);
        }
        return formatter.toString();
    }

    public static void main(String[] args) {
        String hex = "a999";
        byte[] expect = {-87, -103};
        byte[] out = SHA1.hexToBytes(hex);
        if (Arrays.equals(expect, out)) {
            System.out.println("yay");
        } else {
            System.out.println("nay");
            System.out.println(Arrays.toString(out));
        }
    }
}
