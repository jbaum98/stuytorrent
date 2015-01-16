import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.io.IOException;
import java.net.URLEncoder;

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
        Torrent t = new Torrent("[kickass.so]the.interview.2014.1080p.5.1.dd.custom.nl.subs.unlimitedmovies.torrent");
        SHA1 sha1 = new SHA1();
        Message m = t.getMetainfo();
        Message info_dict = (Message) m.get("info");
        String info = info_dict.bencode();
        System.out.println(info);
        String info_hash = sha1.digest(info);
        System.out.println(info_hash);
    }
}
