import java.io.OutputStream;
import java.util.Arrays;
import stuytorrent.peer.Sender;

public class OutputStreamMock extends OutputStream {

    public void write(byte[] b) {
        System.out.println("wrote " + Arrays.toString(b) + " to stream");
    }

    public void write(int b) {}
}
