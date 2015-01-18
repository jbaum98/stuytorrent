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
import java.io.BufferedInputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class Peer implements Closeable, AutoCloseable {
    public Socket   socket;
    public BufferedInputStream in;
    public OutputStream out;
    private Torrent torrent;
    public Death  death;
    public String id;

    public Peer(Socket socket, Torrent torrent) throws IOException {
	this(socket, null, torrent);
    }

    public Peer(Socket socket, String id, Torrent torrent) throws IOException {
        this.socket = socket;
	this.id = id;
	this.torrent = torrent;
        in = new BufferedInputStream(socket.getInputStream());
	out = socket.getOutputStream();
        startDeath();
    }

    private void startDeath() {
        death = new Death(this);
        death.start();
    }

    public void send(String message) throws IOException {
        send(message.getBytes(StandardCharsets.ISO_8859_1));
    }

    public void send(byte[] message) throws IOException {
	out.write(message);
    }

    public String toString() {
        return socket.toString();
    }
    public void close() throws IOException {
        socket.close();
        torrent.peers.remove(this);
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
