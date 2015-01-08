import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.Socket;

public class Receiver extends Runner {
    private Peer peer;

    public Receiver(Peer peer) {
        this.peer = peer;
    }

    public void task() {
        try {
            String received;
            synchronized (peer.in) {
                received = peer.in.readLine();
            }
            if (received == null) {
                stop();
            }
            else {
                System.out.println(received + " from " + peer);
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
