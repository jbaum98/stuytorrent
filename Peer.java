import java.net.Socket;
import java.io.IOException;
import java.io.Closeable;

// TODO .equals and .compareTo
public class Peer implements Closeable, AutoCloseable {
    public  Socket   socket;
    private Receiver receiver;
    private Sender   sender;
    public boolean notified = false;


    public Peer(Socket socket) throws IOException {
        this.socket = socket;
        open();
    }

    public void open() throws IOException {
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
            l.interrupt();
        }
        try {
            l.join();
        } catch (InterruptedException e){} // we want it to exit
    }

    public void setNotified() {
        notified = true;
    }
}
