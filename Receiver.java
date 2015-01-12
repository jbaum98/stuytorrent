/*
 * receives messages from a Peer
 * TODO: rework with a buffer of messages,
 * because we'll eventually want to process them seperately instead of just printing them
 */

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.IOException;
import java.net.Socket;

public class Receiver extends LoopThread {
    private Peer peer;
    public BufferedReader in;

    public Receiver(Peer peer) throws IOException {
        this.peer = peer;
        in = new BufferedReader(new InputStreamReader(peer.socket.getInputStream())); // open a Reader to read from the Socket
    }

    protected void task() throws IOException { // see LoopThread.java
        String received;
        synchronized (in) {
            received = in.readLine(); // waits here until the other guy writes to his Socket
        }
        if (received == null) { // when the Socket is closed, it reads nulls
            interrupt();
        } else {
            System.out.println(received + " from " + peer); // TODO: write to message buffer
        }
    }

    public void cleanup() throws IOException { // see LoopThread.java
        in.close();
    }
}
