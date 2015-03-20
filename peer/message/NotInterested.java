package peer.message;

public class NotInterested extends Message {
    public byte[] toBytes() {
        //             |  length  | id |
        byte[] bytes = {0, 0, 0, 1, 3};
        return bytes;
    }

    //public void action(Peer peer) {
        //peer.receiveNotInterested();
    //}

    public String toString() {
        return "Not Interested";
    }
}
