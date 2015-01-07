import java.net.Socket;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

// TODO .equals and .compareTo
public class Peer {
    private Socket socket;
    private Receiver receiver;

    public Peer(Socket socket) {
        this.socket = socket;
        this.receiver = new Receiver(socket);
    }

    public void send(String message) {
        Sender sender = new Sender(socket, message);
        (new Thread(sender)).start();
    }
}
