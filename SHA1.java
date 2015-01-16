import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;

public class SHA1 {
    private static MessageDigest md = null;

    public SHA1() {
        if (md == null) {
            try {
                md = MessageDigest.getInstance("SHA-1");
            } catch (NoSuchAlgorithmException e) {
                System.out.println("You're missing SHA1");
                System.out.println(e.getMessage());
            }
        }
    }

    public String digest(String s) {
        return digest(s.getBytes());
    }

    public String digest(byte[] bytes) {
        return byteArray2Hex(md.digest(bytes));
    }

    private static String byteArray2Hex(byte[] hash) {
        Formatter formatter = new Formatter();
        for (byte b : hash) {
            formatter.format("%02x", b);
        }
        return formatter.toString();
    }
}
