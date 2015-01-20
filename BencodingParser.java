import java.util.ArrayList;
/**
 * parses Bencoded data
 * @see <a href="https://wiki.theory.org/BitTorrentSpecification#Bencoding">Bit Torrent Specification</a>
 */
public class BencodingParser {
    private String data;
    /** current position */
    private int counter;

    public BencodingParser(String data) {
        this.data = data;
        this.counter = 0;
    }

    private boolean isDigit(char c) {
        return (c >= '0' && c <= '9');
    }

    /**
     * parse an integer.
     * letter i used for integer, but we sometimes need to parse ints &gt; 2^31
     * so we use longs
     */
    public long i() {
        int start = counter;
        int end = data.indexOf('e', counter);
        counter = end + 1;
        return Long.parseLong(data.substring(start+1,end));
    }

    /**
     * parse a string
     */
    public String s() {
        int colonIndex = data.indexOf(':', counter);
        int length = Integer.parseInt(data.substring(counter,colonIndex));
        int end = colonIndex + length + 1; // one after last char
        counter = end;
        return data.substring(colonIndex+1,end);
    }

    /**
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

    /**
     * parse a list
     */
    public ArrayList l() {
        counter++; //get past l
        return many();
    }

    /**
     * parse a dictionary
     */
    public BencodingMap d() {
        counter++; //get past d
        ArrayList parsed = many(); // get a list of elements
        BencodingMap out = new BencodingMap();
        for (int i = 0; i < parsed.size(); i+=2) {
            out.put(parsed.get(i), parsed.get(i+1)); // every other element is a key or a value
        }
        return out;
    }
}
