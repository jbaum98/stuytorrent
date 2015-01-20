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
    public final byte[] bitfield;

    public BitfieldMessage(byte[] bitfield) {
        this.bitfield = bitfield;
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
