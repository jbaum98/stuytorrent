import java.util.HashMap;
import java.util.Map;
import java.util.Formatter;
import java.nio.charset.StandardCharsets;
import java.nio.charset.Charset;
import java.net.URLEncoder;
import java.io.UnsupportedEncodingException;

public class QueryMap extends HashMap<String,String> {
    private static final Charset charset = StandardCharsets.ISO_8859_1;
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

    private String encode(byte[] bytes) {
        Formatter formatter = new Formatter();
        for (byte b : bytes) {
            formatter.format("%%%02x", b);
        }
        return formatter.toString();
    }

    private String encode(String s) {
        try {
            return URLEncoder.encode(s, charset.name());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Somehow you lack "+charset.name()+" even though I already checked you had it... this should never happen",e);
        }
    }
}
