import java.util.*;
import java.io.*;

class torrent{
    private Message info;

    public torrent(String filename) {
	try{
	    BufferedReader r = new BufferedReader(new FileReader(filename));
	    String s = r.readLine();
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
