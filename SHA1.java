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

    public String digest(String s, boolean url) {
        return digest(s.getBytes(), url);
    }

    public String digest(byte[] bytes) {
        return digest(bytes, false);
    }

    public String digest(byte[] bytes, boolean url) {
        byte[] digest = md.digest(bytes);
        return byteArray2Hex(digest, url);
    }

    private static String byteArray2Hex(byte[] hash, boolean url) {
        String base = url ? "%%" : new String();
        Formatter formatter = new Formatter();
        for (byte b : hash) {
            formatter.format(base+"%02x", b);
        }
        return formatter.toString();
    }
}
