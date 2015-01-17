import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;
import java.nio.charset.StandardCharsets;

public class SHA1 {
    private MessageDigest md = null;

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
        return digest(s.getBytes(StandardCharsets.ISO_8859_1));
    }

    public String digest(String s, boolean url) {
        return digest(s.getBytes(StandardCharsets.ISO_8859_1), url);
    }

    public String digest(byte[] bytes) {
        return digest(bytes, false);
    }

    public String digest(byte[] bytes, boolean url) {
        byte[] digest = md.digest(bytes);
	md.reset();
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
