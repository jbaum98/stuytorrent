package stuytorrent.peer.message;

import stuytorrent.peer.Peer;
import java.util.concurrent.atomic.AtomicBoolean;

public class NotInterested extends Message {
    public byte[] toBytes() {
        //             |  length  | id |
        byte[] bytes = {0, 0, 0, 1, 3};
        return bytes;
    }

    public Runnable action(Peer peer) {
        return new NotInterestedTask(peer.peer_interested);
    }

    public String toString() {
        return "Not Interested";
    }
}

class NotInterestedTask implements Runnable {
    private final AtomicBoolean peer_interested;

    public NotInterestedTask(AtomicBoolean peer_interested) {
        this.peer_interested = peer_interested;
    }

    public void run() {
        peer_interested.set(false);
    }
}
