import java.io.IOException;

class Death extends Thread {
    private static final int DEFAULT_TIMEOUT = 2;
    private Peer peer;
    private int timeout;

    public Death(Peer peer, double timeout_mins) {
        this.peer = peer;
        this.timeout = (int)(timeout_mins * 60 * 1000);
    }

    public Death(Peer peer) {
        this(peer, DEFAULT_TIMEOUT);
    }

    public void run() {
        sleep();
        killPeer();
    }

    private void killPeer() {
        try {
            peer.close();
        } catch (IOException e) {
            e.printStackTrace(System.out);
        }
    }

    private void sleep() {
        try {
            Thread.sleep(timeout);
        } catch (InterruptedException e) {}
    }
}
