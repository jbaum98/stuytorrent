package stuytorrent.peer.message;

import stuytorrent.peer.Peer;

public class Have extends Message {
    public final int piece_index;

    public Have(int piece_index) {
        this.piece_index = piece_index;
    }

    public byte[] toBytes() {
        byte[] b = intToBytes(piece_index);
        //             |  length  | id |  piece index        |
        byte[] bytes = {0, 0, 0, 5, 4,  b[0], b[1], b[2], b[3] };
        return bytes;
    }

    public Runnable action(Peer peer) {
        return new HaveTask(peer.bitfield, piece_index);
    }

    public String toString() {
        return "Have <" + piece_index + ">";
    }
}

class HaveTask implements Runnable {
    private final stuytorrent.Bitfield bitfield;
    private final int piece_index;

    public HaveTask(stuytorrent.Bitfield bitfield, int piece_index) {
        this.bitfield = bitfield;
        this.piece_index = piece_index;
    }

    public void run() {
        bitfield.setPresent(piece_index);
    }

}
