import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

public class Driver {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java Driver <path to .torrent file>");
            System.exit(0);
        }
        String filename = args[0];
        Client client = new Client(6666);
        client.start();
        client.addTorrent(filename);
        BufferedReader in = new BufferedReader( new InputStreamReader(System.in));
        try {
            String line = in.readLine();
            while (line != null) {
                client.torrents.iterator().next().done(null);
                line = in.readLine();
            }
        } catch (IOException e) {
            System.out.println("Error with reading user input");
        }
    }
}
