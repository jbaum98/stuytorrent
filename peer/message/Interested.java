package stuytorrent.peer.message;

import stuytorrent.peer.Peer;
import java.util.concurrent.atomic.AtomicBoolean;

public class Interested extends Message {
    public byte[] toBytes() {
        //             |  length  | id |
        byte[] bytes = {0, 0, 0, 1, 2};
        return bytes;
    }

    public Runnable action(Peer peer) {
        return new InterestedTask(peer.peer_interested);
    }

    public String toString() {
        return "Interested";
    }
}

class InterestedTask implements Runnable {
    private final AtomicBoolean peer_interested;

    public InterestedTask(AtomicBoolean peer_interested) {
        this.peer_interested = peer_interested;
    }

    public void run() {
        peer_interested.set(true);
    }
}
