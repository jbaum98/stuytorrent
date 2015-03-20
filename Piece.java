package stuytorrent;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.Random;

/**
 * represents a piece of a {@link Torrent}
 */

public class Piece {
    private final static SHA1 sha1 = new SHA1();

    private final int index;
    private final int length;
    private final byte[] hash;

    private final Interval status;

    public Piece(int index, int length, byte[] hash) {
        this.index = index;
        this.length = length;
        this.hash = hash;
        status = new Interval(length);
    }

    public synchronized void setData(byte[] block) {
    }

    private void setBytes(int begin, byte[] block) {
    }


    public byte[] getBytes() {
        return new byte[0];
    }

    private byte[] calculateHash() {
        return new byte[0];
    }

    public String toString() {
        return "Piece: " + index;
    }

}
