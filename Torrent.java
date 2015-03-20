package stuytorrent;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ByteArrayOutputStream;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.net.URL;
import java.net.MalformedURLException;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.io.FileOutputStream;

/**
 * represents a torrent we want to download
 */

public class Torrent {
    private final SHA1 sha1 = new SHA1();

    private final Client client;
    public final HashSet<Peer> peers;

    public final String peer_id;

    public MetaInfo metainfo;
    public Info info;
    public PieceList pieces;

    private int uploaded = 0;
    private int downloaded = 0;

    public byte[] handshake;

    /**
     * reads and parses the metainfo file
     * @param filename name of metainfo file; TODO must be in same directory
     * @param client   reference to client
     */
    public Torrent(String filename, Client client) throws IOException {
        this.client = client;
        this.peers= new HashSet<Peer>();

        this.peer_id = new String(sha1.digest(Double.toString(Math.random())), Globals.CHARSET); // TODO better peer_id

        metainfo = new MetaInfoFile(Paths.get(filename));
        info = metainfo.info();

        pieces = new PieceList(info);

        setHandshake();
        start();
    }

    private void setHandshake() {
        String pstr = "BitTorrent protocol";
        byte[] pstr_bytes = pstr.getBytes(Globals.CHARSET);
        byte pstrlen = (byte) pstr_bytes.length;
        handshake = new byte[49 + pstrlen];

        handshake[0] = pstrlen;

        for (int i = 0; i < pstrlen; i++) {
            handshake[i+1] = pstr_bytes[i];
        }

        byte[] peer_id_bytes = peer_id.getBytes(Globals.CHARSET);
        for(int i = 0; i<20; i++) {
            handshake[pstrlen+9+i] = metainfo.infoHash()[i];
            handshake[pstrlen+29+i] = peer_id_bytes[i];
        }
    }

    @Override
    public boolean equals(Object other){
        if (this == other) return true;
        if ( !(other instanceof Torrent)) return false;
        Torrent otherTorrent = (Torrent) other;
        return Arrays.equals(this.metainfo.infoHash(), otherTorrent.metainfo.infoHash());
    }

    @Override
    public int hashCode() {
        return metainfo.infoHash().hashCode();
    }

    /**
     * starts the Torrent by downloading peers from the Tracker
     * and connecting to those Peers
     */
    public void start() {
        BencodingMap response = updateTracker(trackerRequest("started"));
        System.out.println("got tracker response");

        if (response.containsKey("failure reason")) {
            throw new RuntimeException("Tracker request failed with "+(String)response.get("failure reason"));
        }

        byte[] peer_bytes = ((String) response.get("peers")).getBytes(Globals.CHARSET);
        System.out.println("parsing some peers");
        PeerToBe[] peers = parsePeers(peer_bytes);
        System.out.println("bout to pop " + peers.length + " peers");

        for (PeerToBe peer : peers) {
            try {
                connect(peer);
            } catch (IOException e) {
                throw new RuntimeException("Error on connecting to peer at " + peer, e);
            }
        }
    }


    private long left() {
        return size;
    }

    private void connect(PeerToBe url) throws IOException {
        url.start();
    }


    public boolean addPeer(Peer peer) {
        synchronized (peers) {
            return peers.add(peer);
        }
    }

    public boolean removePeer(Peer peer) {
        synchronized (peers) {
            return peers.remove(peer);
        }
    }

    public byte[] getChunk(int index, int begin, int length) {
        return pieces[index].getBytes(begin, length);
    }

    public void addChunk(int index, int begin, byte[] block) {
        Piece piece = pieces[index];
        Request outstanding = piece.getOutstandingRequest();
        if (outstanding != null && outstanding.begin == begin && outstanding.length == block.length) {
            piece.setData(block);
        } else {
            // System.out.println("rejected index " + index + " begin " + begin + " length " + block.length + " for " + outstanding );
        }
    }

    public String status() {
        String out = new String();
        for (int i = 0; i < pieces.length; i++) {
            Piece p = pieces[i];
            out += p.done() ? 1 : 0;
        }
        return out;
    }

    public void checkDone() {
        for (Piece piece : pieces) {
            if (!piece.done()) return;
        }
        finish();
    }

    private synchronized void finish() {
        try ( FileOutputStream file = new FileOutputStream("out")) {
            for (Piece p : pieces) {
                file.write(p.getBytes());
            }
        } catch (IOException e) {
            System.out.println("Eror on writing");
        }
        synchronized(peers) {
            for (Peer peer : peers) {
                peer.close();
            }
        }
    }

    public static void main(String[] args) {
        byte[] b1 = {1, 2, 3};
        byte[] b2 = {3,4, 5};
        byte[][] bs = {b1, b2};
        try ( FileOutputStream file = new FileOutputStream("out")) {
                for (byte[] b : bs) {
                        file.write(b);
                    }
                } catch (IOException e) {
                System.out.println("Eror on writing");
            }
    }
}

/** stores information needed to connect to a new Peer */
class PeerToBe extends Thread {
    private String hostname;
    private int port;
    private Torrent torrent; 

    public PeerToBe(String hostname, int port, Torrent torrent) {
        this.hostname = hostname;
        this.port = port;
        this.torrent = torrent;
    }

    public void run() {
        System.out.println("creating socket at "+this);
        try {
            Socket socket = new Socket(hostname, port);
            System.out.println("starting peer");
            Peer peer = new Peer(socket, torrent);
        } catch (IOException e) {
            e.printStackTrace(System.out);
        }
    }

    public String toString() {
        return hostname+":"+port;
    }
}
