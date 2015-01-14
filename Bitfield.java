import java.util.Arrays;

public class Bitfield {
    private boolean[] bits;

    public Bitfield(int pieces) {
        bits = new boolean[pieces];
    }

    public boolean[] getBits() {
        return Arrays.copyOf(bits, bits.length);
    }

    public boolean isPresent(int offset) {
        return bits[offset];
    }

    public void setPresent(int offset) {
        bits[offset] = true;
    }

    public void setAbsent(int offset) {
        bits[offset] = false;
    }

    public void setPresence(int offset, boolean status) {
        bits[offset] = status;
    }

}
