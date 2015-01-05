import java.io.*;
import java.util.*;

public class BencodingParser {
    public int parseInt(String data) {
        if (data.charAt(0) != 'i' || data.charAt(data.length() - 1) != 'e') {
            throw new IllegalArgumentException("Data must be valid bencoded integer.");
        }
        int out;
        try {
            out = Integer.parseInt(data.substring(1, data.length() - 1));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Data must be valid bencoded integer.");
        }
        return out;
    }

    public String parseString(String data) {
        int sepIndex = data.indexOf(":");
        if (sepIndex < 0) {
            throw new IllegalArgumentException("Data must be valid bencoded string.");
        }
        int length;
        try {
            length = Integer.parseInt(data.substring(0, sepIndex));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Data must be valid bencoded string.", e);
        }
        if ( length != data.length() - (sepIndex+1) )  { // check that length matches
            throw new IllegalArgumentException("Length given must match length of string.");
        }
        return data.substring(sepIndex + 1);
    }

    public ArrayList parseList(String data) {
        if (data.charAt(0) != 'l') {
            throw new IllegalArgumentException("Data must be valid bencoded list.");
        }
        int open = 1;
        int close = 0;
        ArrayList out = new ArrayList();
        for (int i = 1; i < data.length(); i++) {
            char c = data.charAt(i);
            if (c == 'i') {
                int opening_index = i;
                while (c != 'e') {
                    i++;
                    if (i == data.length()) {
                        throw new IllegalArgumentException("Data must be valid bencoded list.");
                    }
                    c = data.charAt(i);
                }

            }
        }
    }
    // public HashMap parseHash(String data) {
    //     if (data.charAt(0) != 'd' || data.charAt(data.length() - 1) != 'e') {
    //         throw new IllegalArgumentException("Data must be valid bencoded hash.");
    //     }
    //     HashMap out = new HashMap();
    //     int start = 0;
    //     boolean key = false;
    //     for (int i = 0; i < data.length(); i++) {
    //         char first = data.charAt(start);
    //         if (first == 'i') {
    //             int closingIndex = data.indexOf("e", start);
    //             if (closingIndex == -1) {
    //                 throw new IllegalArgumentException("Data must be valid bencoded hash.");
    //             } else {
    //                 out.add(data.substring(0, closingIndex));
    //                 i = closingIndex;
    //             }
    //         } else if (first > '0' && first < '9') { // checks if first is a digit
    //             int sepIndex = data.indexOf(":", start);
    //             // int length = data.
    //         }
    //     }
    //     return soFar;
    // }

    public static void main(String[] args) {
        BencodingParser bp = new BencodingParser();
        System.out.println(bp.parseInt("i43e"));
        System.out.println(bp.parseString("4:cats"));
        // System.out.println(bp.parseString("5:cats"));
        System.out.println(bp.parseString("cats"));
    }
}
