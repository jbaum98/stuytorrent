package test;

import java.util.Random;
import java.util.Arrays;
import java.io.OutputStream;

public class Sender {
    public static peer.Sender s = new peer.Sender(new OutputStreamMock());

    public static void main(String[] args) {
        Random r = new Random();

        for (int i = 0; i < 1000; i++) {
            (new Thread(new sendTask(r.nextInt(5000)))).start();
        }

        try {
            Thread.sleep(6000);
        } catch (InterruptedException e) {}

        s.shutdown();

    }

    static class sendTask implements Runnable {
        private final long delay; 

        public sendTask(long delay) {
            this.delay = delay;
        }

        public void run() {
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {}
            s.send((""+delay).getBytes());
        }
    }
}


class OutputStreamMock extends OutputStream {

    public void write(byte[] b) {
        System.out.println("wrote " + Arrays.toString(b) + " to stream");
    }

    public void write(int b) {}
}
