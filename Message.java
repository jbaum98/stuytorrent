public abstract class Message {
    public static KeepAlive keepAlive() {
        return new KeepAlive();
    }

    public static Choke choke() {
        return new Choke();
    }

    public static Unchoke unchoke() {
        return new Unchoke();
    }

    public static Interested interested() {
        return new Interested();
    }

    public static NotInterested notInterested() {
        return new NotInterested();
    }

}

class KeepAlive extends Message {}

class Choke extends Message {}

class Unchoke extends Message {}

class Interested extends Message {}

class NotInterested extends Message {}

class Have extends Message {
    public final int piece_index;

    public Have(int piece_index) {
        this.piece_index = piece_index;
    }
}

class BitfieldMessage extends Message {
    public final boolean[] bitfield;

    public BitfieldMessage(byte[] bytes) throws IllegalArgumentException {
        bitfield = new boolean[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            if (bytes[i] == 0 || bytes[i] == 1) {
                bitfield[i] = bytes[i] == 1;
            } else {
                throw new IllegalArgumentException("Input consists of non-binary data");
            }
        }
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
}

class PieceMessage {
    public final int index;
    public final int begin;
    public final byte[] block;

    public PieceMessage(int index, int begin, byte[] block) {
        this.index = index;
        this.begin = begin;
        this.block = block;
    }
}
