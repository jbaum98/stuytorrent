package stuytorrent.peer.message;

import stuytorrent.peer.Peer;

public class KeepAlive extends Message {
    public byte[] toBytes() {
        byte[] bytes = {0, 0, 0, 0};
        return bytes;
    }

    public Runnable action(Peer peer) {
        return new KeepAliveTask();
    };

    public String toString() {
        return "KeepAlive";
    }
}

class KeepAliveTask implements Runnable {
    public void run() {
        // TODO call off Death when is actually implemented
    }
}
