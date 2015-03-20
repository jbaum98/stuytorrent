package peer.message;

public class Request extends Message {
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

    //public void action(Peer peer) {
        //peer.request(index, begin, length);
    //}

    public String toString() {
        return "Request <index " + index + "> <begin: " + begin + "> <length: " + length + ">";
    }
}
