package stuytorrent.peer.message;

import java.util.Arrays;
import stuytorrent.peer.Peer;

public abstract class Message {
    public abstract byte[] toBytes();
    public abstract String toString();

    public abstract Runnable action(Peer peer);

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
