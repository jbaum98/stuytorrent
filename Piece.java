import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class Piece {
    private byte[] data;
    private String hash;

    public Piece(String hash) {
        this.hash = hash;
    }

    public Piece(String hash, int length) {
        this(hash);
        this.data = new byte[length];
    }

    public void setData(byte[] data) {
        setData(data, 0);
    }

    public void setData(byte[] data, int offset) {
        for (int i = offset; i < data.length; i++) {
            this.data[i] = data[i-offset];
        }
    }

    public byte[] getData() {
        return getData(0);
    }

    public byte[] getData(int offset) {
        return Arrays.copyOfRange(data, offset, data.length);
    }

    public boolean isComplete() throws NoSuchAlgorithmException {
        String calculated_hash = calculateHash();
        return calculated_hash.equals(this.hash);
    }

    private String calculateHash() throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        return byteArray2Hex(md.digest(data));
    }

    private static String byteArray2Hex(byte[] hash) {
        Formatter formatter = new Formatter();
        for (byte b : hash) {
            formatter.format("%02x", b);
        }
        return formatter.toString();
    }

    public static void main(String[] args) throws Exception {
        Piece p = new Piece("a9993e364706816aba3e25717850c26c9cd0d89d", 3); // hash for 'abc'
        byte[] b = "abc".getBytes(StandardCharsets.US_ASCII);
        p.setData(b);
        if (p.isComplete()) {
            System.out.println("fuck yeah");
        } else {
            System.out.println("didn't work:");
            System.out.println("expected hash to be: " + p.hash);
            System.out.println("calculated: " + p.calculateHash());
        }
    }
}
