package stuytorrent.peer;

import java.io.IOException;

/**
 * closses a {@link Peer} after waiting some time
 * to ensure that {@link Peer}s who do not send keep-alive messages are discarded
 */

public class KillPeerTask implements Runnable {
    private final Peer peer;

    public KillPeerTask(Peer peer) {
        this.peer = peer;
    }

    public void run() {
        peer.shutdownReceiver();
        peer.shutdownSender();
        peer.shutdown();
        peer.removeFromPeerList();
        System.out.println("KillPeerTask killed " + peer);
    }
}
