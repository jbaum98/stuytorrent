import java.io.IOException;
import java.util.ArrayList;
import java.net.ServerSocket;
import java.net.Socket;

public class Client extends Runner {
    protected int listeningPort;
    protected ServerSocket listeningSocket;
    public volatile ArrayList<Peer> peers = new ArrayList<Peer>(); // TODO make unique and thread-safe

    public Client(int listeningPort){
        this.listeningPort = listeningPort;

        startListen();
    }

    public void task() {

            Socket peerSocket = null;
            try {
                peerSocket = listeningSocket.accept();
            } catch (IOException e) {
                throw new RuntimeException("Error accepting client connection", e);
            }
            if (peerSocket != null) { addPeer(peerSocket); }
    }

    private void startListen() {
        try {
            listeningSocket = new ServerSocket(listeningPort);
        } catch (IOException e) {
            throw new RuntimeException("Cannot open listeningPort " + listeningPort, e);
        }
    }

    private void addPeer(Socket peerSocket) {
        PeerRunner runner = new PeerRunner(peerSocket, peers);
        (new Thread(runner)).start();
    }

    public synchronized void stop(){
        running = false;
        try {
            listeningSocket.close();
            closeAllPeers();
        } catch (IOException e) {
            throw new RuntimeException("Error closing client", e);
        }
    }

    public synchronized void connect(String hostname, int peerPort) {
        Socket peerSocket = null;
        try {
            peerSocket = new Socket(hostname, peerPort);
        } catch (IOException e) {
            throw new RuntimeException("Cannot open connection to " + hostname + " on port " + peerPort, e);
        }
        addPeer(peerSocket);
    }

    public synchronized void sendToAllPeers(String message) {
        for (Peer peer : peers) {
            peer.send(message);
        }
    }

    public synchronized void closeAllPeers() throws IOException {
        for (Peer peer : peers) {
            peer.close();
        }
    }
}

class PeerRunner implements Runnable {
    Peer peer = null;
    ArrayList<Peer> peers;
    public PeerRunner(Socket socket, ArrayList<Peer> peers) {
        try {
            this.peer = new Peer(socket);
        } catch (IOException e) {
            System.out.println("Error createing peer for " + socket);
            System.out.println(e.getMessage());
        }
        this.peers = peers;
    }
    public void run() {
        synchronized (peers) {
            if (peer != null) {peers.add(peer);}
        }
    }
}

