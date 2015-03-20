package stuytorrent.peer.message;

import stuytorrent.peer.Peer;
import java.util.concurrent.atomic.AtomicBoolean;

public class Unchoke extends Message {
    public byte[] toBytes() {
        //             |  length  | id |
        byte[] bytes = {0, 0, 0, 1, 1};
        return bytes;
    }

    public Runnable action(Peer peer) {
        return new UnchokeTask(peer.peer_choking);
    }

    public String toString() {
        return "Unchoke";
    }
}

class UnchokeTask implements Runnable {
    private final AtomicBoolean peer_choking;

    public UnchokeTask(AtomicBoolean peer_choking) {
        this.peer_choking = peer_choking;
    }

    public void run() {
        peer_choking.set(false);
    }
}
