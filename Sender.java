/*
 * sends messages to another Client
 */

import java.util.concurrent.LinkedBlockingQueue;
import java.io.PrintWriter;
import java.io.OutputStream;
import java.io.IOException;
import java.net.Socket;

public class Sender extends LoopThread {
    private Peer peer;
    private PrintWriter out;
    public LinkedBlockingQueue<String> messages = new LinkedBlockingQueue<String>(); // works as a buffer, you add to front and take from back

    public Sender(Peer peer) throws IOException {
        this.peer = peer;
        out = new PrintWriter(peer.socket.getOutputStream(), true); // create somewhere to write to
    }

    protected void task() throws IOException, InterruptedException { // see LoopThread.java
        out.println(messages.take());
        /*
         * print to the Socket writer the last guy in the outgoing buffer
         * waits for something to get put in the outgoing buffer if it's empty
         */
    }


    public boolean send(String message) {
        return messages.offer(message); // add to the outgoing buffer
    }

    protected void cleanup() throws IOException { // see LoopThread.java
        out.close();
    }
}
