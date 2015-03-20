package stuytorrent;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * parses Bencoded data
 * @see <a href="https://wiki.theory.org/BitTorrentSpecification#Bencoding">Bit Torrent Specification</a>
 */
public class BencodingParser {
    private String data;
    /** current position */
    private int index;

    public BencodingObj parse(String s) {
        this.data = s;
        this.index = 0;
        ArrayList<BencodingObj> out = new ArrayList<BencodingObj>();
        parse(out);
        if (out.size() == 1) {
            return out.get(0);
        } else {
            return new BencodingObj<ArrayList<BencodingObj>>(out, data);
        }
    }

    /**
     * used to parse lists and hashes
     */
    private void parse(ArrayList<BencodingObj> out) {
        char c;
        while (index < data.length() && (c = data.charAt(index)) != 'e'){
            if (c == 'i') {
                i(out);
            } else if (isDigit(c)) {
                s(out);
            } else if (c == 'l') {
                l(out);
            } else if (c == 'd') {
                d(out);
            } else {
                throw new IllegalArgumentException();
            }
        }
    }

    private boolean isDigit(char c) {
        return (c >= '0' && c <= '9');
    }

    /**
     * parse an integer.
     * letter i used for integer, but we sometimes need to parse ints &gt; 2^31
     * so we use longs
     */
    private void i(ArrayList<BencodingObj> out) {
        int end = data.indexOf('e', index);
        long value = Long.parseLong(data.substring(index+1,end));
        String original = data.substring(index,end+1);
        out.add(new BencodingObj<Long>(value, original));
        index = end + 1;
    }

    /**
     * parse a string
     */
    private void s(ArrayList<BencodingObj> out) {
        int colonIndex = data.indexOf(':', index);
        int length = Integer.parseInt(data.substring(index,colonIndex));
        int end = colonIndex + length;
        String value = data.substring(colonIndex+1,end+1);
        String original = data.substring(index, end+1);
        out.add(new BencodingObj<String>(value, original));
        index = end + 1;
    }


    /**
     * parse a list
     */
    private void l(ArrayList<BencodingObj> out) {
        int start = index;
        index++; //get past l
        ArrayList<BencodingObj> me = new ArrayList<BencodingObj>();
        parse(me);
        out.add(new BencodingObj<ArrayList<BencodingObj>>(me,data.substring(start,index+1)));
        index++; //get past e
    }

    /**
     * parse a dictionary
     */
    private void d(ArrayList<BencodingObj> out) {
        int start = index;
        index++; //get past d
        ArrayList<BencodingObj> me = new ArrayList<BencodingObj>();
        parse(me);
        HashMap<String, BencodingObj> map = new HashMap<String, BencodingObj>();
        for (int i = 0; i + 1 < me.size(); i+=2) {
            map.put((String) me.get(i).value, me.get(i+1)); // every other element is a key or a value
        }
        out.add(new BencodingObj<HashMap<String, BencodingObj>>(map,data.substring(start,index+1)));
        index++; //get past e
    }

    public static void main(String[] args) {
        BencodingParser bp = new BencodingParser();
        System.out.println(bp.parse(args[0]));
    }
}

class BencodingObj<T> {
    public final T      value;
    public final String original;

    public BencodingObj(T value, String original) {
        this.value = value;
        this.original = original;
    }

    public String toString() {
        return value.toString();
    }

    public static void main(String[] args) {
        String s1 = "d3:onei1e3:twoi2e4:listli1ei2eee";
        String s2 = "d4:this4:thate";
        BencodingParser bp = new BencodingParser();
        System.out.println(bp.parse(s1));
        System.out.println(bp.parse(s2));
    }
}
