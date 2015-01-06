import java.io.*;
import java.util.*;

public class BencodingParser {
    private String data;
    private int counter;
    public TreeMap dict;

    public BencodingParser(String data) {
        this.data = data;
        counter = 0;
        this.dict = d();
    }

    private boolean isDigit(char c) {
        return (c >= '0' && c <= '9');
    }

    private int i() {
        int start = counter;
        int end = data.indexOf('e', counter);
        counter = end + 1;
        return Integer.parseInt(data.substring(start+1,end));
    }

    private String s() {
        int colonIndex = data.indexOf(':', counter);
        System.out.println("Counter " + counter);
        System.out.println("colonIndex " + colonIndex);
        System.out.println(data.substring(counter,colonIndex));
        int length = Integer.parseInt(data.substring(counter,colonIndex));
        int end = colonIndex + length + 1; // one after last char
        counter = end;
        return data.substring(colonIndex+1,end);
    }

    private ArrayList many() {
        ArrayList out = new ArrayList();
        char c = data.charAt(counter);
        while (c != 'e'){
            c = data.charAt(counter);
            if (c == 'i') {
                out.add(i()); 
            } else if (isDigit(c)) {
                out.add(s());
            } else if (c == 'l') {
                out.add(l());
            } else if (c == 'd') {
                out.add(d());
            } else if (c == 'e') {
                return out;
            } else {
                throw new IllegalArgumentException();
            }
        }
        return out;
    }

    private ArrayList l() {
        counter++; //get past l
        return many();
    }

    private TreeMap d() {
        counter++; //get past d
        ArrayList parsed = many();
        TreeMap out = new TreeMap();
        for (int i = 0; i < parsed.size(); i+=2) {
            out.put(parsed.get(i), parsed.get(i+1));
        }
        return out;
    }

    public static void main(String[] args) {
        // System.out.println(bp.parseInt("i43e"));
        // System.out.println(bp.parseInt("i124e"));
        // System.out.println(bp.parseString("4:cats"));
        // System.out.println(bp.parseString("5:cats"));
        // System.out.println(bp.parseString("cats"));
        BencodingParser bp = new BencodingParser("d2:itl4:cats6:iamsofllelelllli0eeeeei12e2:noi1ee4:dogs3:hiei0eee");
        System.out.println(bp.dict);
        // System.out.println(out);
        // String dict = "d3:onei1e3:twoi2e5:threei3e4:listli1ei2ei3eee";
        // TreeMap out2 = bp.parseDictionary("d4:meta"+dict+"e");
        // System.out.println(out2);
    }
}
