import java.util.HashMap;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public interface MetaInfo {
    public String announce();
    public Info   info();
}

class MetaInfoFile implements MetaInfo {
    private static final Parser BP = new BencodingParser();

    private final String announce;
    private final Info   info;

    public MetaInfoFile(Path metainfo_file) throws IOException {
        byte[] bytes = Files.readAllBytes(metainfo_file);
        String s = new String(bytes, Globals.CHARSET);
        HashMap map = BP.parse(s);

        announce = (String) map.get("announce");

        HashMap info_map = (HashMap) map.get("info");
        info  = new InfoSingle(info_map);
    }

    public String announce() {
        return announce;
    }

    public Info info() {
        return info;
    }
}
