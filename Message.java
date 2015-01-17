/*
 * encapsulates the Bencoding parser in a Hash object
 * by passing Bencoded string to the constructor
 * and parsing it
 * for more info on bencoding see https://wiki.theory.org/BitTorrentSpecification#Bencoding
 */

import java.io.*;
import java.util.*;
import java.nio.charset.StandardCharsets;

/*
 * parses Bencoded data
 * basic algorithm keeps track of position in counter
 * and calls the requisite method depending on the type
 * it encounters. This creates nested calls that reflect the
 * nested data structures (lists and dictionaries).
 */
class BencodingParser {
    private String data;
    private int counter; // current position

    public BencodingParser(String data) {
        this.data = data;
        this.counter = 0;
    }

    private boolean isDigit(char c) {
        return (c >= '0' && c <= '9');
    }

    /*
     * parse an integer
     * letter i used for integer, but we sometimes need to parse ints > 2^31
     * so we use longs
     */
    public long i() {
        int start = counter;
        int end = data.indexOf('e', counter);
        counter = end + 1;
        return Long.parseLong(data.substring(start+1,end));
    }

    /*
     * parse a string
     */
    public String s() {
        int colonIndex = data.indexOf(':', counter);
        int length = Integer.parseInt(data.substring(counter,colonIndex));
        int end = colonIndex + length + 1; // one after last char
        counter = end;
        return data.substring(colonIndex+1,end);
    }

    /*
     * used to parse lists and hashes
     */
    private ArrayList many() {
        ArrayList out = new ArrayList();
        char c = data.charAt(counter);
        while (c != 'e'){
            if (c == 'i') {
                out.add(i());
            } else if (isDigit(c)) {
                out.add(s());
            } else if (c == 'l') {
                out.add(l());
            } else if (c == 'd') {
                out.add(d());
            } else {
                throw new IllegalArgumentException();
            }
            c = data.charAt(counter);
        }
        counter++;
        return out;
    }

    /*
     * parse a list
     */
    public ArrayList l() {
        counter++; //get past l
        return many();
    }

    /*
     * parse a dictionary
     */
    public Message d() {
        counter++; //get past d
        ArrayList parsed = many(); // get a list of elements
        Message out = new Message();
        for (int i = 0; i < parsed.size(); i+=2) {
            out.put(parsed.get(i), parsed.get(i+1)); // every other element is a key or a value
        }
        return out;
    }
}

class BencodingWriter{

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
            } else if (o instanceof Message){
                out+=d((Message)o);
            } else {
                throw new IllegalArgumentException();
            }
        }
        return out;
    }

    public String l(ArrayList a){
        String out = new String("l");
        out+=many(a.toArray());
        out+="e";
        return out;
    }

    public String d(Message t) {
        ArrayList a = new ArrayList();
        for(Object o : t.entrySet()){
            Map.Entry me = (Map.Entry)o;
            a.add(me.getKey());
            a.add(me.getValue());
        }
        String out = new String("d");
        out+=many(a.toArray());
        out+="e";
        return out;
    }
}

public class Message extends TreeMap {
    public Message() {
    }

    public Message(String data) {
        super((new BencodingParser(data).d()));
    }

    public String bencode() {
        return (new BencodingWriter()).d(this);
    }

    public static void main(String[] args) {
        // System.out.println(bp.parseInt("i43e"));
        // System.out.println(bp.parseInt("i124e"));
        // System.out.println(bp.parseString("4:cats"));
        // System.out.println(bp.parseString("5:cats"));
        // System.out.println(bp.parseString("cats"));
        Message m = new Message("d2:itl4:cats6:iamsofllelelllli0eeeeei12e2:noi1ee4:dogs3:hiei0eee");
        System.out.println(m);
        System.out.println(m.bencode());
        // String dict = "d3:onei1e3:twoi2e5:threei3e4:listli1ei2ei3eee";
        // TreeMap out2 = bp.parseDictionary("d4:meta"+dict+"e");
        // System.out.println(out2);
    }
}
