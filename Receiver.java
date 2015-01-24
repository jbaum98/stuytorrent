import java.util.concurrent.LinkedBlockingQueue;
import java.util.Arrays;
import java.io.InputStream;
import java.io.IOException;

public class Receiver extends LoopThread {
    private final InputStream in;
    private final Peer peer;

    public final LinkedBlockingQueue<Message> messages = new LinkedBlockingQueue<Message>();

    public Receiver(Peer peer, InputStream in) {
        this.peer = peer;
        this.in = in;
    }

    protected void task() {
        int len;
        try{
            len = in.read();
        } catch (IOException e) {
            interrupt();
            return;
        }
        if (len == 0) {
            try {
                messages.put(new KeepAlive());
            } catch (InterruptedException e) {
                /* we expect to be interrupted */
            }
            return;
        } else {
            byte[] bytes = new byte[len];
            try {
                in.read(bytes);
            } catch (IOException e) {
                interrupt();
                return;
            }
            byte id = bytes[0];
            Message message = null;
            switch (id) {
                default: byte[] piece_index, bitfield, index, begin, length, block;
                         break;
                case 0: message = new Choke();
                        break;
                case 1: message = new Unchoke();
                        break;
                case 2: message = new Interested();
                        break;
                case 3: message = new NotInterested();
                        break;
                case 4:
                        piece_index = Arrays.copyOfRange(bytes, 1, 5);
                        message = new Have(piece_index);
                        break;
                case 5:
                        bitfield = Arrays.copyOfRange(bytes, 1, bytes.length);
                        message = new BitfieldMessage(bitfield);
                        break;
                case 6:
                        index  = Arrays.copyOfRange(bytes, 1, 5);
                        begin  = Arrays.copyOfRange(bytes, 5, 9);
                        length = Arrays.copyOfRange(bytes, 9, 13);
                        message = new Request(index, begin, length);
                        break;
                case 7:
                        index  = Arrays.copyOfRange(bytes, 1, 5);
                        begin  = Arrays.copyOfRange(bytes, 5, 9);
                        block  = Arrays.copyOfRange(bytes, 9, bytes.length);
                        message = new PieceMessage(index, begin, block);
                        break;

            }
            if (message != null) {
                try {
                    messages.put(message);
                } catch (InterruptedException e) {
                    /* we expect to be interrupted */
                }
            }
        }
    }

    protected void cleanup() {
        if (! peer.socket.isClosed()) { peer.close(); }
    }
}
