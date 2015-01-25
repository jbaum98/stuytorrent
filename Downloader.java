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
            ArrayList<Piece> need = getNeededPieces();
            Random r = new Random();
            if (need.size() == 0) {
                return;
            }
            for (int i = 0; i < 10; i++) {
                Piece p = need.get(r.nextInt(need.size()));
                Request request = p.getOutstandingRequest();
                // System.out.println("want to send " + request);
                if (request != null) {
                    peer.send(request);
                }
            }
            try {
                sleep(1000/need.size());
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
                if (! (piece.done()) ) {
                    out.add(piece);
                }
            }
        }
        return out;
    }
}
