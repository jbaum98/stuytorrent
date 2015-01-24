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
import java.nio.charset.StandardCharsets;
import java.nio.charset.Charset;
import java.net.URL;
import java.net.MalformedURLException;
import java.net.HttpURLConnection;
import java.net.Socket;

/**
 * represents a torrent we want to download
 */

public class Torrent {
    private static final Charset charset = StandardCharsets.ISO_8859_1;
    private final SHA1 sha1 = new SHA1();

    private final Client client;
    private final HashSet<Peer> peers;

    public final String peer_id;

    public BencodingMap metainfo;
    public BencodingMap info;
    public byte[]  piece_hashes;
    public int    piece_size;
    public int     num_pieces;
    public Piece[] pieces;
    public long    size;

    public byte[] info_hash;


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

        this.peer_id = new String(sha1.digest(Double.toString(Math.random())), charset); // TODO better peer_id

        parseMeta(Paths.get(filename));

        // CALCULATE SIZE
        calculateSize();
        fillPieces();

        // SET INFO HASH
        info_hash = sha1.digest(info.bencode());
        System.out.println(Arrays.toString(info_hash));
        //////////////////////

        // SET HANDSHAKE
        String pstr = "BitTorrent protocol";
        byte[] pstr_bytes = pstr.getBytes(charset);
        byte pstrlen = (byte) pstr_bytes.length;
        handshake = new byte[49 + pstrlen];

        handshake[0] = pstrlen;

        for (int i = 0; i < pstrlen; i++) {
            handshake[i+1] = pstr_bytes[i];
        }

        byte[] peer_id_bytes = peer_id.getBytes(charset);
        for(int i = 0; i<20; i++) {
            handshake[pstrlen+9+i] = info_hash[i];
            handshake[pstrlen+29+i] = peer_id_bytes[i];
        }
        //////////////////////
    }

    private void parseMeta(Path metainfo_file) throws IOException {
        byte[] bytes = Files.readAllBytes(metainfo_file);
        String metainfo_string = new String(bytes, charset);

        metainfo = new BencodingMap(metainfo_string);
        info = (BencodingMap) (metainfo.get("info"));
    }

    private void calculateSize() {
        piece_hashes = ((String)info.get("pieces")).getBytes(charset);
        Long piece_size_long = (Long) info.get("piece length");
        System.out.println(piece_size_long);
        piece_size = (int) Math.max(Math.min(Integer.MAX_VALUE, piece_size_long), Integer.MIN_VALUE);
        num_pieces = piece_hashes.length / 20;
        size = piece_size * num_pieces;
    }

    private void fillPieces() {
        pieces = new Piece[num_pieces];
        for (int i = 0; i < num_pieces; i++) {
            byte[] hash = Arrays.copyOfRange(piece_hashes, i*20, (i+1)*20);
            pieces[i] = new Piece(hash, piece_size);
        }
    }

    @Override
    public boolean equals(Object other){
        if (this == other) return true;
        if ( !(other instanceof Torrent)) return false;
        Torrent otherTorrent = (Torrent) other;
        return this.info_hash.equals(otherTorrent.info_hash);
    }

    @Override
    public int hashCode() {
        return info_hash.hashCode();
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

        byte[] peer_bytes = ((String) response.get("peers")).getBytes(charset);
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

    /**
     * connects to the tracker
     * @return BencodingMap containing parsed response
     */
    private BencodingMap updateTracker(String url) {
        byte[] tracker_response;
        HttpURLConnection connection;
        try {
            URL tracker_url = new URL(url);
            connection = (HttpURLConnection)tracker_url.openConnection();
        } catch (IOException e) {
            throw new RuntimeException("Couldn't connect to tracker", e);
        }
        try (
                InputStream response = connection.getInputStream();
                BufferedInputStream reader = new BufferedInputStream(response);
                ByteArrayOutputStream temp_output = new ByteArrayOutputStream();
            ) {
            byte[] buf = new byte[1];

            while(reader.read(buf) != -1)
                temp_output.write(buf);

            tracker_response = temp_output.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Error on updating tracker", e);
        }
        String response_str = new String(tracker_response, charset);
        BencodingMap response = new BencodingMap(response_str);
        return response;
    }

    /** extracts peer information from peers string in tracker response */
    private PeerToBe[] parsePeers(byte[] peers) {
        PeerToBe[] out = new PeerToBe[peers.length/6];
        int[] unsigneds = new int[peers.length];
        for (int i = 0; i < peers.length; i++) {
            unsigneds[i] = byteToInt(peers[i]);
        }
        for (int i = 0; i < unsigneds.length; i+=6) {
            String hostname = unsigneds[i]+"."+unsigneds[i+1]+"."+unsigneds[i+2]+"."+unsigneds[i+3]; // construct IP address
            int port = unsigneds[i+4]*256+unsigneds[i+5]; // port is stored as 2 bytes, need to be concatenated
            out[i/6] = new PeerToBe(hostname, port);
        }
        return out;
    }

    /** converts a signed byte to an an integer in range [0, 256] */
    private int byteToInt(byte b) {
        int out = (int) b;
        if (out < 0) { out += 256; }
        return out;
    }

    public String trackerRequest(){
        return trackerRequest(null);
    }

    /** @return the url with proper query string for the tracker request */
    public String trackerRequest(String event) {
        String url = ((String) metainfo.get("announce"));
        QueryMap queries = new QueryMap();

        queries.put("info_hash", info_hash);
        queries.put("port", client.listeningPort);
        queries.put("peer_id", peer_id);
        queries.put("uploaded", uploaded);
        queries.put("downloaded", downloaded);
        queries.put("left", left());
        queries.put("compact", 1);
        if (event != null) {
            queries.put("event", event);
        }
        String out = createURL(url, queries);
        System.out.println(out);
        return out;
    }

    /** adds a query string to a url */
    private String createURL(String url, QueryMap queries_hash) {
        String query_string = new String();
        Set<Map.Entry<String,String>> queries = queries_hash.entrySet();
        Iterator<Map.Entry<String,String>> i = queries.iterator();
        while(i.hasNext()) {
            Map.Entry<String, String> query = i.next();
            query_string += query.getKey() + "=" + query.getValue();
            if (i.hasNext()) { // not the last one
                query_string += "&";
            }
        }
        String out = url + "?" + query_string;
        return out;
    }


    private long left() {
        return size;
    }

    private void connect(PeerToBe url) throws IOException {
        System.out.println("creating socket at "+url);
        Socket socket = new Socket(url.hostname, url.port);
        System.out.println("starting peer");
        Peer peer = new Peer(socket, this);
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
        synchronized (pieces) {
            return pieces[index].getBytes(begin, length);
        }
    }

    public void addChunk(int index, int begin, byte[] block) {
        synchronized (pieces) {
            pieces[index].setData(begin, block);
        }
    }
}

/** stores information needed to connect to a new Peer */
class PeerToBe {
    public String hostname;
    public int port;

    public PeerToBe(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;
    }

    public String toString() {
        return hostname+":"+port;
    }
}
