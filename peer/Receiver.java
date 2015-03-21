package stuytorrent.peer;

import java.io.DataInputStream;
import java.io.IOException;

import java.util.Arrays;

import stuytorrent.peer.message.*;

public class Receiver {
    private final DataInputStream in;
    private final Peer peer;
    private final Runnable killPeer;
    private boolean isRunning = true;

    public Receiver(Peer peer, DataInputStream in, Runnable killPeer) {
        this.peer = peer;
        this.killPeer = killPeer;
        this.in = in;
    }

    public void run() {
        while (isRunning) {
        try {
            int len = in.readInt();
            Message message;
            if (len < 0) {
                System.out.println("Closed because negative status");
                closePeer();
            } else if (len == 0) {
                message = new KeepAlive();
                return;
            } else {
                byte id = in.readByte();
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
                        System.out.println("interested! " + peer);
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
                        //System.out.println(Arrays.toString(bitfield));
                        message = new Bitfield(bitfield);
                        break;
                    case 6:
                        index  = in.readInt();
                        begin  = in.readInt();
                        length = in.readInt();
                        message = new Request(index, begin, length);
                        System.out.println("request from " + peer);
                        break;
                    case 7:
                        index  = in.readInt();
                        begin  = in.readInt();
                        byte[] block  = new byte[len-9];
                        in.readFully(block);
                        message = new Piece(index, begin, block);
                        break;
                    default:
                        closePeer();
                        return;
                }
                peer.takeAction(message);
            }
        } catch (IOException e) {
            System.out.println("Error on read");
            closePeer();
        }
        }
    }

    public synchronized void shutdown() {
        isRunning = false;
        try {
            in.close();
        } catch (IOException e) {
            System.out.println("Error closing InputStream");
        }
    }

    public void closePeer() {
        (new Thread(killPeer)).start();
    }
}
