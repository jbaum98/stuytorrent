package stuytorrent.peer.message;

import stuytorrent.peer.Peer;
import java.util.concurrent.atomic.AtomicBoolean;

public class Piece extends Message {
    public final int index;
    public final int begin;
    public final byte[] block;

    public Piece(int index, int begin, byte[] block) {
        this.index = index;
        this.begin = begin;
        this.block = block;
    }

    public byte[] toBytes() {
        byte[] len = intToBytes(9+block.length);
        byte[] i = intToBytes(index);
        byte[] b = intToBytes(begin);

        byte[] bytes = {
            len[0],  len[1],  len[2],  len[3],
            7, // id
            i[0],  i[1],  i[2],  i[3],
            b[0],  b[1],  b[2],  b[3],
        };
        return bytes;
    }

    public Runnable action(Peer peer) {
        return new PieceTask(index, begin, block, peer);
    }

    public String toString() {
        return "Piece <index " + index + "> <begin: " + begin + ">";
    }
}

class PieceTask implements Runnable {
    //private final Torrent torrent;
    private final Peer peer;
    private final int index;
    private final int begin;
    private final byte[] block;

    public PieceTask(int index, int begin, byte[] block, Peer peer) {
        this.index = index;
        this.begin = begin;
        this.block = block;
        this.peer = peer;
    }

    public void run() {
        peer.submitChunk(index, begin, block);
    }
}
