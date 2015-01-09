import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.IOException;
import java.net.Socket;

public class Receiver extends LoopThread {
    private Peer peer;
    public BufferedReader in;

    public Receiver(Peer peer) throws IOException {
        this.peer = peer;
        in = new BufferedReader(new InputStreamReader(peer.socket.getInputStream()));
    }

    protected void task() throws IOException {
        String received;
        synchronized (in) {
            received = in.readLine();
        }
        if (received == null) {
            interrupt();
        } else {
            System.out.println(received + " from " + peer);
        }
    }

    public void cleanup() throws IOException {
        in.close();
    }
}
