package peer.message;

public class Unchoke extends Message {
    public byte[] toBytes() {
        //             |  length  | id |
        byte[] bytes = {0, 0, 0, 1, 1};
        return bytes;
    }

    //public void action(Peer peer) {
        //peer.receiveUnchoke();
    //}

    public String toString() {
        return "Unchoke";
    }
}
