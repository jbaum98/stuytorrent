package stuytorrent.peer;

import java.io.OutputStream;
import java.io.IOException;

import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

import stuytorrent.peer.message.Message;

public class Sender {
    private final ExecutorService exec = Executors.newSingleThreadExecutor();
    private final OutputStream out;
    private final Runnable killPeer;

    public Sender(OutputStream out, Runnable killPeer) {
        this.out = out;
        this.killPeer = killPeer;
    }

    public void send(byte[] m) {
        exec.submit(new SendTask(this, out, m));
    }

    public void send(Message m) {
        send(m.toBytes());
    }

    public synchronized void shutdown() {
        exec.shutdown();
        try {
            out.close();
        } catch (IOException e) {
            System.out.println("Error closing OutputStream");
        }
    }

    public void closePeer() {
        (new Thread(killPeer)).start();
    }
}

class SendTask implements Runnable {
    private final Sender sender;
    private final byte[] m;
    private final OutputStream out;

    public SendTask(Sender sender, OutputStream out, byte[] m) {
        this.sender = sender;
        this.out = out;
        this.m   = m;
    }

    public void run() {
        Integer status = 0; // 0 is all good
        try {
            out.write(m);
        } catch (IOException e) {
            System.out.println("Error on sending");
            sender.closePeer();
        }
    }
}
