import java.util.HashMap;
import java.nio.charset.StandardCharsets;
import java.nio.charset.Charset;

public interface Info {
    public int piece_length();
    public byte[]    pieces();
    public String    name();
    public int       length();
}

class InfoSingle implements Info {
    private static final Charset CHARSET = StandardCharsets.ISO_8859_1;
    private final int piece_length;
    private final byte[]    pieces;
    private final String    name;
    private final int       length;

    public InfoSingle(HashMap map) {
        piece_length = (int) map.get("piece_length");
        pieces = ((String) map.get("pieces")).getBytes(CHARSET);
        name = (String) map.get("name");
        length = (int) map.get("length");
    }

    public int piece_length() {
        return piece_length;
    }

    public byte[] pieces() {
        return pieces;
    }

    public String name() {
        return name;
    }

    public int length() {
        return length;
    }
}
