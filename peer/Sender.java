package stuytorrent.peer;

import java.io.OutputStream;
import java.io.IOException;

import java.util.concurrent.Callable;
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
        exec.submit(new SendTask(m));
    }

    public void send(Message m) {
        send(m.toBytes());
    }

    public synchronized void shutdown() {
        try {
            out.close();
        } catch (IOException e) {
            System.out.println("Error on sending");
        }
        exec.shutdown();
    }

    public void closePeer() {
        (new Thread(killPeer)).start();
    }

    class SendTask implements Callable<Void> {
        private final byte[] m;

        public SendTask(byte[] m) {
            this.m   = m;
        }

        @Override
        public Void call() throws Exception {
            Integer status = 0; // 0 is all good
            try {
                out.write(m);
            } catch (IOException e) {
                Sender.this.closePeer();
            }
            return null;
        }
    }

}
