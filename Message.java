public abstract class Message {
    public abstract byte[] toBytes();
}

class KeepAlive extends Message {
    public byte[] toBytes() {
        byte[] bytes = {0, 0, 0, 0};
        return bytes;
    }
}

class Choke extends Message {
    public byte[] toBytes() {
        //             |  length  | id |
        byte[] bytes = {0, 0, 0, 1, 0  };
        return bytes;
    }
}

class Unchoke extends Message {
    public byte[] toBytes() {
        //             |  length  | id |
        byte[] bytes = {0, 0, 0, 1, 1};
        return bytes;
    }
}

class Interested extends Message {
    public byte[] toBytes() {
        //             |  length  | id |
        byte[] bytes = {0, 0, 0, 1, 2};
        return bytes;
    }
}

class NotInterested extends Message {
    public byte[] toBytes() {
        //             |  length  | id |
        byte[] bytes = {0, 0, 0, 1, 3};
        return bytes;
    }
}

class Have extends Message {
    public final Binteger piece_index;

    public Have(int integer) {
        this.piece_index = new Binteger(integer);
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
            bytes[i] = (byte) (bitfield[i-start.length] ? 1 : 0);
        }

        return bytes;

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
        //             |  length  | id |
        byte[] bytes = {0, 0, 0, 13, 6,
            index.bytes[0],  index.bytes[1],  index.bytes[2],  index.bytes[3],
            begin.bytes[0],  begin.bytes[1],  begin.bytes[2],  begin.bytes[3],
            length.bytes[0], length.bytes[1], length.bytes[2], length.bytes[3],
        };
        return bytes;
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
}
