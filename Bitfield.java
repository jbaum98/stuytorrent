package stuytorrent;

import java.util.Arrays;

/**
 * Stores information on which pieces a peer possesses
 */
public class Bitfield {
    private boolean[] bits;
    private boolean done;

    public Bitfield() {
        bits = new boolean[0];
    }

    public Bitfield(int pieces) {
        bits = new boolean[pieces];
    }

    public Bitfield(stuytorrent.peer.message.Bitfield message) {
        bits = bytesToBits(message.bitfield);
    }

    public synchronized void override(byte[] bytes) {
        boolean[] incoming = bytesToBits(bytes);
        for (int i = 0; i < bits.length; i++) {
            bits[i] = incoming[i];
        }
        //bits[bits.length-1] = false;
        //bits[bits.length-2] = false;
        // bits[bits.length-3] = false;
        //System.out.println(Arrays.toString(bits));
    }
    
    private boolean[] bytesToBits(byte[] bytes) {
        boolean[] bits = new boolean[bytes.length * 8];
        for (int i = 0; i < bytes.length; i++) {
            byte b = bytes[i];
            for(int j = 7; j >= 0; j--) {
                bits[8*(i) + j] = (b & 1) == 1;
                b >>>= 1;
            }
        }
        return bits;
    }

    /**
     * @return a copy of {@link #bits}
     */
    public synchronized boolean[] getBits() {
        return Arrays.copyOf(bits, bits.length);
    }
    
    public synchronized boolean isPresent(int piece_index) {
        return bits[piece_index];
    }
    
    public synchronized void setPresent(int piece_index) {
        bits[piece_index] = true;
    }
    
    public synchronized void setAbsent(int piece_index) {
        bits[piece_index] = false;
    }
    
    public synchronized void setPresence(int piece_index, boolean status) {
        bits[piece_index] = status;
    }
    
    public static void main(String[] args) {
        Bitfield b = new Bitfield(75);
        byte[] bytes = {-1, 0, -1, -17, -32, -1, -1, -1, -1, -32};
        b.override(bytes);
    }
        
}
