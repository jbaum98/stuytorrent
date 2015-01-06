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
    public int parseInt(String data) {
        if (data.charAt(0) != 'i' || data.charAt(data.length() - 1) != 'e') {
            throw new IllegalArgumentException("Data must be valid bencoded integer.");
        }
        int out;
        try {
            out = Integer.parseInt(data.substring(1, data.length() - 1));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Data must be valid bencoded integer.");
        }
        return out;
    }

    public String parseString(String data) {
        int sepIndex = data.indexOf(":");
        if (sepIndex < 0) {
            throw new IllegalArgumentException("Data must be valid bencoded string.");
        }
        int length;
        try {
            length = Integer.parseInt(data.substring(0, sepIndex));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Data must be valid bencoded string.", e);
        }
        if ( length != data.length() - (sepIndex+1) )  { // check that length matches
            throw new IllegalArgumentException("Length given must match length of string.");
        }
        return data.substring(sepIndex + 1);
    }

    private void checkLength(int i, String data) {
        if (i == data.length()) {
            throw new IllegalArgumentException("Data must be valid bencoded list.");
        }
    }

    private boolean isDigit(char c) { return (c >= '0' && c <= '9'); }

    private void throwError() {
        throw new IllegalArgumentException("Data must be valid bencoded list.");
    }

    public ArrayList parseList(String data) {
        if (data.charAt(0) != 'l') {
            throwError();
        }
        ArrayList out = new ArrayList();
        Stack stack = new Stack();
        for (int i = 1; i < data.length()-1; i++ ) {
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
                    throwError();
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
                    }
                }

                stack.pop(); // will always be okay because isEnding so not empty so >= 1
                // else if (chunk.dictionary)  { out.add(parseDictionary(toParse)); }
            }
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
    }
}
