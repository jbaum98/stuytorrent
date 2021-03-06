package stuytorrent.peer;

import java.net.Socket;
import java.io.IOException;
import java.io.DataInputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.charset.Charset;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

import stuytorrent.Bitfield;
//import stuytorrent.Torrent;
//import stuytorrent.TorrentList;
import stuytorrent.peer.message.*;

/**
 * represents another torrent Client to which we are connected
 */
public class Peer {
    private final ExecutorService exec = Executors.newSingleThreadExecutor();
    public  Socket socket;
    private Receiver        receiver;
    private Sender          sender;
    //private Downloader      downloader;

    //public Torrent torrent = null;
    private String id; private byte[] info_hash;

    //private Death death;

    /** if HE is chocking ME */
    public final AtomicBoolean peer_choking    = new AtomicBoolean(true);
    /** if HE is interested in  ME */
    public final AtomicBoolean peer_interested = new AtomicBoolean(false);
    /** if I am chocking HIM */
    public final AtomicBoolean am_choking      = new AtomicBoolean(true);
    /** if I am chocking HIM */
    public final AtomicBoolean am_interested   = new AtomicBoolean(false);

    public Bitfield bitfield;
    private AtomicBoolean closed = new AtomicBoolean(false);
    /**
     * called when we are initiating the connection
     * @param socket  the {@link java.net.Socket} to the Peer
     * @param torrent the {@link Torrent} for which we want to download make a connection
     */
    public Peer(Socket socket) throws IOException {
        //startDeath();
        this.socket = socket;
        setStreams();
        //this.torrent = torrent;

        sendHandshake();
        System.out.println("sent handshake");

        //receiveHandshake();
        System.out.println("received handshake");

        if (!verify()) {
            System.out.println("closed because unverified");
            //close();
            return;
        }
        System.out.println("verified");

        setBitfield();
        //setDownloader();
        //torrent.addPeer(this);
        System.out.println("starting");
        startThreads();
        System.out.println("started");
    }

    /**
     * called when the Peer is intitiating the connection
     * @param socket   the {@link java.net.Socket} to the other client
     * @param torrents the {@link Client#torrents} so we can determine if we are serving the torrent the Peer has requested
     */
    public Peer(Socket socket, int poop) throws IOException {
        this.socket = socket;
        setStreams();

        //receiveHandshake();

        //torrent = torrents.getTorrent(info_hash);
        //if (torrent == null) {
        //System.out.println("Couldn't find torrent so closed");
        //close();
        //return;
        //}

        sendHandshake();

        setBitfield();
        //setDownloader();
        //torrent.addPeer(this);
        startThreads();
    }

    public Peer(String hostname, int port) throws IOException {
        this(new Socket(hostname, port));
    }

    private void setStreams() throws IOException {
        receiver = new Receiver(this, new DataInputStream(socket.getInputStream()), getKillPeer());
        sender = new Sender(socket.getOutputStream(), getKillPeer());
    }

    /** sends a handshake
     * <b>{@link torrent}</b> must be set
     */
    public void sendHandshake() throws IOException {
        //send(torrent.handshake);
    }

    /**
     * receives a handshake and sets {@link id} and {@link info_hash}
     * @return the info_hash from the handshake
     */
    // TODO move to Receiver
    /*public void receiveHandshake() throws IOException {
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
      id = new String(peer_id_bytes, stuytorrent.Globals.CHARSET);
      }*/

    private boolean verify() {
        return false;
        //return Arrays.equals(torrent.info_hash, info_hash);
    }

    private void setBitfield() {
        //bitfield = new Bitfield(torrent.pieces.length);
    }

    private void setDownloader() {
        //downloader = new Downloader(this, torrent.pieces);
    }

    private void startThreads() {
        //receiver.start();
        //responder.start();
        //downloader.start();

    }

    public byte[] getChunk(int index, int begin, int length) {
        return new byte[0];
        //return torrent.getChunk(index, begin, length);
    }

    public void submitChunk(int index, int begin, byte[] block) {
        //torrent.addChunk(index, begin, block);
    }

    public Runnable getKillPeer() {
        return new KillPeerTask(this);
    }

    public void choke() {
        am_choking.set(true);
        //send(new Choke());
    }

    public void unchoke() {
        am_choking.set(false);
        //send(new Unchoke());
    }

    public void interested() {
        am_interested.set(true);
        //send(new Interested());
    }

    public void notInterested() {
        am_interested.set(false);
        send(new NotInterested());
    }

    public void send(Message m) {}

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

    public void shutdownSender() {
        sender.shutdown();
    }

    public void shutdownReceiver() {
        //receiver.shutdown();
    }

    public void removeFromPeerList() {
    }

    public void takeAction(Message m) {
        exec.submit(m.action(this));
    }

    public Sender getSender() {
        return sender;
    }

    public synchronized void shutdown() {
        try {
            socket.close();
        } catch (IOException e) {
            System.out.println("Error on closing socket");
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

enum PeerStatus { NOOB, READY, CONNECTED }
