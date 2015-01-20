import java.util.ArrayList;
import java.util.Map;

/**
 * produces Bencoded representation from {@link BencodingMap}
 * @see <a href="https://wiki.theory.org/BitTorrentSpecification#Bencoding">Bit Torrent Specification</a>
 */
public class BencodingWriter{

    /** produces bencoded integer */
    public String i(long l){
        return "i"+(Long.toString(l))+"e";
    }

    /** produces bencoded string */
    public String s(String str) {
        return (Integer.toString(str.length()))+":"+str;
    }

    /** used in {@link l} and {@link d} to handle nested types*/
    private String many(Object[] list){
        String out = new String();
        for(Object o : list){
            if (o instanceof Long){
                out+=i((long)o);
            } else if (o instanceof String) {
                out+=s((String)o);
            } else if (o instanceof ArrayList){
                out+=l((ArrayList)o);
            } else if (o instanceof BencodingMap){
                out+=d((BencodingMap)o);
            } else {
                throw new IllegalArgumentException();
            }
        }
        return out;
    }

    /** produces bencoded list */
    public String l(ArrayList a){
        String out = new String("l");
        out+=many(a.toArray());
        out+="e";
        return out;
    }

    /** produces bencoded dictionary */
    public String d(BencodingMap t) {
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
