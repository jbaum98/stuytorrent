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
    private final OutputStream        out;

    private final Torrent torrent;

    public  final String id;
    private final byte[] info_hash;

    private Death death;
    /**
     * called when we are initiating the connection
     * @param socket  the {@link java.net.Socket} to the Peer
     * @param torrent the {@link Torrent} for which we want to download make a connection
     */
    public Peer(Socket socket, Torrent torrent) throws IOException {
        this.socket = socket;
        in = new BufferedInputStream(socket.getInputStream());
        out = socket.getOutputStream();

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

        torrent.addPeer(this);
        startDeath();
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

    public void send(byte[] message) throws IOException {
        synchronized (out) {
            out.write(message);
        }
    }

    public void send(String message) throws IOException {
        send(message.getBytes());
    }

    public String toString() {
        return socket.toString();
    }

    public synchronized void close() throws IOException {
        socket.close();
        if (torrent != null) {
            torrent.removePeer(this);
        }
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
