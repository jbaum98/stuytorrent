import java.net.Socket;
import java.io.IOException;
import java.io.Closeable;
import java.io.BufferedInputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.charset.Charset;

/**
 * represents another torrent Client to which we are connected
 */
public class Peer implements Closeable, AutoCloseable {
    private static final Charset charset = StandardCharsets.ISO_8859_1;
    public  final Socket socket;
    private final BufferedInputStream in;
    private final Receiver            receiver;
    private final OutputStream        out;
    private final Responder           responder;

    private final Torrent torrent;

    public  final String id;
    private final byte[] info_hash;

    private Death death;

    /** if HE is chocking ME */
    private volatile boolean peer_choking    = true;
    /** if HE is interested in  ME */
    private volatile boolean peer_interested = false;
    /** if I am chocking HIM */
    private volatile boolean am_choking      = true;
    /** if I am chocking HIM */
    private volatile boolean am_interested   = false;

    public Bitfield bitfield = new Bitfield();;
    /**
     * called when we are initiating the connection
     * @param socket  the {@link java.net.Socket} to the Peer
     * @param torrent the {@link Torrent} for which we want to download make a connection
     */
    public Peer(Socket socket, Torrent torrent) throws IOException {
        this.socket = socket;
        in = new BufferedInputStream(socket.getInputStream());
        out = socket.getOutputStream();
        receiver = new Receiver(this, in);
        responder = new Responder(receiver, this);

        this.torrent = torrent;

        sendHandshake();

        HandshakeInfo received = receiveHandshake();
        info_hash = received.info_hash;
        id = received.id;

        // verify the info_hash
        if (! info_hash.equals(torrent.info_hash)) {
            close();
            return;
        }

        Message first = receiver.messages.peek();
        if ( first instanceof BitfieldMessage) {
            first.action(this);
        }

        torrent.addPeer(this);
        startDeath();
    }

    /**
     * called when the Peer is intitiating the connection
     * @param socket   the {@link java.net.Socket} to the other client
     * @param torrents the {@link Client#torrents} so we can determine if we are serving the torrent the Peer has requested
     */
    public Peer(Socket socket, TorrentList torrents) throws IOException {
        this.socket = socket;
        in = new BufferedInputStream(socket.getInputStream());
        out = socket.getOutputStream();
        receiver = new Receiver(this, in);
        responder = new Responder(receiver,this);

        HandshakeInfo received = receiveHandshake();
        info_hash = received.info_hash;
        id = received.id;

        // verify info_hash and find torrent
        torrent = torrents.getTorrent(info_hash);
        if (torrent == null) {
            close();
            return;
        }

        sendHandshake();

        Message first = receiver.messages.peek();
        if ( first instanceof BitfieldMessage) {
            first.action(this);
        }

        torrent.addPeer(this);
        startDeath();
        System.out.println(id);
    }

    private void startDeath() {
        death = new Death(this);
        death.start();
    }

    /** sends a handshake
     * <b>{@link torrent}</b> must be set
     */
    public void sendHandshake() throws IOException {
        send(torrent.handshake);
    }

    /**
     * receives a handshake and sets {@link id} and {@link info_hash}
     * @return the info_hash from the handshake
     */
    public HandshakeInfo receiveHandshake() throws IOException {
        byte[] pstrlen, pstr, reserved, info_hash, peer_id_bytes;

        pstrlen = new byte[1];
        in.read(pstrlen);

        pstr = new byte[pstrlen[0]];
        in.read(pstr);

        reserved = new byte[8];
        in.read(reserved);

        info_hash = new byte[20];
        in.read(info_hash);

        peer_id_bytes = new byte[20];
        in.read(peer_id_bytes);

        String id = new String(peer_id_bytes, charset);

        return new HandshakeInfo(info_hash, id);
    }

    public void send(byte[] message) {
        try {
            synchronized (out) {
                out.write(message);
            }
        } catch (IOException e) {
            e.printStackTrace(System.out);
            close();
        }
    }

    public void send(String message) {
        send(message.getBytes());
    }

    // MESSAGE METHODS
    // @see <a href="https://wiki.theory.org/BitTorrentSpecification#Messages">Bit Torrent Specification</a>

    /** called when {@link Peer} recieves a keep-alive message */
    public void keepalive() {
        synchronized (death) {
            death.interrupt();
            death = new Death(this);
        }
    }

    /** called when {@link Peer} recieves a choke message */
    public void choke() {
        peer_choking = true;
    }

    /** called when {@link Peer} recieves an unchoke message */
    public void unchoke() {
        peer_choking = false;
    }

    /** called when {@link Peer} recieves an interested message */
    public void interested() {
        peer_interested = true;
    }

    /** called when {@link Peer} recieves a not interested message */
    public void notInterested() {
        peer_interested = false;
    }

    /** called when {@link Peer} recieves a have message */
    public void have(int piece_index) {
        bitfield.setPresent(piece_index);
    }

    public void bitfield(byte[] bytes) {
        bitfield.override(bytes);
    }

    /** called when {@link Peer} recieves a request message */
    public void request(int index, int begin, int length) {
        send(torrent.getChunk(index, begin, length));
    }

    /** called when {@link Peer} recieves a piece message */
    public void piece(int index, int begin, byte[] block) {
        torrent.addChunk(index, begin, block);
    }

    // CHOKED/INTERESTED GETTERS

    public boolean peer_choking() {
        return peer_choking;
    }

    public boolean peer_interested() {
        return peer_interested;
    }

    public boolean am_choking() {
        return am_choking;
    }

    public boolean am_interested() {
        return am_interested;
    }

    /** closes a {@link Peer} by closing the socket and removing itself from it's {@link Torrent}'s {@link Torrent#peers} */
    public synchronized void close() {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace(System.out);
        }
        receiver.interrupt();
        if (torrent != null) {
            torrent.removePeer(this);
        }
    }

    public String toString() {
        return id;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if ( !(other instanceof Peer)) return false;
        Peer otherPeer = (Peer) other;
        return this.id.equals(otherPeer.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

}

class HandshakeInfo {
    public final byte[] info_hash;
    public final String id;

    public HandshakeInfo(byte[] info_hash, String id) {
        this.info_hash = info_hash;
        this.id = id;
    }
}
