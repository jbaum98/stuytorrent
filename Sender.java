import java.util.ArrayList;
import java.io.PrintWriter;
import java.io.OutputStream;
import java.io.IOException;
import java.net.Socket;

public class Sender extends LoopThread {
    private Peer peer;
    private PrintWriter out;
    public volatile ArrayList<String> messages = new ArrayList<String>();

    public Sender(Peer peer) throws IOException {
        super(peer);
        this.peer = peer;
        out = new PrintWriter(peer.socket.getOutputStream(), true);
    }

    protected void task() throws IOException {
        if (messages.size() > 0) {
            out.println(pop());
        }
    }

    private String pop() {
        return messages.remove(messages.size() - 1);
    }

    public void send(String message) {
        synchronized(messages) {
            messages.add(message);
        }
    }

    protected void cleanup() throws IOException {
        out.close();
    }
}
