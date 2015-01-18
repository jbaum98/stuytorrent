import java.util.HashSet;

public class TorrentList extends HashSet<Torrent> {
    public Torrent getTorrent(byte[] info_hash) {
	for (Object o : this) {
	    Torrent t = (Torrent) o;
	    if (t.info_hash.equals(info_hash)) {
		return t;
	    }
	}
	return null;
    }
}
