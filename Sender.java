import java.util.ArrayList;
import java.io.PrintWriter;
import java.io.IOException;
import java.net.Socket;

public class Sender extends Runner {
    private Peer peer;
    public volatile ArrayList<String> messages = new ArrayList<String>();

    public Sender(Peer peer) {
        this.peer = peer;
    }

    public void task() {
        if (messages.size() > 0) {
            peer.out.println(pop());
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
}
