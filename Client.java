/*
 * Listens for connections
 */
import java.io.IOException;
import java.util.ArrayList;
import java.net.ServerSocket;
import java.net.Socket;

public class Client extends LoopThread {
    public int listeningPort;
    protected ServerSocket listeningSocket;
    public Torrent torrent;

    public Client(int listeningPort){
        this.listeningPort = listeningPort;
        startListen();
    }

    public void task() throws IOException { // see LoopThread.java
        Socket peerSocket = null;
        try {
            peerSocket = listeningSocket.accept(); // waits here until something tries to connect
        } catch (IOException e) {
            throw new RuntimeException("Error accepting client connection", e);
        }
        if (peerSocket != null) { addPeer(peerSocket); }
    }

    /* Opens the listening port */
    private void startListen() {
        try {
            listeningSocket = new ServerSocket(listeningPort);
        } catch (IOException e) {
            throw new RuntimeException("Cannot open listeningPort " + listeningPort, e);
        }
    }

    /*
     * starts a new thread to handle the connection
     * this allows the Client process to spend more time listening
     * and reduces the likelihood that it is busy when something tries to connect
     */
    private void addPeer(Socket peerSocket) throws IOException {
        //(new PeerRunner(peerSocket, peers)).start(); // see PeerRunner at bottom of this file
    }

    public void cleanup(){ // see LoopThread
        try {
            listeningSocket.close();
            //closeAllPeers();
        } catch (IOException e) {
            throw new RuntimeException("Error closing client", e);
        }
    }

    /*
     * connects to another client
     */
    public void connect(String hostname, int peerPort) throws IOException {
        Socket peerSocket = null;
        try {
            peerSocket = new Socket(hostname, peerPort);
        } catch (IOException e) {
            throw new RuntimeException("Cannot open connection to " + hostname + " on port " + peerPort, e);
        }
        addPeer(peerSocket);
    }

    /*public synchronized void sendToAllPeers(String message) {
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
	}*/

    public void addTorrent(String filename) {
	torrent = new Torrent(filename, this);
    }

    public static void main(String[] args) {
	Client client = new Client(6666);
	client.addTorrent("ubuntu_torrentarino");
	System.out.println(client.torrent.start());
    }
}

/*
 * this handles new peers so the Client can get back to listening
 */
/*class PeerRunner extends Thread {
    Peer peer = null;
    ArrayList<Peer> peers;

    public PeerRunner(Socket socket, ArrayList<Peer> peers) throws IOException {
        this.peer = new Peer(socket);
        //this.peers = peers; // this is a reference to the Client's peers list
    }
    public void run() {
        synchronized (peers) { // lock the client's peer list
            if (peer != null) {peers.add(peer);}
	    }
	return;
    }
    }*/

