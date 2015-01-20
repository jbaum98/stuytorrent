import java.util.TreeMap;
import java.nio.charset.StandardCharsets;

/**
 * uses {@link BencodingParser} and {@link BencodingWriter} to encapsulate
 * Bencoding operations in a TreeMap
 */

public class BencodingMap extends TreeMap {
    private static final long serialVersionUID = 6649525734686742785L;

    public BencodingMap() {
    }

    public BencodingMap(String data) {
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
        BencodingMap m = new BencodingMap("d2:itl4:cats6:iamsofllelelllli0eeeeei12e2:noi1ee4:dogs3:hiei0eee");
        System.out.println(m);
        System.out.println(m.bencode());
        // String dict = "d3:onei1e3:twoi2e5:threei3e4:listli1ei2ei3eee";
        // TreeMap out2 = bp.parseDictionary("d4:meta"+dict+"e");
        // System.out.println(out2);
    }
}
