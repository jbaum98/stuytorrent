package stuytorrent.test;

import java.util.Arrays;
import java.io.OutputStream;

public class OutputStreamMock extends OutputStream {

    public void write(byte[] b) {
        System.out.println("wrote " + Arrays.toString(b) + " to stream");
    }

    public void write(int b) {}
}
