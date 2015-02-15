import java.util.HashMap;
import java.util.Arrays;

public abstract class Info {
    public abstract long piece_length();
    public abstract byte[]    pieces();
    public abstract String    name();
    public abstract long       length();

    public byte[] hash(int piece_index) {
        return Arrays.copyOfRange(pieces(), piece_index*20, (piece_index-1)*20);
    }
}

class InfoSingle extends Info {
    private final long piece_length;
    private final byte[]    pieces;
    private final String    name;
    private final long       length;

    public InfoSingle(HashMap<String, BencodingObj> map) {
        piece_length = (long) map.get("piece length").value;
        pieces = ((String) map.get("pieces").value).getBytes(Globals.CHARSET);
        name = (String) map.get("name").value;
        length = (long) map.get("length").value;
    }

    public long piece_length() {
        return piece_length;
    }

    public byte[] pieces() {
        return pieces;
    }

    public String name() {
        return name;
    }

    public long length() {
        return length;
    }
}
