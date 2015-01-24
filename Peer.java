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
    public  Socket socket;
    private InputStream in;
    private Receiver            receiver;
    private OutputStream        out;
    private Responder           responder;
    private Downloader          downloader;

    private Torrent torrent;

    private String id;
    private byte[] info_hash;

    private Death death;

    /** if HE is chocking ME */
    private AtomicBoolean peer_choking    = new AtomicBoolean(true);
    /** if HE is interested in  ME */
    private AtomicBoolean peer_interested = new AtomicBoolean(false);
    /** if I am chocking HIM */
    private AtomicBoolean am_choking      = new AtomicBoolean(true);
    /** if I am chocking HIM */
    private AtomicBoolean am_interested   = new AtomicBoolean(false);

    public Bitfield bitfield;
    /**
     * called when we are initiating the connection
     * @param socket  the {@link java.net.Socket} to the Peer
     * @param torrent the {@link Torrent} for which we want to download make a connection
     */
    public Peer(Socket socket, Torrent torrent) throws IOException {
        startDeath();
        this.socket = socket;
        setStreams();
        this.torrent = torrent;

        sendHandshake();
        System.out.println("sent handshake");

        receiveHandshake();
        System.out.println("received handshake");

        if (!verify()) {
            close();
            return;
        }
        System.out.println("verified");

        setBitfield();
        setDownloader();
        torrent.addPeer(this);
        System.out.println("starting");
        startThreads();
        System.out.println("started");
    }

    /**
     * called when the Peer is intitiating the connection
     * @param socket   the {@link java.net.Socket} to the other client
     * @param torrents the {@link Client#torrents} so we can determine if we are serving the torrent the Peer has requested
     */
    public Peer(Socket socket, TorrentList torrents) throws IOException {
        startDeath();
        this.socket = socket;
        setStreams();

        receiveHandshake();

        torrent = torrents.getTorrent(info_hash);
        if (torrent == null) {
            close();
            return;
        }

        sendHandshake();

        setBitfield();
        setDownloader();
        torrent.addPeer(this);
        startThreads();
    }

    private void startDeath() {
        death = new Death(this);
        death.start();
    }

    private void setStreams() throws IOException {
        in = socket.getInputStream();
        out = socket.getOutputStream();
        receiver = new Receiver(this, in);
        responder = new Responder(receiver, this);
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
    public void receiveHandshake() throws IOException {
        byte[] pstrlen, pstr, reserved, peer_id_bytes;

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
        id = new String(peer_id_bytes, charset);
    }

    private boolean verify() {
        return Arrays.equals(torrent.info_hash, info_hash);
    }

    private void setBitfield() {
        bitfield = new Bitfield(torrent.pieces.length);
    }

    private void setDownloader() {
        downloader = new Downloader(this, torrent.pieces);
    }

    private void startThreads() {
        receiver.start();
        responder.start();
        downloader.start();

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
