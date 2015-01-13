/*
 * encapsulates the Bencoding parser in a Hash object
 * by passing Bencoded string to the constructor
 * and parsing it
 * for more info on bencoding see https://wiki.theory.org/BitTorrentSpecification#Bencoding
 */

import java.io.*;
import java.util.*;

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
     * so we use doubles
     */
    public double i() {
        int start = counter;
        int end = data.indexOf('e', counter);
        counter = end + 1;
        return Double.parseDouble(data.substring(start+1,end));
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
    public TreeMap d() {
        counter++; //get past d
        ArrayList parsed = many(); // get a list of elements
        TreeMap out = new TreeMap();
        for (int i = 0; i < parsed.size(); i+=2) {
            out.put(parsed.get(i), parsed.get(i+1)); // every other element is a key or a value
        }
        return out;
    }
}

public class Message extends TreeMap {
    public Message(String data) {
        super((new BencodingParser(data)).d()); // parse data and add to self
    }

    public static void main(String[] args) {
        // System.out.println(bp.parseInt("i43e"));
        // System.out.println(bp.parseInt("i124e"));
        // System.out.println(bp.parseString("4:cats"));
        // System.out.println(bp.parseString("5:cats"));
        // System.out.println(bp.parseString("cats"));
        Message m = new Message("d2:itl4:cats6:iamsofllelelllli0eeeeei12e2:noi1ee4:dogs3:hiei0eee");
        System.out.println(m);
        // String dict = "d3:onei1e3:twoi2e5:threei3e4:listli1ei2ei3eee";
        // TreeMap out2 = bp.parseDictionary("d4:meta"+dict+"e");
        // System.out.println(out2);
    }
}
