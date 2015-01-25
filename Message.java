import java.util.Arrays;

public abstract class Message {
    public abstract byte[] toBytes();
    public abstract void action(Peer peer);
    public abstract String toString();

    protected static byte[] intToBytes(int integer) {
        byte[] out = new byte[4];
        for (int in = integer, i = out.length-1; i >= 0; in >>>= 8, i--) {
            out[i] = (byte) (in & 0xFF);
        }
        return out;
    }

    public static void main(String[] args) {
        System.out.println(Arrays.toString(intToBytes(Integer.MIN_VALUE)));
    }
}

class KeepAlive extends Message {
    public byte[] toBytes() {
        byte[] bytes = {0, 0, 0, 0};
        return bytes;
    }

    public void action(Peer peer) {};

    public String toString() {
        return "KeepAlive";
    }
}

class Choke extends Message {
    public byte[] toBytes() {
        //             |  length  | id |
        byte[] bytes = {0, 0, 0, 1, 0  };
        return bytes;
    }

    public void action(Peer peer) {
        peer.receiveChoke();
    }

    public String toString() {
        return "Choke";
    }
}

class Unchoke extends Message {
    public byte[] toBytes() {
        //             |  length  | id |
        byte[] bytes = {0, 0, 0, 1, 1};
        return bytes;
    }

    public void action(Peer peer) {
        peer.receiveUnchoke();
    }

    public String toString() {
        return "Unchoke";
    }
}

class Interested extends Message {
    public byte[] toBytes() {
        //             |  length  | id |
        byte[] bytes = {0, 0, 0, 1, 2};
        return bytes;
    }

    public void action(Peer peer) {
        peer.receiveInterested();
    }

    public String toString() {
        return "Interested";
    }
}

class NotInterested extends Message {
    public byte[] toBytes() {
        //             |  length  | id |
        byte[] bytes = {0, 0, 0, 1, 3};
        return bytes;
    }

    public void action(Peer peer) {
        peer.receiveNotInterested();
    }

    public String toString() {
        return "Not Interested";
    }
}

class Have extends Message {
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

    public void action(Peer peer) {
        peer.have(piece_index);
    }

    public String toString() {
        return "Have <" + piece_index + ">";
    }
}

class BitfieldMessage extends Message {
    public final byte[] bitfield;

    public BitfieldMessage(byte[] bytes) {
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

    public void action(Peer peer) {
        peer.bitfield(bitfield);
    }

    public String toString() {
        return "BitfieldMessage " + Arrays.toString(bitfield);
    }
}

class Request extends Message {
    public final int index;
    public final int begin;
    public final int length;

    public Request(int index, int begin, int length) {
        this.index  = index;
        this.begin  = begin;
        this.length = length;
    }

    public byte[] toBytes() {
        byte[] i = intToBytes(index);
        byte[] b = intToBytes(begin);
        byte[] l = intToBytes(length);
        //             |  length   | id |
        byte[] bytes = {0, 0, 0, 13, 6,
            i[0],  i[1],  i[2],  i[3],
            b[0],  b[1],  b[2],  b[3],
            l[0], l[1], l[2], l[3],
        };
        return bytes;
    }

    public void action(Peer peer) {
        peer.request(index, begin, length);
    }

    public String toString() {
        return "Request <index " + index + "> <begin: " + begin + "> <length: " + length + ">";
    }
}

class PieceMessage extends Message {
    public final int index;
    public final int begin;
    public final byte[] block;

    public PieceMessage(int index, int begin, byte[] block) {
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

    public void action(Peer peer) {
        peer.piece(index, begin, block);
    }

    public String toString() {
        return "Request <index " + index + "> <begin: " + begin + ">";
    }
}
