import java.util.Arrays;
import java.util.HashSet;
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
    private Client client;
    public HashSet<Peer> peers;

    private SHA1 sha1 = new SHA1();

    public String peer_id = sha1.digest(Double.toString(Math.random()), true);
    public byte[] peer_id_bytes = peer_id.getBytes(charset);

    public String info_hash;
    public byte[] info_hash_bytes;

    public Message metainfo;
    public Message info;
    private long size;

    private int uploaded = 0;
    private int downloaded = 0;

    public byte[] handshake;

    private static final Charset charset = StandardCharsets.ISO_8859_1;
 
    public Torrent(String filename, Client client) {
	this.client = client;
	
	// read file
        Path p = Paths.get(filename);
        byte[] b = null;
        try {
            b = Files.readAllBytes(p);
        } catch (IOException e) {
            e.printStackTrace();
        }
	
        String s = new String(b, charset);
        metainfo = new Message(s);
	info = (Message) (metainfo.get("info"));
	
	byte[] pieces = ((String)info.get("pieces")).getBytes();
	long piece_length = (long) info.get("piece length");

	size = piece_length * pieces.length / 20;

	info_hash = sha1.digest(info.bencode(),true);
	info_hash_bytes = info_hash.getBytes(charset);
	
	setHandshake();
    }

    public boolean equals(Torrent other){
	return this.info_hash.equals(other.info_hash);
    }

    public void start() {
	Message response = updateTracker(trackerRequest("started"));
	System.out.println("got tracker response: " + response);
	byte[] peer_bytes = ((String) response.get("peers")).getBytes(charset);
	PeerToBe[] peers = parsePeers(peer_bytes);
	System.out.println("bout to pop " + peers.length + " peers");
	for (PeerToBe peer : peers) {
	    try {
		connect(peer);
	    } catch (IOException e) {
		System.out.println("oh no on connect");
	    }
	}
    }

    private Message updateTracker(String url) {
	byte[] tracker_response = null;
	HttpURLConnection connection = null;
	try {
	    URL tracker_url = new URL(url);
	    connection = (HttpURLConnection)tracker_url.openConnection();
	} catch (IOException e) {}
	try (
	     InputStream response = connection.getInputStream();
	     BufferedInputStream reader = new BufferedInputStream(response);
	     ByteArrayOutputStream temp_output = new ByteArrayOutputStream();
	     ) {
		byte[] buf = new byte[1];
			
		while(reader.read(buf) != -1)
		    temp_output.write(buf);
		
		tracker_response = temp_output.toByteArray();
	    } catch (IOException e) {}
	String response_str = new String(tracker_response, charset);
	Message response = new Message(response_str);
	return response;
    }

    private PeerToBe[] parsePeers(byte[] peers) {
	PeerToBe[] out = new PeerToBe[peers.length/6];
	for (int i = 0; i < peers.length; i+=6) {
	    String hostname = byteToInt(peers[i])+"."+byteToInt(peers[i+1])+"."+byteToInt(peers[i+2])+"."+byteToInt(peers[i+3]);
	    int port = byteToInt(peers[i+4])*256+byteToInt(peers[i+5]);
	    out[i/6] = new PeerToBe(hostname, port);
	}
	return out;
    }

    private int byteToInt(byte b) {
	int out = (int) b;
	if (out < 0) { out += 256; }
	return out;
    }

    public String trackerRequest(String status) {
	return trackerRequest() + "&"+"event="+status;
    }
    
    public String trackerRequest(){
        String url = new String(((String)metainfo.get("announce"))+"?");
	int port = client.listeningPort;
	int compact = 1;
        url+="info_hash="+info_hash+"&"+"peer_id="+peer_id+"&"+"port="+port+"&"+"uploaded="+uploaded+"&"+"downloaded="+downloaded+"&"+"left="+left()+"&"+"compact="+compact;
        return url;
    }

    private long left() {
        return size;
    }

    private void connect(PeerToBe url) throws IOException {
	System.out.println("trying to connect to " + url.hostname + ":" + url.port);
	Socket socket = new Socket(url.hostname, url.port);
	System.out.println("just made me a socket");
	Peer peer = new Peer(socket, this);
	System.out.println("just made me a peer");
	peer.send(handshake);
	System.out.println("just shook me a hand");
	
	byte[] pstrlen, pstr, reserved, info_hash, peer_id_bytes;
	try {
	    pstrlen = new byte[1];
	    peer.in.read(pstrlen);
	    
	    pstr = new byte[pstrlen[0]];
	    peer.in.read(pstr);
	    
	    reserved = new byte[8];
	    peer.in.read(reserved);
	    
	    info_hash = new byte[20];
	    peer.in.read(info_hash);
	    
	    peer_id_bytes = new byte[20];
	    peer.in.read(peer_id_bytes);
	} catch (IOException e) {
	    info_hash = null;
	    peer_id_bytes = null;
	}
	if (info_hash == null || peer_id_bytes == null) {return;}
	System.out.println("got shook back if ya know what im sayin ;)");

	peer.id = new String(peer_id_bytes, charset);
	System.out.println("oh yeah we connected some shit to " + url.hostname);
    }

    public void setHandshake() {
	String pstr = "BitTorrent protocol";
	byte[] pstr_bytes = pstr.getBytes(charset);
	byte pstrlen = (byte) pstr_bytes.length;
	handshake = new byte[49 + pstrlen];

	handshake[0] = pstrlen;


	for (int i = 0; i < pstrlen; i++) {
	    handshake[i+1] = pstr_bytes[i];
	}

	for(int i = 0; i<20; i++) {
	    handshake[pstrlen+9] = info_hash_bytes[i];
	    handshake[pstrlen+29] = peer_id_bytes[i];
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
}
