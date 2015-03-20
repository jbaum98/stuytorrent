package stuytorrent;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Iterator;
import java.util.Formatter;
import java.net.URLEncoder;
import java.io.UnsupportedEncodingException;

/**
 * extends {@link java.util.HashMap} to convert Strings, byte[]s, ints and longs
 * to proper URL encoding on put
 */

public class QueryMap extends HashMap<String,String> {
    private static final long serialVersionUID = 1567891877869114093L;

    public String put(String key, String value) {
        return super.put(encode(key), encode(value));
    }

    public String put(String key, byte[] value) {
        return super.put(encode(key), encode(value));
    }

    public String put(String key, int value) {
        return super.put(encode(key), encode(Integer.toString(value)));
    }

    public String put(String key, long value) {
        return super.put(encode(key), encode(Long.toString(value)));
    }

    public String toString() {
        String out = new String();
        Set<Map.Entry<String,String>> queries = entrySet();
        Iterator<Map.Entry<String,String>> i = queries.iterator();
        while(i.hasNext()) {
            Map.Entry<String, String> query = i.next();
            out += query.getKey() + "=" + query.getValue();
            if (i.hasNext()) { // not the last one
                out += "&";
            }
        }
        return out;
    }

    private String encode(byte[] bytes) {
        Formatter formatter = new Formatter();
        for (byte b : bytes) {
            formatter.format("%%%02x", b);
        }
        return formatter.toString();
    }

    private String encode(String s) {
        try {
            return URLEncoder.encode(s, Globals.CHARSET.name());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Somehow you lack "+Globals.CHARSET.name()+" even though I already checked you had it... this should never happen",e);
        }
    }
}
