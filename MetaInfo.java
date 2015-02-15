import java.util.HashMap;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public interface MetaInfo {
    public String announce();
    public Info   info();
    public byte[] infoHash();
}

class MetaInfoFile implements MetaInfo {
    private static final BencodingParser BP = new BencodingParser();
    private static final SHA1 sha = new SHA1();

    private final String announce;
    private final Info   info;
    private final byte[] infoHash;

    public MetaInfoFile(Path metainfo_file) throws IOException {
        byte[] bytes = Files.readAllBytes(metainfo_file);
        String s = new String(bytes, Globals.CHARSET);
        HashMap<String, BencodingObj> map = (HashMap<String, BencodingObj>) BP.parse(s).value;

        announce = (String) map.get("announce").value;

        BencodingObj<HashMap<String, BencodingObj>> info_obj = (BencodingObj<HashMap<String, BencodingObj>>) map.get("info");
        info  = new InfoSingle(info_obj.value);
        infoHash = sha.digest(info_obj.original);

    }

    public String announce() {
        return announce;
    }

    public Info info() {
        return info;
    }

    public byte[] infoHash() {
        return infoHash;
    }

    public String toString() {
        return "announce: "+announce()+"\n"
            + "info:"+"\n"
            + "\t"+"piece length: " + info.piece_length()+"\n"
            + "\t"+"name: " + info.name()+"\n"
            + "\t"+"length: " + info.length()+"\n"
            + "infoHash: " + SHA1.bytesToHex(infoHash());
    }
}
