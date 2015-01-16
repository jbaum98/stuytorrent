/*
 * represents a Peer to which the Client is connected
 * has two jobs:
 *  - listen for incoming messages (Receiver)
 *  - send outgoing messages (Sender)
 * uses seperate Threads for both, see Receiver.java and Sender.java respectively
 */

import java.net.Socket;
import java.io.IOException;
import java.io.Closeable;

class Death extends Thread {
    private static final int DEFAULT_TIMEOUT = 2;
    private Peer peer;
    private int timeout;

    public Death(Peer peer, double timeout_mins) {
        this.peer = peer;
        this.timeout = (int)(timeout_mins * 60 * 1000);
    }

    public Death(Peer peer) {
        this(peer, DEFAULT_TIMEOUT);
    }

    public void run() {
        sleep();
        killPeer();
    }

    private void killPeer() {
        try {
            peer.close();
        } catch (IOException e) {
            e.printStackTrace(System.out);
        }
    }

    private void sleep() {
        try {
            Thread.sleep(timeout);
        } catch (InterruptedException e) {}
    }
}

public class Peer implements Closeable, AutoCloseable {
    // public  Torrent  torrent;
    public  Socket   socket;
    public  Receiver receiver;
    public  Sender   sender;
    public Death  death;

    public Peer(Socket socket) throws IOException {
        this.socket = socket;
        startReceiver();
        startSender();
        startDeath();
    }

    private void startReceiver() throws IOException {
        receiver = new Receiver(this);
        receiver.start();
    }

    private void startSender() throws IOException {
        sender = new Sender(this);
        sender.start();
    }

    private void startDeath() {
        death = new Death(this);
        death.start();
    }

    public void send(String message) {
        sender.send(message);
    }

    public String toString() {
        return socket.toString();
    }
    public void close() throws IOException {
        socket.shutdownInput();
        closeLoopThread(receiver);
        closeLoopThread(sender);
        socket.close();
        // Torrent.removePeer(self);
    }

    public void closeLoopThread(LoopThread l) throws IOException {
        synchronized (l) {
            l.interrupt(); // tell the thing to stop
        }
        try {
            l.join(); // wait for it to actually stop
        } catch (InterruptedException e){} // we want it to exit so InterruptedExceptions are good
    }

    public static void main (String[] args) throws IOException {
        System.out.println("start: " + System.currentTimeMillis());
        Peer p = new Peer(new Socket("localhost", 3000));
    }
}
