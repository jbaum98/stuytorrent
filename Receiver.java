/* * receives messages from a Peer */ 
import java.util.concurrent.LinkedBlockingQueue;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.IOException;

class MessageHandler extends LoopThread {
    private Receiver receiver;

    public MessageHandler(Receiver receiver) {
        this.receiver = receiver;
    }

    public void task() {
        try {
            System.out.println(receiver.take());
        } catch (InterruptedException e) {}
    }

    public void cleanup() {}
}

public class Receiver extends LoopThread {
    private Peer peer;
    private BufferedReader in;
    private LinkedBlockingQueue<String> messages = new LinkedBlockingQueue<String>(); // works as a buffer, you add to front and take from back
    private MessageHandler handler;

    public Receiver(Peer peer) throws IOException {
        this.peer = peer;
        in = new BufferedReader(new InputStreamReader(peer.socket.getInputStream())); // open a Reader to read from the Socket
        handler = new MessageHandler(this);
        handler.start();
    }

    protected void task() throws IOException { // see LoopThread.java
        String received;
        synchronized (in) {
            received = in.readLine(); // waits here until the other guy writes to his Socket
        }
        if (received == null) { // when the Socket is closed, it reads nulls
            interrupt();
        } else {
            messages.offer(received);
        }
    }

    protected void cleanup() throws IOException { // see LoopThread.java
        handler.interrupt();
        try {
            handler.join();
        } catch (InterruptedException e) {}
        in.close();
    }

    public String poll() {
        return messages.poll();
    }

    public String take() throws InterruptedException {
        return messages.take();
    }
}
