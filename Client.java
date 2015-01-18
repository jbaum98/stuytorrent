import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Listens for connections and stores current {@link Torrent}s
 */
public class Client extends LoopThread {
    public int listeningPort;
    protected ServerSocket listeningSocket;
    public TorrentList torrents;

    public Client(int listeningPort){
        this.listeningPort = listeningPort;
        this.torrents = new TorrentList();
        startListen();
    }

    /**
     * listens for new connections
     * @see LoopThread
     */
    public void task() throws IOException {
        Socket peerSocket = null;
        try {
            peerSocket = listeningSocket.accept(); // waits here until something tries to connect
        } catch (IOException e) {
            throw new RuntimeException("Error accepting client connection", e);
        }
        if (peerSocket != null) { addPeer(peerSocket); }
    }

    /** Opens the listening port */
    private void startListen() {
        try {
            listeningSocket = new ServerSocket(listeningPort);
        } catch (IOException e) {
            throw new RuntimeException("Cannot open listeningPort " + listeningPort, e);
        }
    }

    /**
     * starts a new {@link ClientConnectionHandler} to handle the connection
     * this allows the {@link Client} to spend more time listening
     * and reduces the likelihood that it is busy when something tries to connect
     */
    private void addPeer(Socket peerSocket) throws IOException {
        ClientConnectionHandler handler = new ClientConnectionHandler(peerSocket, torrents);
        handler.start();
    }

    /**
     * closes the {@link listeningPort}
     * @see LoopThread
     */
    public void cleanup(){
        try {
            listeningSocket.close();
            // TODO close torrents
        } catch (IOException e) {
            throw new RuntimeException("Error closing client", e);
        }
    }

    /**
     * adds a new {@link Torrent}
     * @param filename the name of the metainfo file; TODO must be in same directory as class file
     * @see Torrent
     */
    public void addTorrent(String filename) {
        Torrent torrent = null;
        try {
            torrent = new Torrent(filename, this);
        } catch (IOException e) {
            throw new RuntimeException("Error adding torrent from " + filename, e);
        }
        if (torrent != null) torrents.add(torrent);
    }

    public static void main(String[] args) {
        Client client = new Client(6666);
        client.addTorrent("ubuntu_torrentarino");
        System.out.println("torrent added");
        Torrent t = client.torrents.iterator().next();
        t.start();
    }
}

/**
 * handles new peers so the {@link Client} can get back to listening
 */
class ClientConnectionHandler extends Thread {
    private final Socket socket;
    private final TorrentList torrents;

    /**
     * @param socket   the socket to the connecting Peer
     * @param torrents a reference to the {@link Client#torrents}
     */
    public ClientConnectionHandler(Socket socket, TorrentList torrents) {
        this.socket = socket;
        this.torrents = torrents;
    }

    /**
     * @see Peer#Peer(Socket, TorrentList)
     */
    public void run() {
        try {
            new Peer(socket, torrents);
        } catch (IOException e) {
            throw new RuntimeException("Error connecting to peer at " + socket.getRemoteSocketAddress() + ":" + socket.getPort(), e);
        }
    }

}

