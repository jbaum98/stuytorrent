import java.util.*;
import java.io.*;

class torrent{
    private Message info;

    public torrent(String filename) {
	try{
	    String s = new Scanner(new File(filename)).useDelimiter("\\A").next();
	    info = new Message(s);
	}catch(IOException e){
	    e.printStackTrace();
	}
    }

    public Message getInfo(){
	return info;
    }
}

public class test{
    public static void main(String[] args){
	torrent t = new torrent("[kickass.so]the.interview.2014.1080p.5.1.dd.custom.nl.subs.unlimitedmovies.torrent");
	System.out.println(t.getInfo());
    }
}
