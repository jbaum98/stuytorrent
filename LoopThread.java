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
            e.printStackTrace(System.out);
        }
        interrupt();
    }

    abstract protected void cleanup() throws Exception; // cleanup is run after interrupt
}
