import java.nio.file.Paths;
import java.io.IOException;

public class Test {
    public static void main(String[] args) {
        try {
         MetaInfo m = new MetaInfoFile(Paths.get("ubuntu_torrentarino"));
         System.out.println(m);
        } catch (IOException e) {
            throw new RuntimeException("Couldn't read file", e);
        }
    }
}
