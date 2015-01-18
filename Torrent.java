import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.io.IOException;
import java.net.URL;
import java.net.MalformedURLException;
import java.net.HttpURLConnection;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ByteArrayOutputStream;
import java.io.BufferedInputStream;
import java.util.Arrays;
import java.util.HashSet;

public class Torrent {
    private Message metainfo;
    private Message info;
    private long size;
    private String bencodedInfo;
    public String peer_id = sha1.digest(Double.toString(Math.random()), true);
    public byte[] peer_id_bytes = peer_id.getBytes(StandardCharsets.ISO_8859_1);
    private static final SHA1 sha1 = new SHA1();
    private Client client;
    private int uploaded = 0;
    private int downloaded = 0;
    public String info_hash;
    public byte[] info_hash_bytes;
    public HashSet<Peer> peers;

    public Torrent(String filename, Client client) {
	this.client = client;
        Path p = Paths.get(filename);
        byte[] b = new byte[0];
        try {
            b = Files.readAllBytes(p);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String s = new String(b, StandardCharsets.ISO_8859_1);
        metainfo = new Message(s);
	info = (Message) (metainfo.get("info"));
	byte[] pieces = ((String)info.get("pieces")).getBytes(StandardCharsets.ISO_8859_1);
	long piece_length = (long) info.get("piece length");
	System.out.println(pieces.length);
	size = piece_length * pieces.length  / 20;
	info_hash = sha1.digest(info.bencode(),true);
	info_hash_bytes = info_hash.getBytes(StandardCharsets.ISO_8859_1);
    }

    public boolean equals(Torrent other){
	return this.info_hash.equals(other.info_hash);
    }

    public Message start() {
	String url = trackerRequest("started");
	System.out.println("url is " + url);
	byte[] tracker_response = new byte[0];
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
	    } 
	catch (IOException e) {}
	String response_str = new String(tracker_response, StandardCharsets.ISO_8859_1);
	System.out.println(response_str);
	Message response = new Message(response_str);
	System.out.println(Arrays.toString(parsePeers(((String)response.get("peers")).getBytes(StandardCharsets.ISO_8859_1))));
	return response;
    }

    public String trackerRequest(String status) {
	return trackerRequest() + "&"+"event="+status;
    }

    private String[] parsePeers(byte[] peers) {
	String[] out = new String[peers.length/6];
	for (int i = 0; i < peers.length; i+=6) {
	    out[i/6] = ""+byteToInt(peers[i])+"."+byteToInt(peers[i+1])+"."+byteToInt(peers[i+2])+"."+byteToInt(peers[i+3])+":"+byteToInt(peers[i+4])+byteToInt(peers[i+5]);
	}
	return out;
    }

    private int byteToInt(byte b) {
	int out = (int) b;
	if (out < 0) { out += 256; }
	return out;
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

    public Message getMetainfo(){
        return metainfo;
    }
}
