import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.io.IOException;
import java.net.URLEncoder;

public class Torrent {
    private Message metainfo;
    private Message info;
    private long size;
    private String bencodedInfo;
    public String peer_id = sha1.digest(Double.toString(Math.random()).getBytes(),true);
    private static final SHA1 sha1 = new SHA1();
    private Client client;
    private int uploaded = 0;
    private int downloaded = 0;

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
	String pieces = (String) info.get("pieces");
	long piece_length = (long) info.get("piece length");
	size = piece_length * ( (long) pieces.length() / 20);
    }

    public String start() {
	return trackerRequest("started");
    }

    public String trackerRequest(String status) {
	return trackerRequest() + "&"+"status="+status;
    }


    public String trackerRequest(){
        String url = new String(((String)metainfo.get("announce"))+"?");
        
        String info_hash = sha1.digest(info.bencode(),true);
	int port = client.listeningPort;
	int compact = 0;
        url+="info_hash="+info_hash+"&"+"peer_id="+peer_id+"&"+"port="+port+"&"+"uploaded="+uploaded+"downloaded="+downloaded+"left="+left()+"&"+"compact="+compact;
        return url;
    }

    private long left() {
        return size;
    }

    public Message getMetainfo(){
        return metainfo;
    }
}
