import java.net.Socket;
import java.io.IOException;

public class TestThread extends LoopThread {
    protected void task() {
        System.out.println("running");
    }

    protected void cleanup() {
        System.out.println("cleanup");
    }

    public static void main(String[] args) throws IOException {
        TestThread t = new TestThread();
        t.start();
        // try { Thread.sleep(100); } catch (InterruptedException e) {}
        // t.interrupt();
        // try {t.join(); } catch (InterruptedException e) {}
        synchronized (t) {
            System.out.println("telling him to stop");
            t.interrupt(); // tell the thing to stop
        }
        try {
            System.out.println("waiting");
            t.join(); // wait for it to actually stop
        } catch (InterruptedException e){} // we want it to exit so InterruptedExceptions are good
        System.out.println("we out");
    }

}
