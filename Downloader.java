import java.util.ArrayList;

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
        ArrayList<PieceWithIndex> needs = getNeededPieces();
        for (PieceWithIndex p : needs) {
            Chunk chunk = p.piece.getRequest();
            if (chunk != null) {
                peer.send(chunk.toRequest(p.index));
            }
        }
        try {
            this.sleep(1000);
        } catch (InterruptedException e) {
            interrupt();
        }
    }

    protected void cleanup() {}

    private ArrayList<PieceWithIndex> getNeededPieces() {
        ArrayList<PieceWithIndex> out = new ArrayList<PieceWithIndex>();
        boolean[] gots = peer.bitfield.getBits();
        synchronized (pieces) {
            for (int i = 0; i < gots.length; i++) {
                if (gots[i]) {
                    Piece piece = pieces[i];
                    if (!(piece.done.get())) {
                        out.add(new PieceWithIndex(piece, i));
                    }
                }
            }
        }
        return out;
    }
}

class PieceWithIndex {
    public final Piece piece;
    public final int index;
    
    public PieceWithIndex(Piece piece, int index) {
        this.piece = piece;
        this.index = index;
    }
}
                        
