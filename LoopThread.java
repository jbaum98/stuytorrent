import java.io.IOException;

/**
 * abstracts the common task of having a thread that repeats the same task until it is interrupted
 */
abstract public class LoopThread extends Thread {

    /**
     * will be repeated until the thread is interrupted
     */
    protected abstract void task();

    /** @see Thread#run */
    public void run() {
        while (!isInterrupted()) {
            task();
        }
        synchronized (this) {
            cleanup();
        }
        interrupt();
    }

    /** will be run after the {@link LoopThread} is Interrupted */
    abstract protected void cleanup();
}
