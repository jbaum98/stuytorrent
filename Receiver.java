import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.Socket;

public class Receiver implements Runnable {
    private Peer peer;
    private boolean running = true;

    public Receiver(Peer peer) {
        this.peer = peer;
        System.out.println("made a receiver with peer " + peer);
    }

    public void run() {
        try {
            String received;
            while (isRunning()) {
                synchronized (peer.in) {
                    received = peer.in.readLine();
                }
                if (received == null) { stop(); }
                else { System.out.println(received + " from " + peer); }
            }
        } catch (IOException e) {
            System.out.println("Error on reading from " + peer);
            System.out.println(e.getMessage());
        }
    }

    public synchronized boolean isRunning() {
        return running;
    }

    public synchronized void stop() {
        running = false;
    }
}
