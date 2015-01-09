import java.io.IOException;

abstract public class LoopThread extends Thread {
    private boolean running = true;
    private LoopThreadParent parent = null;

    public LoopThread(LoopThreadParent parent) {
        this.parent = parent;
    }
    public LoopThread() {
        this(null);
    }


    protected abstract void task() throws Exception;

    public void run() {
        try {
            while (!isInterrupted()) {
                task();
            }
            synchronized (this) {
                cleanup();
                if (parent != null) {
                    parent.setNotified();
                }
                synchronized (parent) {
                    parent.notify();
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    abstract protected void cleanup() throws Exception;
}
