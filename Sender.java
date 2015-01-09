import java.util.concurrent.LinkedBlockingQueue;
import java.io.PrintWriter;
import java.io.OutputStream;
import java.io.IOException;
import java.net.Socket;

public class Sender extends LoopThread {
    private Peer peer;
    private PrintWriter out;
    public LinkedBlockingQueue<String> messages = new LinkedBlockingQueue<String>();

    public Sender(Peer peer) throws IOException {
        this.peer = peer;
        out = new PrintWriter(peer.socket.getOutputStream(), true);
    }

    protected void task() throws IOException, InterruptedException {
        out.println(messages.take());
    }

    public boolean send(String message) {
        return messages.offer(message);
    }

    protected void cleanup() throws IOException {
        out.close();
    }
}
