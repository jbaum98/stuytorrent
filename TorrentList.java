import java.util.HashSet;

public class TorrentList extends HashSet<Torrent> {
    private static final long serialVersionUID = 2005891835595747228L; // to make java happy

    public synchronized Torrent getTorrent(byte[] info_hash) {
        for (Torrent t: this) {
            if (t.info_hash.equals(info_hash)) {
                return t;
            }
        }
        return null;
    }
}
