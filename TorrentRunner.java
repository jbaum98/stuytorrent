public class TorrentRunner extends Thread {
    private Torrent torrent;
   
    public TorrentRunner(Torrent torrent) {
        this.torrent = torrent;
    }

    public void run() {
        while (!torrent.done()) {
            System.out.println(
        }
    }
