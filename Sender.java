import java.io.PrintWriter;
import java.io.IOException;
import java.net.Socket;

public class Sender implements Runnable {
    private Peer peer;
    private String message;

    public Sender(Peer peer, String message) {
        this.peer = peer;
        this.message = message;
    }

    public void run() {
        synchronized (peer.out) {
            peer.out.println(message);
        }
    }
}
