package stuytorrent.peer.message;

public class Interested extends Message {
    public byte[] toBytes() {
        //             |  length  | id |
        byte[] bytes = {0, 0, 0, 1, 2};
        return bytes;
    }

    //public void action(Peer peer) {
        //peer.receiveInterested();
    //}

    public String toString() {
        return "Interested";
    }
}
