package stuytorrent.peer.message;

import stuytorrent.peer.Peer;
import java.util.concurrent.atomic.AtomicBoolean;

public class Choke extends Message {
    public byte[] toBytes() {
        //             |  length  | id |
        byte[] bytes = {0, 0, 0, 1, 0  };
        return bytes;
    }

    public Runnable action(Peer peer) {
        return new ChokeTask(peer.peer_choking);
    }

    public String toString() {
        return "Choke";
    }
}

class ChokeTask implements Runnable {
    private final AtomicBoolean peer_choking;

    public ChokeTask(AtomicBoolean peer_choking) {
        this.peer_choking = peer_choking;
    }

    public void run() {
        peer_choking.set(true);
    }
}
