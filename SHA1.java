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
                e.printStackTrace(System.out);
            }
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

}
