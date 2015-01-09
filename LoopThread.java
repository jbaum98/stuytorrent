import java.io.IOException;

abstract public class LoopThread extends Thread {

    protected abstract void task() throws Exception;

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

    abstract protected void cleanup() throws Exception;
}
