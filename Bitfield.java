import java.util.Arrays;

/**
 * Stores information on which pieces a peer possesses
 */
public class Bitfield {
    private boolean[] bits;

    public Bitfield(int pieces) {
        bits = new boolean[pieces];
    }

    public Bitfield(BitfieldMessage message) {
        bits = message.bitfield;
    }

    /**
     * @return a copy of {@link #bits}
     */
    public boolean[] getBits() {
        return Arrays.copyOf(bits, bits.length);
    }

    public boolean isPresent(int piece_index) {
        return bits[piece_index];
    }

    public void setPresent(int piece_index) {
        bits[piece_index] = true;
    }

    public void setAbsent(int piece_index) {
        bits[piece_index] = false;
    }

    public void setPresence(int piece_index, boolean status) {
        bits[piece_index] = status;
    }

}
