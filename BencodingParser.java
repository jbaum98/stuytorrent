import java.io.*;
import java.util.*;

class Stack {
    private final static Chunk EMPTY = new emptyChunk();
    ArrayList<Chunk> stack;

    public Stack() {
        stack = new ArrayList<Chunk>();
    }

    public boolean push(Chunk chunk) {
        return stack.add(chunk);
    }

    public Chunk pop() {
        if (stack.size() > 0) {
            return stack.remove(stack.size() - 1);
        } else {
            return null;
        }
    }

    public Chunk active() {
        if (stack.size() > 0) {
            return stack.get(stack.size() -1);
        } else {
            return EMPTY;
        }
    }

    public boolean single() {
        return stack.size() <= 1;
    }

    public String toString() { return stack.toString(); }
}

enum Type {
    INTEGER(false, 'e'), STRING(false, ':'), LIST(true, 'e'), DICTIONARY(true,'e');
    public final boolean nested;
    public final char ending;
    Type(boolean nested, Character ending) {
        this.nested = nested;
        this.ending = ending;
    }
}

class Chunk {
    public final Type type;
    public final Integer start;
    public final boolean nested;
    public final boolean integer;
    public final boolean string;
    public final boolean list;
    public final boolean dictionary;

    public Chunk(Type type, Integer start) {
        this.type       = type;
        this.start      = start;
        this.nested     = type.nested;
        this.integer    = type == Type.INTEGER;
        this.string     = type == Type.STRING;
        this.list       = type == Type.LIST;
        this.dictionary = type == Type.DICTIONARY;
    }
    public Chunk() {
        this.type       = null;
        this.start      = null;
        this.nested     = true; // so doesn't inhibit new chunks being added to stack
        this.integer    = false;
        this.string     = false;
        this.list       = false;
        this.dictionary = false;
    }

    public boolean isEnding(char c) { return c == type.ending; }

    public String toString() {
        return type.toString() + ": " + start;
    }
}

class emptyChunk extends Chunk {
    public boolean isEnding(char c) { return false; }
    public String toString() { return "EMPTY"; }
}

public class BencodingParser {

    private boolean isDigit(char c) { return (c >= '0' && c <= '9'); }

    private void throwError(String type) {
        throw new IllegalArgumentException("Data must be valid bencoded " + type + ".");
    }
    
    public int parseInt(String data) {
        if (data.charAt(0) != 'i' || data.charAt(data.length() - 1) != 'e') {
            throwError("integer");
        }
        return Integer.parseInt(data.substring(1, data.length() - 1));
    }

    public String parseString(String data) {
        int sepIndex = data.indexOf(":");
        if (sepIndex < 0) {
            throwError("string");
        }
        int length = Integer.parseInt(data.substring(0, sepIndex));
        if ( length != data.length() - (sepIndex+1) )  { // check that length matches
            throwError("string");
        }
        return data.substring(sepIndex + 1);
    }

    private ArrayList parseMany(String data) {
        ArrayList out = new ArrayList();
        Stack stack = new Stack();
        for (int i = 0; i < data.length(); i++ ) {
            char c = data.charAt(i);
            if (stack.active().nested) { // we are not in an atomic chunk
                if (c == 'i') {
                    stack.push(new Chunk(Type.INTEGER, i));
                } else if (isDigit(c)) {
                    stack.push(new Chunk(Type.STRING, i));
                } else if (c == 'l') {
                    stack.push(new Chunk(Type.LIST, i));
                } else if (c == 'd') {
                    stack.push(new Chunk(Type.DICTIONARY, i));
                } else if (c != 'e') {
                    throwError("list");
                }
            }
            if (stack.active().isEnding(c)) {
                if (stack.active().string){
                    int length = Integer.parseInt(data.substring(stack.active().start, i));
                    i += length; // now i is one past at last character of string
                }

                if (stack.single()) { // we can write straight to out
                    String toParse = data.substring(stack.active().start, i+1);

                    if (stack.active().integer) {
                        out.add(parseInt(toParse));
                    } else if (stack.active().string) {
                        out.add(parseString(toParse));
                    } else if (stack.active().list) {
                        out.add(parseList(toParse));
                    } else if (stack.active().dictionary) {
                        out.add(parseDictionary(toParse));
                    }
                }

                stack.pop(); // will always be okay because isEnding so not empty so >= 1
            }
        }
        return out;
    }

    public ArrayList parseList(String data) {
        if (data.charAt(0) != 'l') {
            throwError("list");
        }
        return parseMany(data.substring(1, data.length()-1));
    }

    public HashMap parseDictionary(String data) {
        if (data.charAt(0) != 'd') {
            throwError("list");
        }
        ArrayList parsed = parseMany(data.substring(1, data.length()-1));
        if (parsed.size() % 2 == 1) {
            throwError("list");
        }
        HashMap out = new HashMap();
        for (int i = 0; i < parsed.size(); i+=2) {
            out.put(parsed.get(i), parsed.get(i+1));
        }
        return out;
    }

    public static void main(String[] args) {
        BencodingParser bp = new BencodingParser();
        System.out.println(bp.parseInt("i43e"));
        System.out.println(bp.parseInt("i124e"));
        System.out.println(bp.parseString("4:cats"));
        // System.out.println(bp.parseString("5:cats"));
        // System.out.println(bp.parseString("cats"));
        ArrayList out = bp.parseList("l4:cats6:iamsofllelelllli0eeeeei12e2:noi1ee4:dogs3:hiei0ee");
        System.out.println(out);
        String dict = "d3:onei1e3:twoi2e5:threei3e4:listli1ei2ei3eee";
        HashMap out2 = bp.parseDictionary("d4:meta"+dict+"e");
        System.out.println(out2);
    }
}
