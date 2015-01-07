import java.io.PrintWriter;
import java.io.IOException;
import java.net.Socket;

public class Sender implements Runnable {
    private Socket socket;
    private String message;

    public Sender(Socket socket, String message) {
        this.message = message;
        this.socket = socket;
    }

    public void run() {
        try (PrintWriter out = new PrintWriter(socket.getOutputStream(), true);) {
            out.println(message);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
