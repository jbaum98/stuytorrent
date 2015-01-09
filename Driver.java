import java.util.*;
import java.io.*;

public class Driver {
    private static Client client;
    private static String prompt = ">";

    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            throw new IllegalArgumentException("Usage: java Driver <port>");
        }
        int port = Integer.parseInt(args[0]);
        client = new Client(port);
        client.start();
        String input;

        System.out.print(prompt);
        try (BufferedReader in = new BufferedReader( new InputStreamReader(System.in) );) {
            while ((input = in.readLine()) != null) {
                String firstWord = firstWord(input);
                switch (firstWord) {
                    case "connect":
                        connect(input.substring(7));
                        break;

                    case "peers":
                        printPeers();
                        break;

                    default:
                        client.sendToAllPeers(input);
                }
                System.out.print(prompt);
            }
        } catch (Exception e) {throw e;}
        client.interrupt();
    }

    private static String firstWord(String input) {
        int firstSpace = input.indexOf(' ');
        firstSpace = firstSpace == -1 ? input.length() : firstSpace;
        return input.substring(0,firstSpace);
    }

    private static void connect(String portsString) throws IOException {
        for (String port : portsString.split(" ")) {
            if (port.length()> 0) {client.connect("localhost", Integer.parseInt(port));}
        }
    }

    private static void printPeers() {
        for (Peer peer : client.peers) {
            System.out.println(peer);
        }
    }
}
