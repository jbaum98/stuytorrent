import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.Random;

/**
 * represents a piece of a {@link Torrent}
 */

public class Piece {
    private byte[] data;
    private final byte[] hash;
    private final static SHA1 sha1 = new SHA1();
    private ArrayList<Chunk> need;
    public  final AtomicBoolean done;

    public Piece(byte[] hash, int length) {
        this.done = new AtomicBoolean(false);
        this.hash = hash;
        this.data = new byte[length];

        fillNeed();
    }

    private synchronized void fillNeed() {
        this.need = new ArrayList<Chunk>();

        int num_chunks = data.length / 16384;
        int overflow = data.length % 16384;

        if (overflow == 0) {
            num_chunks--;
            overflow = 16384;
        }

        for (int i = 0; i < num_chunks; i++) {
            need.add(new Chunk(i*16384, (i+1)*16384));
        }
        need.add( new Chunk(num_chunks * 16384, data.length) ); 
    }
    
    public synchronized void setData(int begin, byte[] block) {
        System.out.println("get some");
        if (begin % 16384 != 0) {
            throw new IllegalArgumentException("Max is a bitch/ not a multiple");
        }
        int index = findChunk(begin);
        if (index != -1) {
            setBytes(begin, block);
            synchronized (need) {
                need.remove(index);
            }
	}
    }

    private void setBytes(int begin, byte[] block) {
        for (int i = 0; i < block.length; i++) {
            data[begin + i] = block[i];
        }

    }

    private int findChunk(int begin) {
        int high = need.size();
        int low = -1;
        while(high-low>1){
            int i = (high + low)/2;
            Chunk chunk = need.get(i);
	    int c = begin - chunk.begin;
	    if(c==0){
		return i;
	    }
	    if(c>0){
		low=i;
	    }
	    if(c<0){
		high=i;
	    }
	}
	return -1;
    }
    
    public synchronized byte[] getBytes(int offset, int length) {
        return Arrays.copyOfRange(data, offset, offset+length);
    }

    public synchronized Chunk getRequest() {
        if (need.size() > 0) {
            Random r = new Random();
            return need.get(r.nextInt(need.size()));
        } else {
            // WE'RE DONE!!!
            if (Arrays.equals(calculateHash(), hash)) {
                done.set(true);
                return null;
            } else {
                clear(); // ;o
                return getRequest();
            }
        }
    }
    
    private void clear() {
        fillNeed(); 
        data = new byte[data.length];
    }
        
    private byte[] calculateHash() {
        return sha1.digest(data);
    }

}

class Chunk {
    public final int begin;
    public final int end;
    public final int length;
    
    public Chunk(int begin, int end) {
        this.begin = begin;
        this.end = end;
        this.length = end - begin;
    }

    public Request toRequest(int index) {
        return new Request(index, begin, length);
    }
}
