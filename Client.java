import java.io.IOException;
import java.util.ArrayList;
import java.net.ServerSocket;
import java.net.Socket;

public class Client extends LoopThread {
    protected int listeningPort;
    protected ServerSocket listeningSocket;
    private ArrayList<Peer> peers = new ArrayList<Peer>(); // TODO make unique and thread-safe

    public Client(int listeningPort){
        this.listeningPort = listeningPort;

        startListen();
    }

    public void task() throws IOException {
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

    private void addPeer(Socket peerSocket) throws IOException {
        (new PeerRunner(peerSocket, peers)).start();
    }

    public void cleanup(){
        try {
            listeningSocket.close();
            closeAllPeers();
        } catch (IOException e) {
            throw new RuntimeException("Error closing client", e);
        }
    }

    public void connect(String hostname, int peerPort) throws IOException {
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

    public Peer[] getPeers() {
        return peers.toArray(new Peer[0]);
    }
}

class PeerRunner extends Thread {
    Peer peer = null;
    ArrayList<Peer> peers;

    public PeerRunner(Socket socket, ArrayList<Peer> peers) throws IOException {
        this.peer = new Peer(socket);
        this.peers = peers;
    }
    public void run() {
        synchronized (peers) {
            if (peer != null) {peers.add(peer);}
        }
    }
}

