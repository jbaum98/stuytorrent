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

public class Torrent {
    private static final Charset charset = StandardCharsets.ISO_8859_1;
    private final SHA1 sha1 = new SHA1();

    private final Client client;
    private final HashSet<Peer> peers;

    public final String peer_id;

    public Message metainfo;
    public Message info;

    public byte[] info_hash;

    private long size;

    private int uploaded = 0;
    private int downloaded = 0;

    public final byte[] handshake;


    public Torrent(String filename, Client client) throws IOException {
        this.client = client;
        this.peers= new HashSet<Peer>();

        this.peer_id = new String(sha1.digest(Double.toString(Math.random())), charset); // TODO better peer_id

        // READ AND PARSE FILE
        Path path = Paths.get(filename);
        byte[] bytes = Files.readAllBytes(path);
        String metainfo_string = new String(bytes, charset);

        metainfo = new Message(metainfo_string);
        info = (Message) (metainfo.get("info"));
        //////////////////////

        // CALCULATE SIZE
        byte[] pieces = ((String)info.get("pieces")).getBytes(charset);
        long piece_length = (long) info.get("piece length");

        size = piece_length * pieces.length / 20;

        // SET INFO HASH
        info_hash = sha1.digest(info.bencode());
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
            handshake[pstrlen+9] = info_hash[i];
            handshake[pstrlen+29] = peer_id_bytes[i];
        }
        //////////////////////
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

    public void start() {
        Message response = updateTracker(trackerRequest("started"));
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

    private Message updateTracker(String url) {
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
        Message response = new Message(response_str);
        return response;
    }

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
}

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
