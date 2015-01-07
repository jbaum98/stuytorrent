import java.io.IOException;
import java.util.ArrayList;
import java.net.ServerSocket;
import java.net.Socket;

public class Client implements Runnable {
    protected int listeningPort;
    protected ServerSocket listeningSocket;
    protected boolean isStopped = false;
    protected ArrayList<PeerRunner> peers = new ArrayList<PeerRunner>();

    public Client(int listeningPort){
        this.listeningPort = listeningPort;
    }

    public void run() {
        startListen();

        while (! isStopped()) {
            Socket peerSocket = null;
            try {
                peerSocket = listeningSocket.accept();
            } catch (IOException e) {
                throw new RuntimeException("Error accepting client connection", e);
            }
            startPeerRunner(peerSocket);
        }
    }

    private void startListen() {
        try {
            listeningSocket = new ServerSocket(listeningPort);
        } catch (IOException e) {
            throw new RuntimeException("Cannot open listeningPort " + listeningPort, e);
        }
    }

    private void startPeerRunner(Socket peerSocket) {
        PeerRunner runner = new PeerRunner(peerSocket);
        addPeer(runner);
        new Thread(runner).start();
    }

    private synchronized boolean isStopped() {
        return isStopped;
    }

    private synchronized boolean addPeer(PeerRunner peer) {
        return peers.add(peer);
    }

    public synchronized void stop(){
        this.isStopped = true;
        try {
            this.listeningSocket.close();
        } catch (IOException e) {
            throw new RuntimeException("Error closing client", e);
        }
    }

    public void connect(String hostname, int peerPort) {
        Socket peerSocket = null;
        try {
            peerSocket = new Socket(hostname, peerPort);
        } catch (IOException e) {
            throw new RuntimeException("Cannot open connection to " + hostname + " on port " + peerPort, e);
        }
        startPeerRunner(peerSocket);
    }
}

