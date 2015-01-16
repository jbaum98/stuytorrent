import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.io.IOException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;

public class Torrent{
    private Message metainfo;
    private String bencodedInfo;

    public Torrent(String filename) {
        Path p = Paths.get(filename);
        byte[] b = new byte[0];
        try {
            b = Files.readAllBytes(p);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String s = new String(b, StandardCharsets.US_ASCII);
        metainfo = new Message(s);
    }
    
    private static String byteArray2Hex(byte[] hash) {                                                               
	Formatter formatter = new Formatter();                                                                       
	for (byte b : hash) {                                                                                        
	    formatter.format("%02x", b);                                                                             
	}                                                                                                            
	return formatter.toString();                                                                                 
    }
    
    public String trackerRequest(){
	String url = new String(((String)metainfo.get("announce"))+"?");
	MessageDigest cript = MessageDigest.getInstance("SHA-1");
	cript.reset();
	cript.update((((Message)(metainfo.get("info"))).bencode()).getBytes());
	byte[] output = cript.digest();
	String info_hash = new String(Hex.encodeHex(cript.digest()),CharSet.forName("UTF-8"));
	url+="info_hash="+info_hash;
	//url+="info_hash="+URLEncoder.encode(, "UTF-8");
	return url;
    }

    public Message getMetainfo(){
        return metainfo;
    }

    public static void main(String[] args){
        Torrent t = new Torrent("[kickass.so]the.interview.2014.1080p.5.1.dd.custom.nl.subs.unlimitedmovies.torrent");
        System.out.println(t.getMetainfo());
    }
}
