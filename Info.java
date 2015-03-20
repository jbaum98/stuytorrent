package stuytorrent;

import java.util.HashMap;
import java.util.Arrays;

public abstract class Info {
    public abstract int piece_length();
    public abstract byte[]    pieces();
    public abstract String    name();
    public abstract long       length();

    public byte[] hash(int piece_index) {
        return Arrays.copyOfRange(pieces(), piece_index*20, (piece_index-1)*20);
    }

    public int num_pieces() {
        return (int) (length() / piece_length() + 1);
    }

    public int overflow() {
        return (int) (length() % piece_length());
    }
}

class InfoSingle extends Info {
    private final int piece_length;
    private final byte[]    pieces;
    private final String    name;
    private final long      length;

    public InfoSingle(HashMap<String, BencodingObj> map) {
        piece_length = (int) map.get("piece length").value;
        pieces = ((String) map.get("pieces").value).getBytes(Globals.CHARSET);
        name = (String) map.get("name").value;
        length = (long) map.get("length").value;
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

    public long length() {
        return length;
    }
}
