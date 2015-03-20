package stuytorrent;

import java.util.HashMap;
import java.net.URL;
import java.net.MalformedURLException;
import java.net.HttpURLConnection;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ByteArrayOutputStream;
import java.io.BufferedInputStream;
import java.io.IOException;

public class HttpTracker implements Tracker {
    private boolean started = false;
    private String url;
    private byte[] infoHash;
    private int port;
    private String peerId;

    public HttpTracker(String url, byte[] infoHash, int port, String peerId) {
        this.url = url;
        this.infoHash = infoHash;
        this.port = port;
        this.peerId = peerId;
    }

    public boolean isStarted() {
        return started;
    }

    public TrackerResponse start(long uploaded, long downloaded, long left) {
        TrackerResponse response =  sendRequest(buildRequest(uploaded, downloaded, left, "started"));
        if (response.successful()) {
            started = true;
        }
        return response;
    }

    public TrackerResponse update(long uploaded, long downloaded, long left) {
        TrackerResponse response =  sendRequest(buildRequest(uploaded, downloaded, left, null));
        return response;
    }

    public TrackerResponse stop(long uploaded, long downloaded, long left) {
        TrackerResponse response =  sendRequest(buildRequest(uploaded, downloaded, left, "stopped"));
        if (response.successful()) {
            started = false;
        }
        return response;
    }

    private String buildRequest(long uploaded, long downloaded, long left, String event) {
        QueryMap queries = new QueryMap();

        queries.put("info_hash", infoHash);
        queries.put("port", port);
        queries.put("peer_id", peerId);
        queries.put("uploaded", uploaded);
        queries.put("downloaded", downloaded);
        queries.put("left", left);
        queries.put("compact", 1);
        if (event != null) {
            queries.put("event", event);
        }
        return url + queries;
    }

    private TrackerResponse sendRequest(String request) {
        byte[] trackerResponse;
        URL trackerUrl;
        HttpURLConnection connection;
        try {
            trackerUrl = new URL(url);
        } catch (MalformedURLException e) {
            throw new RuntimeException("Malformed tracker url: " + url, e);
        }
        try {
            connection = (HttpURLConnection) trackerUrl.openConnection();
        } catch(IOException e) {
            throw new RuntimeException("Error connecting to tracker", e);
        }
        try (
            InputStream response = connection.getInputStream();
            BufferedInputStream reader = new BufferedInputStream(response);
            ByteArrayOutputStream tempOutput = new ByteArrayOutputStream();
            ) {
            byte[] buf = new byte[1];

            while(reader.read(buf) != -1)
                tempOutput.write(buf);

            trackerResponse = tempOutput.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Error on updating tracker", e);
        }
        String responseStr = new String(trackerResponse, Globals.CHARSET);
        return new HttpTrackerResponse(responseStr);
    }
}

class HttpTrackerResponse implements TrackerResponse {
    private final BencodingParser bp = new BencodingParser();
    private final HashMap<String, BencodingObj> response;

    public HttpTrackerResponse(String response) {
        this.response = (HashMap<String, BencodingObj>) bp.parse(response).value;
    }

    public boolean successful() {
        return !response.containsKey("failure reason");
    }

    public String failureReason() {
        return successful() ? null : (String) response.get("failure reason").value;
    }

    public Peer[] peers() {
        if (!successful()) {
            return new Peer[0];
        }
        byte[] rawBytes = ((String) response.get("peers").value).getBytes(Globals.CHARSET);

        Peer[] out = new Peer[rawBytes.length/6];

        for (int i = 0; i < rawBytes.length; i+=6) {
            String hostname =
                toI(rawBytes[i])+"."+toI(rawBytes[i+1])+"."+toI(rawBytes[i+2])+"."+toI(rawBytes[i+3]); // construct IP address
            int port = 0xFF & rawBytes[i+4] << 8 & rawBytes[i+5]; // port is stored as 2 bytes, need to be concatenated
            out[i/6] = new Peer(hostname, port);
        }
        return out;
    }

    /** converts a signed byte to an an integer in range [0, 256] */
    private int toI(byte b) {
        return (0xFF & b);
    }
}
