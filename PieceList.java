import java.util.LinkedHashSet;

public class PieceList extends LinkedHashSet<Piece> {

    public PieceList(Info info) {
        super(info.num_pieces());
        int i;
        for (i = 0; i < info.num_pieces() - 1; i++) {
            add(new Piece(i, info.piece_length(), info.hash(i)));
        }
        i++;
        add(new Piece(i, info.overflow(), info.hash(i)));
    }
}
