import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.io.IOException;

public class Torrent{
    private Message info;

    public Torrent(String filename) {
        Path p = Paths.get("../interview.torrent");
        byte[] b = new byte[0];
        try {
            b = Files.readAllBytes(p);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String s = new String(b, StandardCharsets.US_ASCII);
        info = new Message(s);
    }

    public Message getInfo(){
        return info;
    }

    public static void main(String[] args){
        Torrent t = new Torrent("[kickass.so]the.interview.2014.1080p.5.1.dd.custom.nl.subs.unlimitedmovies.torrent");
        System.out.println(t.getInfo());
    }
}
