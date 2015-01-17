import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.Arrays;

public class Torrent{
    private Message metainfo;
    private String bencodedInfo;
    private static final SHA1 sha1 = new SHA1();

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


    public String trackerRequest(){
        String url = new String(((String)metainfo.get("announce"))+"?");
        Message info = (Message) (metainfo.get("info"));
        String info_hash = sha1.digest(info.bencode());
        url+="info_hash="+info_hash;
        return url;
    }

    public Message getMetainfo(){
        return metainfo;
    }

    public static void main(String[] args){
	Path k = Paths.get("ubuntu_info");
       	/*Path p = Paths.get("ubuntu.torrent");
	  byte[] b = new byte[0];*/

	byte[] w = new byte[0];
        try {
            //b = Files.readAllBytes(p);
	    w = Files.readAllBytes(k);
        } catch (IOException e) {
            e.printStackTrace();
        }
	//String s = new String(b, StandardCharsets.ISO_8859_1);
	//	Message m = new Message(s);
	//Message info = (Message) m.get("info");
	//String info_s = info.bencode();
	//System.out.println(s);*/
	//System.out.println(info_bytes[157]);
	
        /*Torrent t = new Torrent("[kickass.so]the.interview.2014.1080p.5.1.dd.custom.nl.subs.unlimitedmovies.torrent");*/
        SHA1 sha1 = new SHA1();

        //Message m = t.getMetainfo();
        //Message info_dict = (Message) m.get("info");
        //String info = info_dict.bencode();
        //System.out.println(info);
	// String info_hash = sha1.digest(info_bytes);
        //System.out.println(info_hash);
	//System.out.println(b);
	String good = new String(w, StandardCharsets.ISO_8859_1);
	/*if (info_s == good) {
	    System.out.println("good");
	} else {
	    System.out.println("bad");
	    System.out.println("good is " + good);
	    System.out.println("bad is " + info_s);
	    }*/
	System.out.println(sha1.digest(good));
    }
}
