/*
 * abstracts the common task of having a thread that repeats the same task until it is interrupted
 */

import java.io.IOException;

abstract public class LoopThread extends Thread {

    protected abstract void task() throws Exception; // the task method will be repeated until the thread is interrupted

    public void run() {
        try {
            while (!isInterrupted()) {
                task();
            }
            synchronized (this) {
                cleanup();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    abstract protected void cleanup() throws Exception; // cleanup is run after interrupt

    public static void main(String[] args) {
        TestThread t = new TestThread();
        t.start();
        try { Thread.sleep(100); } catch (InterruptedException e) {}
        t.interrupt();
        System.out.println("we out");
    }
}

class TestThread extends LoopThread {
    protected void task() {
        System.out.println("running");
    }

    protected void cleanup() {
        System.out.println("cleanup");
    }

}
