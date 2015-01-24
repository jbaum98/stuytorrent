import java.net.Socket;
import java.io.IOException;
import java.io.Closeable;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.charset.Charset;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.Arrays;

/**
 * represents another torrent Client to which we are connected
 */
public class Peer implements Closeable, AutoCloseable {
    private static final Charset charset = StandardCharsets.ISO_8859_1;
    public  final Socket socket;
    private final InputStream in;
    private final Receiver            receiver;
    private final OutputStream        out;
    private final Responder           responder;
    private final Downloader          downloader;

    private final Torrent torrent;

    public  final String id;
    private final byte[] info_hash;

    private Death death;

    /** if HE is chocking ME */
    private AtomicBoolean peer_choking    = new AtomicBoolean(true);
    /** if HE is interested in  ME */
    private AtomicBoolean peer_interested = new AtomicBoolean(false);
    /** if I am chocking HIM */
    private AtomicBoolean am_choking      = new AtomicBoolean(true);
    /** if I am chocking HIM */
    private AtomicBoolean am_interested   = new AtomicBoolean(false);

    public Bitfield bitfield = new Bitfield();
    /**
     * called when we are initiating the connection
     * @param socket  the {@link java.net.Socket} to the Peer
     * @param torrent the {@link Torrent} for which we want to download make a connection
     */
    public Peer(Socket socket, Torrent torrent) throws IOException {
        startDeath();
        this.socket = socket;
        in = socket.getInputStream();
        out = socket.getOutputStream();
        receiver = new Receiver(this, in);
        receiver.start();
        responder = new Responder(receiver, this);
        responder.start();

        this.torrent = torrent;

        sendHandshake();
        System.out.println("send handshake");

        HandshakeInfo received = receiveHandshake();

        System.out.println("received handshake");

        info_hash = received.info_hash;
        id = received.id;

        downloader = new Downloader(this, torrent.pieces);
        System.out.println("our hash: " + SHA1.bytesToHex(torrent.info_hash));
        System.out.println("their hash: " + SHA1.bytesToHex(info_hash));
        // verify the info_hash
        if (! Arrays.equals(torrent.info_hash, info_hash)) {
            close();
            return;
        }
        
        System.out.println("verified");

        Message first = receiver.messages.peek();
        if ( first instanceof BitfieldMessage) {
            first.action(this);
        }

        torrent.addPeer(this);
        downloader.start();
    }

    /**
     * called when the Peer is intitiating the connection
     * @param socket   the {@link java.net.Socket} to the other client
     * @param torrents the {@link Client#torrents} so we can determine if we are serving the torrent the Peer has requested
     */
    public Peer(Socket socket, TorrentList torrents) throws IOException {
        startDeath();
        this.socket = socket;
        in = socket.getInputStream();
        out = socket.getOutputStream();
        receiver = new Receiver(this, in);
        responder = new Responder(receiver,this);

        HandshakeInfo received = receiveHandshake();
        info_hash = received.info_hash;
        id = received.id;

        // verify info_hash and find torrent
        torrent = torrents.getTorrent(info_hash);
        downloader = new Downloader(this, torrent.pieces);
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
        downloader.start();
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
        System.out.println(Arrays.toString(torrent.handshake));
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
        System.out.println("read " + in.read(info_hash));

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

    public void send(Message message) {
        send(message.toBytes());
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
    public void receiveChoke() {
        peer_choking.set(true);
    }

    /** called when {@link Peer} recieves an unchoke message */
    public void receiveUnchoke() {
        peer_choking.set(false);
    }

    /** called when {@link Peer} recieves an interested message */
    public void receiveInterested() {
        peer_interested.set(true);
    }

    /** called when {@link Peer} recieves a not interested message */
    public void receiveNotInterested() {
        peer_interested.set(false);
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

    public void choke() {
        am_choking.set(true);
        send(new Choke());
    }

    public  void unchoke() {
        am_choking.set(false);
        send(new Unchoke());
    }

    public void interested() {
        am_interested.set(true);
        send(new Unchoke());
    }
    
    public void notInterested() {
        am_interested.set(false);
        send(new NotInterested());
    }

    // CHOKED/INTERESTED GETTERS

    public boolean peer_choking() {
        return peer_choking.get();
    }

    public boolean peer_interested() {
        return peer_interested.get();
    }

    public boolean am_choking() {
        return am_choking.get();
    }

    public boolean am_interested() {
        return am_interested.get();
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
