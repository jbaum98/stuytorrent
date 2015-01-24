public class Responder extends LoopThread {
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
            System.out.println(message + " from " + peer);
        } catch (InterruptedException e) {
            interrupt();
            return;
        }
        message.action(peer);
    }

    protected void cleanup() {
    }
}
