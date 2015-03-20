package stuytorrent;

import java.nio.file.Paths;
import java.io.IOException;

public class Test {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java Test <path to metainfo file>");
            throw new RuntimeException();
        }
        String filename = args[0];
        try {
         MetaInfo m = new MetaInfoFile(Paths.get(filename));
         System.out.println(m);
        } catch (IOException e) {
            throw new RuntimeException("Couldn't read file", e);
        }
    }
}
