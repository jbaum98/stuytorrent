import java.util.Arrays;

public abstract class Message {
    public abstract byte[] toBytes();
    //public abstract void action(Peer peer);
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
/*
class KeepAlive extends Message {
    public byte[] toBytes() {
        byte[] bytes = {0, 0, 0, 0};
        return bytes;
    }

    public void action(Peer peer) {
        peer.keepalive();
    };

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
    public final Binteger piece_index;

    public Have(int piece_index) {
        this.piece_index = piece_index;
    }

    public Have(byte[] bytes) {
        this.piece_index = new Binteger(bytes);
    }

    public byte[] toBytes() {
        byte[] b = piece_index.bytes;
        //             |  length  | id |  piece index        |
        byte[] bytes = {0, 0, 0, 1, 4,  b[0], b[1], b[2], b[3] };
        return bytes;
    }

    public void action(Peer peer) {
        peer.have(piece_index.integer);
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
        //             |  length  | id |
        byte[] start = {0, 0, 0, 1, 5};
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
    public final Binteger index;
    public final Binteger begin;
    public final Binteger length;

    public Request(byte[] index, byte[] begin, byte[] length) {
        this.index  = new Binteger(index);
        this.begin  = new Binteger(begin);
        this.length = new Binteger(length);
    }

    public Request(int index, int begin, int length) {
        this.index  = new Binteger(index);
        this.begin  = new Binteger(begin);
        this.length = new Binteger(length);
    }

    public byte[] toBytes() {
        //             |  length   | id |
        byte[] bytes = {0, 0, 0, 13, 6,
            index.bytes[0],  index.bytes[1],  index.bytes[2],  index.bytes[3],
            begin.bytes[0],  begin.bytes[1],  begin.bytes[2],  begin.bytes[3],
            length.bytes[0], length.bytes[1], length.bytes[2], length.bytes[3],
        };
        return bytes;
    }

    public void action(Peer peer) {
        peer.request(index.integer, begin.integer, length.integer);
    }

    public String toString() {
        return "Request <index " + index + "> <begin: " + begin + "> <length: " + length + ">";
    }
}

class PieceMessage extends Message {
    public final Binteger index;
    public final Binteger begin;
    public final byte[] block;

    public PieceMessage(byte[] index, byte[] begin, byte[] block) {
        this.index = new Binteger(index);
        this.begin = new Binteger(begin);
        this.block = block;
    }

    public PieceMessage(int index, int begin, byte[] block) {
        this.index = new Binteger(index);
        this.begin = new Binteger(begin);
        this.block = block;
    }

    public byte[] toBytes() {
        Binteger len = new Binteger(9+block.length);
        byte[] bytes = {
            len.bytes[0],  len.bytes[1],  len.bytes[2],  len.bytes[3],
            7, // id
            index.bytes[0],  index.bytes[1],  index.bytes[2],  index.bytes[3],
            begin.bytes[0],  begin.bytes[1],  begin.bytes[2],  begin.bytes[3],
        };
        return bytes;
    }

    public void action(Peer peer) {
        peer.piece(index.integer, begin.integer, block);
    }

    public String toString() {
        return "Request <index " + index + "> <begin: " + begin + ">";
    }
}
*/
