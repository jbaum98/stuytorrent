import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.Random;

/**
 * represents a piece of a {@link Torrent}
 */

public class Piece {
    private byte[] data;
    private final byte[] hash;
    private final static SHA1 sha1 = new SHA1();
    private final Torrent torrent;
    private ArrayList<Request> need;
    private Request outstanding;
    private final AtomicBoolean done;
    public final int index;

    public Piece(byte[] hash, int length, int index, Torrent torrent) {
        this.torrent = torrent;
        this.hash = hash;
        this.data = new byte[length];
        this.index = index;
        this.done = new AtomicBoolean(false);
        fillNeed();
    }

    private void fillNeed() {
        this.need = new ArrayList<Request>();

        int num_chunks = data.length / 16384;
        int overflow = data.length % 16384;

        if (overflow == 0) {
            num_chunks--;
            overflow = 16384;
        }

        for (int i = 0; i < num_chunks; i++) {
            need.add(new Request(index, i*16384, 16384));
        }
        need.add( new Request(index, num_chunks * 16384, overflow) );
    }

    public synchronized void setData(byte[] block) {
        if (!done()) {
            setBytes(outstanding.begin, block);
            need.remove(outstanding);
            checkDone();
            if (done()) {
                outstanding = null;
            } else {
                outstanding = need.get((new Random()).nextInt(need.size()));
            }
        }
    }

    private void setBytes(int begin, byte[] block) {
        if (!done()) {
            for (int i = 0; i < block.length; i++) {
                data[begin + i] = block[i];
            }
        }
    }

    private void checkDone() {
        if (need.size() == 0) {
            if (Arrays.equals(hash, calculateHash()) ) {
                done.set(true);
                torrent.checkDone();
            } else {
                reset();
            }
        }
    }

    public byte[] getBytes() {
        return getBytes(0, data.length);
    }

    public synchronized byte[] getBytes(int offset, int length) {
        return Arrays.copyOfRange(data, offset, offset+length);
    }

    public synchronized Request getOutstandingRequest() {
        return outstanding;
    }

    private synchronized void reset() {
        done.set(false);
        fillNeed();
        data = new byte[data.length];
    }

    private byte[] calculateHash() {
        synchronized (data) {
            return sha1.digest(data);
        }
    }

    public String toString() {
        return "Piece: " + index;
    }

    public boolean done() {
        return done.get();
    }
}
