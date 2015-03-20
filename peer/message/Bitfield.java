package stuytorrent.peer.message;

import java.util.Arrays;
import stuytorrent.peer.Peer;

public class Bitfield extends Message {
    public final byte[] bitfield;

    public Bitfield(byte[] bytes) {
        this.bitfield = bytes;
    }

    public byte[] toBytes() {
        int length = 1 + bitfield.length;
        byte[] l = intToBytes(length);
        //             |  length  | id |
        byte[] start = {l[0], l[1], l[2], l[3], 5};
        byte[] bytes = new byte[5+bitfield.length];

        // copy start
        for(int i = 0; i < start.length; i++) {
            bytes[i] = start[i];
        }

        // copy bitfield
        for(int i = start.length; i < bytes.length; i++) {
            bytes[i] = bitfield[i-start.length];
        }

        return bytes;

    }

    public Runnable action(Peer peer) {
        return new BitfieldTask(peer.bitfield, bitfield);
    }

    public String toString() {
        return "Bitfield " + Arrays.toString(bitfield);
    }
}

class BitfieldTask implements Runnable {
    private final stuytorrent.Bitfield bitfield;
    private final byte[] bytes;

    public BitfieldTask(stuytorrent.Bitfield bitfield, byte[] bytes) {
        this.bitfield = bitfield;
        this.bytes = bytes;
    }

    public void run() {
        bitfield.override(bytes);
    }
}
