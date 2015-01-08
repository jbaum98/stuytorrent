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
    public  PrintWriter     out;
    public  BufferedReader  in;


    public Peer(Socket socket) throws IOException {
        this.socket = socket;
        open();
    }

    public void open() throws IOException {
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        receiver = new Receiver(this);
        receiverThread = new Thread(receiver);
        receiverThread.start();
    }

    public void send(String message) {
        Sender sender = new Sender(this, message);
        (new Thread(sender)).start();
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
