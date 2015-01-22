import java.util.Arrays;
import java.util.ArrayList;
import java.nio.ByteBuffer;

/**
 * represents an integer as both a Java <code>int</code>
 * and as a <code>byte[]</code>, with constructors for both
 */

public class Binteger {
    public final byte[] bytes;
    public final int    integer;

    public Binteger() {
        this.bytes = new byte[4];
        this.integer = 0;
    }

    public Binteger(byte[] bytes) {
        this.bytes = bytes;
        this.integer = byteToInt();
    }

    public Binteger(int integer) {
        this.integer = integer;
        this.bytes = intToBytes();
    }

    /**
     * converts {@link Binteger#bytes} to its integer representation.
     * got info from http://www.nayuki.io/page/javas-signed-byte-type-is-a-mistake
     * EXPLANATION:
     *   starts at the rightmost byte
     *   x & 0xFF unsigns and converts to int because 0xFF = 255 = 0b11111111
     *   x << y shifts all bits to the left, multiplying by 2^y. multiplying by 2*8y shifts 8 bits, aligning it with fresh zeroes in out
     *   0 | y copies y onto x
     */
    private int byteToInt() {
        int out = 0;
        for (int i = 0; i < bytes.length; i++) {
            out |= ((bytes[bytes.length - i - 1] & 0xFF) << 8*i);
        }
        return out;
    }

    /**
     * converts {@link Binteger#integer} to its hex representation.
     * got info from https://stackoverflow.com/questions/2183240/java-integer-to-byte-array
     * EXPLANATION:
     *   copies integer to in and counts the indices of out from the right
     *   in & 0xFF gets the rightmose 8 bits
     *   in <<<=8 shifts in 8 bits to the right
     */
    private byte[] intToBytes() {
        byte[] out = new byte[4];
        for (int in = integer, i = out.length-1; in > 0 && i >= 0; in >>>= 8, i--) {
            out[i] = (byte) (in & 0xFF);
        }
        return out;
    }

    public static void main(String[] args) {
        Binteger b1 = new Binteger(73459822);
        System.out.println("b1 is " + Arrays.toString(b1.bytes));
        byte[] b = {0, 0, 7, 5};
        Binteger b2 = new Binteger(b);
        System.out.println("b2 is " + b2.integer);
    }
}
