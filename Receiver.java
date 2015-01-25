import java.util.concurrent.LinkedBlockingQueue;
import java.util.Arrays;
import java.io.DataInputStream;
import java.io.IOException;

public class Receiver extends LoopThread {
    private final DataInputStream in;
    private final Peer peer;

    public final LinkedBlockingQueue<Message> messages = new LinkedBlockingQueue<Message>(1000);

    public Receiver(Peer peer, DataInputStream in) {
        this.peer = peer;
        this.in = in;
    }

    protected void task() {
        try{
            int len = in.readInt();
            if (len < 0) {
                peer.close();
                interrupt();
                return;
            } else if (len == 0) {
                try {
                    messages.put(new KeepAlive());
                } catch (InterruptedException e) {
                    interrupt();
                }
                return;
            } else {
                byte id = in.readByte();
                Message message = null;
                int index, begin, piece_index, length;
                switch (id) {
                case 0:
                    message = new Choke();
                    break;
                case 1:
                    message = new Unchoke();
                    break;
                case 2:
                    message = new Interested();
                    break;
                case 3:
                    message = new NotInterested();
                    break;
                case 4:
                    piece_index = in.readInt();
                    message = new Have(piece_index);
                    break;
                case 5:
                    byte[] bitfield = new byte[len-1];
                    in.readFully(bitfield);
                    message = new BitfieldMessage(bitfield);
                    break;
                case 6:
                    index  = in.readInt();
                    begin  = in.readInt();
                    length = in.readInt();
                    message = new Request(index, begin, length);
                    break;
                case 7:
                    index  = in.readInt();
                    begin  = in.readInt();
                    byte[] block  = new byte[len-9];
                    in.readFully(block);
                    message = new PieceMessage(index, begin, block);
                    break;
                    
                }
                if (message != null) {
                    try {
                        messages.put(message);
                    } catch (InterruptedException e) {
                        interrupt();
                        peer.close();
                        return;
                    }
                }
            }
        } catch (IOException e) {
            interrupt();
            peer.close();
            return;
        }
    }
    
    protected void cleanup() {
        if (! peer.socket.isClosed()) { peer.close(); }
    }
}
