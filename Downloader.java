import java.util.ArrayList;
import java.util.Random;

public class Downloader extends LoopThread {
    private final Peer peer;
    private final Piece[] pieces;
    
    public Downloader(Peer peer, Piece[] pieces) {
        this.peer = peer;
        this.pieces = pieces;
        peer.unchoke();
        peer.interested();
    }
    
    protected void task() {
        if (!(peer.peer_choking())) {
            ArrayList<Piece> needs = getNeededPieces();
            Random r = new Random();
            if (needs.size() == 0) {
                return;
            }
            for (int i = 0; i < 10; i++) {
                Piece p = needs.get(r.nextInt(needs.size()));
                Chunk chunk = p.getRequest();
                if (chunk != null) {
                    peer.send(chunk.toRequest(p.index));
                } else if (!(p.done.get())) {
                    peer.torrent.done(p);
                }
            }
            try {
                this.sleep(10000/needs.size());
            } catch (InterruptedException e) {
                interrupt();
            }
        }
    }

    protected void cleanup() {}

    private ArrayList<Piece> getNeededPieces() {
        ArrayList<Piece> out = new ArrayList<Piece>();
        boolean[] gots = peer.bitfield.getBits();
        for (int i = 0; i < gots.length; i++) {
            if (gots[i]) {
                Piece piece = pieces[i];
                if (!(piece.done.get())) {
                    out.add(piece);
                }
            }
        }
        return out;
    }
}
