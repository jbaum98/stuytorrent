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

public class Peer implements Closeable, AutoCloseable {
    public  Socket   socket;
    private Receiver receiver;
    private Sender   sender;

    public Peer(Socket socket) throws IOException {
        this.socket = socket;
        startReceiver();
        startSender();
    }

    private void startReceiver() throws IOException {
        receiver = new Receiver(this);
        receiver.start();
    }

    private void startSender() throws IOException {
        sender = new Sender(this);
        sender.start();
    }

    public void send(String message) {
        sender.send(message);
    }

    public String toString() {
        return socket.toString();
    }

    public void close() throws IOException {
        closeLoopThread(receiver);
        closeLoopThread(receiver);
        socket.close();
    }

    public void closeLoopThread(LoopThread l) throws IOException {
        synchronized (l) {
            l.interrupt(); // tell the thing to stop
        }
        try {
            l.join(); // wait for it to actually stop
        } catch (InterruptedException e){} // we want it to exit so InterruptedExceptions are good
    }
}
