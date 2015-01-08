import java.net.Socket;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Closeable;

// TODO .equals and .compareTo
public class Peer implements Closeable {
    public  Socket          socket;
    private Receiver        receiver;
    private Thread          receiverThread;
    private Sender          sender;
    private Thread          senderThread;
    public  PrintWriter     out;
    public  BufferedReader  in;


    public Peer(Socket socket) throws IOException {
        this.socket = socket;
        open();
    }

    public void open() throws IOException {
        startReceiver();
        startSender();
    }

    private void startReceiver() throws IOException {
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        receiver = new Receiver(this);
        receiverThread = new Thread(receiver);
        receiverThread.start();
    }

    private void startSender() throws IOException {
        out = new PrintWriter(socket.getOutputStream(), true);
        sender = new Sender(this);
        senderThread = new Thread(sender);
        senderThread.start();
    }

    public void send(String message) {
        sender.send(message);
    }

    public String toString() {
        return socket.toString();
    }

    public void close() throws IOException {
        receiver.stop();
        in.close();
        out.close();
        socket.close();
    }
}
