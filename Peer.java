import java.net.Socket;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class PeerRunner implements Runnable{
    private Socket socket;

    public PeerRunner(Socket socket) {
        this.socket = socket;
    }

    public void run() {}
}
