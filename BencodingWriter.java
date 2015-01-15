import java.io.*;
import java.util.*;

public class BencodingWriter{

    public String i(long l){
        return "i"+(Long.toString(l))+"e";
    }

    public String s(String str) {
        return (Integer.toString(str.length()))+":"+str;
    }

    private String many(Object[] list){
        String out = new String();
	for(Object o : list){
            if (o instanceof Long){
                out+=i((long)o);
            } else if (o instanceof String) {
                out+=s((String)o);
            } else if (o instanceof ArrayList){
                out+=l((ArrayList)o);
            } else if (o instanceof TreeMap){
                out+=d((TreeMap)o);
            } //else {
	    //throw new IllegalArgumentException();
            //}
        }
        return out;
    }

    public String l(ArrayList a){
        String out = new String("l");
	out+=many(a.toArray());
	out+="e";
	return out;
    }

    public String d(TreeMap t) {
	System.out.println(((t.entrySet()).toArray()).toString());
	String out = new String("d");
	out+=many((t.entrySet()).toArray());
	out+="e";
        return out;
    }

    public static void main(String[] args) {
        Message m = new Message("d2:itl4:cats6:iamsofllelelllli0eeeeei12e2:noi1ee4:dogs3:hiei0eee");
	BencodingWriter w = new BencodingWriter();
	System.out.println(w.d(m));
    }
}
