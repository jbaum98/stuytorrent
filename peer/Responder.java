package stuytorrent.peer;

public class Responder {
    private final Receiver receiver;
    private final Peer     peer;

    public Responder(Receiver receiver, Peer peer) {
        this.receiver = receiver;
        this.peer     = peer;
    }

    protected void task() {
        Message message;
        try {
            message = receiver.messages.take();
        } catch (InterruptedException e) {
            interrupt();
            return;
        }
        peer.keepalive();
        if (message instanceof Request) {
            System.out.println("received" + message + " from " + peer);
        }
        message.action(peer);
    }

    protected void cleanup() {}
}
