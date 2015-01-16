/*
 * represents a Peer to which the Client is connected
 * has two jobs:
 *  - listen for incoming messages (Receiver)
 *  - send outgoing messages (Sender)
 * uses seperate Threads for both, see Receiver.java and Sender.java respectively
 */

import java.net.Socket;
import java.io.IOException;
import java.io.Closeable;

public class Peer implements Closeable, AutoCloseable {
    public  Torrent  torrent;
    public  Socket   socket;
    public  Receiver receiver;
    public  Sender   sender;
    // private Sepuker  sepuker;
    public  boolean  closed;

    public Peer(Socket socket) throws IOException {
        this.socket = socket;
        closed = false;
        startReceiver();
        startSender();
        // startSepuker();
    }

    private void startReceiver() throws IOException {
        receiver = new Receiver(this);
        receiver.start();
    }

    private void startSender() throws IOException {
        sender = new Sender(this);
        sender.start();
    }

    // private void startSepuker() {
    //     sepuker = new Sepuker(this);
    //     sepuker.start();
    // }

    public void send(String message) {
        sender.send(message);
    }

    public String toString() {
        return socket.toString();
    }

    public void close() throws IOException {
        System.out.println("closing receiver");
        closeLoopThread(receiver);
        System.out.println("closing sender");
        closeLoopThread(sender);
        System.out.println("closing socket");
        socket.close();
        System.out.println("done");
        closed = true;
        // Torrent.removePeer(self);
    }

    public void closeLoopThread(LoopThread l) throws IOException {
        synchronized (l) {
            System.out.println("telling him to stop");
            l.interrupt(); // tell the thing to stop
        }
        // try {
            System.out.println("waiting");
            // l.join(); // wait for it to actually stop
        // } catch (InterruptedException e){} // we want it to exit so InterruptedExceptions are good
    }

    public static void main (String[] args) throws IOException {
        System.out.println("start: " + System.currentTimeMillis());
        Peer p = new Peer(new Socket("localhost", 3000));
        // p.close();
        Receiver r = p.receiver;
        // try {Thread.sleep(100);} catch (InterruptedException e) {}
        // t.interrupt();
        // try {
        //     t.join();
        // } catch (InterruptedException e){} // we want it to exit so InterruptedExceptions are good
        p.closeLoopThread(r);
        System.out.println("we got interrupted at " + System.currentTimeMillis());
        System.exit(0);
    }
}

// class Sepuker extends Thread {
//     private Peer peer;

//     public Sepuker(Peer peer) {
//         this.peer = peer;
//     }

//     public void run() {
//         // try {
//         //     Thread.sleep(100);
//         // } catch (InterruptedException e) {}
//         try {
//             peer.close();
//         } catch (IOException e) {
//             System.out.println(e.getMessage());
//         }
//     }

//     public int minutes(int mins) {
//         return mins * 60 * 1000;
//     }
// }
