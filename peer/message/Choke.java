package peer.message;

public class Choke extends Message {
    public byte[] toBytes() {
        //             |  length  | id |
        byte[] bytes = {0, 0, 0, 1, 0  };
        return bytes;
    }

    //public void action(Peer peer) {
        //peer.receiveChoke();
    //}

    public String toString() {
        return "Choke";
    }
}
