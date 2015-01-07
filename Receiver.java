import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Receiver implements Runnable {
    private Socket socket;
    private String peerId;
    private volatile boolean running = true;

    public Receiver(Socket socket) {
        this.socket = socket;
    }

    public void stop() {
        running = false;
    }

    public void run() {
        while (running) {
            try ( BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream())); ) {
                String received = in.readLine();
                System.out.println(peerId + ": " + received);
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
    }
}
