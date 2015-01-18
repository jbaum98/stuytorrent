/*
 * Listens for connections
 */
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class Client extends LoopThread {
    public int listeningPort;
    protected ServerSocket listeningSocket;
    public TorrentList torrents;

    public Client(int listeningPort){
        this.listeningPort = listeningPort;
	this.torrents = new TorrentList();
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

    public void addTorrent(String filename) {
	Torrent torrent = new Torrent(filename, this);
	torrents.add(torrent);
    }

    /*public static void main(String[] args) {
	Client client = new Client(6666);
	client.addTorrent("ubuntu_torrentarino");
	ClientConnectionHandler c = new ClientConnectionHandler(
	System.out.println(Arrays.toString();
	}*/
}

/*
 * this handles new peers so the Client can get back to listening
 */
class ClientConnectionHandler extends Thread {
    private Socket socket;
    private Peer peer;
    private TorrentList torrents;
    private Torrent torrent;

    public ClientConnectionHandler(Socket socket, TorrentList torrents) throws IOException {
        this.socket = socket;
        this.torrents = torrents; // this is a reference to the Client's torrent list
    }
    public void run() {
	// RECEIVE HANDSHAKE\
	byte[] pstrlen, pstr, reserved, info_hash, peer_id;
	try {
	    pstrlen = new byte[1];
	    peer.in.read(pstrlen);
	    
	    pstr = new byte[pstrlen[0]];
	    peer.in.read(pstr);
	    
	    reserved = new byte[8];
	    peer.in.read(reserved);
	    
	    info_hash = new byte[20];
	    peer.in.read(info_hash);
	    
	    peer_id = new byte[20];
	    peer.in.read(peer_id);
	} catch (IOException e) {
	    info_hash = null;
	}
	// GET TORRENT
	torrent  = torrents.getTorrent(info_hash);
	if (torrent == null) {return;}

	// RECIPROCATE HANDSHAKE
	try {
	    peer = new Peer(socket, torrent);
	    peer.send(handshake());
	} catch (IOException e) {}
	
	// ADD PEER
	synchronized (torrent.peers) {
	    torrent.peers.add(peer);
	} // lock the client's peer list
    }
    
    public byte[] handshake() {
	String pstr = "BitTorrent protocol";
	byte[] pstr_bytes = pstr.getBytes(StandardCharsets.ISO_8859_1);
	byte pstrlen = (byte) pstr_bytes.length;
	byte[] out = new byte[49 + pstrlen];

	out[0] = pstrlen;


	for (int i = 0; i < pstrlen; i++) {
	    out[i+1] = pstr_bytes[i];
	}

	for(int i = 0; i<20; i++) {
	    out[pstrlen+9]=torrent.info_hash_bytes[i];
	    out[pstrlen+29]=torrent.peer_id_bytes[i];
	}
	return out;
    }
}

