import java.io.IOException;

/**
 * closses a {@link Peer} after waiting some time
 * to ensure that {@link Peer}s who do not send keep-alive messages are discarded
 */

public class Death extends Thread {
    private static final int DEFAULT_TIMEOUT = 2;
    private final Peer peer;
    private final int timeout;

    public Death(Peer peer, double timeout_mins) {
        this.peer = peer;
        this.timeout = (int)(timeout_mins * 60 * 1000);
    }

    public Death(Peer peer) {
        this(peer, DEFAULT_TIMEOUT);
    }

    public void run() {
        try {
            Thread.sleep(timeout);
        } catch (InterruptedException e) {
            return; // we don't want to kill the Peer if Death is interrupted
        }
        killPeer();
    }

    private void killPeer() {
        try {
            peer.close();
        } catch (IOException e) {
            e.printStackTrace(System.out);
        }
    }
}
