import java.io.IOException;
import java.util.ArrayList;
import java.net.ServerSocket;
import java.net.Socket;

public class Client implements Runnable {
    protected int listeningPort;
    protected ServerSocket listeningSocket;
    public volatile boolean isStopped = false;
    public volatile ArrayList<Peer> peers = new ArrayList<Peer>(); // TODO make unique and thread-safe

    public Client(int listeningPort){
        this.listeningPort = listeningPort;
    }

    public void run() {
        startListen();

        while (! isStopped) {
            Socket peerSocket = null;
            try {
                peerSocket = listeningSocket.accept();
            } catch (IOException e) {
                throw new RuntimeException("Error accepting client connection", e);
            }
            if (peerSocket != null) { addPeer(peerSocket); }
        }
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
        this.isStopped = true;
        try {
            this.listeningSocket.close();
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
}

class PeerRunner implements Runnable {
    Peer peer;
    ArrayList<Peer> peers;
    public PeerRunner(Socket socket, ArrayList<Peer> peers) {
        this.peer = new Peer(socket);
        this.peers = peers;
    }
    public void run() {
        synchronized (peers) {
            peers.add(peer);
        }
    }
}

