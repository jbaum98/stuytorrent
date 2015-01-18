import java.util.HashSet;

/** encapsulates a list of Torrents */

public class TorrentList extends HashSet<Torrent> {
    private static final long serialVersionUID = 2005891835595747228L; // to make java happy

    /** finds a torrent with a given info_hash */
    public synchronized Torrent getTorrent(byte[] info_hash) {
        for (Torrent t: this) {
            if (t.info_hash.equals(info_hash)) {
                return t;
            }
        }
        return null;
    }
}
