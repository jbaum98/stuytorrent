abstract public class Runner implements Runnable {
    protected boolean running = true;

    protected abstract void task();

    public void run() {
        while (isRunning()) {
            task();
        }
    }

    public synchronized boolean isRunning() {
        return running;
    }

    public synchronized void stop() {
        running = false;
    }
}
