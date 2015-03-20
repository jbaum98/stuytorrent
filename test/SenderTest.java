package stuytorrent.test;

import stuytorrent.peer.Sender;

import java.util.Random;

public class SenderTest {
    public static Sender s = new Sender(new OutputStreamMock(), new EmptyRunnable());

    public static void main(String[] args) {

        for (int i = 0; i < 1000; i++) {
            (new Thread(new sendTask(s))).start();
        }

        try {
            Thread.sleep(6000);
        } catch (InterruptedException e) {}

        s.shutdown();

    }
}

class sendTask implements Runnable {
    private final static Random r = new Random();
    private final long delay;
    private final Sender s;

    public sendTask(Sender s) {
        this.s = s;
        delay = r.nextInt(5000);
    }

    public void run() {
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {}
        s.send((""+delay).getBytes());
    }
}
