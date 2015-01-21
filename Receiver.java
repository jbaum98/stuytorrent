import java.util.concurrent.LinkedBlockingQueue;
import java.io.BufferedInputStream;
import java.io.IOException;

public class Receiver extends LoopThread {
    private final BufferedInputStream in;
    private final Peer peer;

    public final LinkedBlockingQueue<Message> messages = new LinkedBlockingQueue<Message>();

    public Receiver(Peer peer, BufferedInputStream in) {
        this.peer = peer;
        this.in = in;
    }

    protected void task() {
        int len;
        try{
            len = in.read();
        } catch (IOException e) {
            interrupt();
            return;
        }
        if (len == 0) {
            try {
                messages.put(Message.keepAlive());
            } catch (IllegalStateException e) {
                e.printStackTrace(System.out);
            } catch (InterruptedException e) {
                /* we expect to be interrupted */
            }
            return;
        } else {
            byte[] message = new byte[len];
            try {
                in.read(message);
            } catch (IOException e) {
                interrupt();
                return;
            }
        }
    }

    protected void cleanup() {
        if (! peer.socket.isClosed()) { peer.close(); }
        interrupt();
    }
}
