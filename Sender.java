import java.io.OutputStream;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

public class Sender {
    private final ExecutorService exec = Executors.newSingleThreadExecutor();
    private final OutputStream out;

    public Sender(OutputStream out) {
        this.out = out;
    }

    public void send(byte[] m) {
        exec.submit(new SendTask(m));
    }

    public void send(Message m) {
        send(m.toBytes());
    }

    public void alert() {}

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
                Sender.this.alert();
            }
            return null;
        }
    }

}
