package peer.message;

public class KeepAlive extends Message {
    public byte[] toBytes() {
        byte[] bytes = {0, 0, 0, 0};
        return bytes;
    }

    //public void action(Peer peer) {};

    public String toString() {
        return "KeepAlive";
    }
}
