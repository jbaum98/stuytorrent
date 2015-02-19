public interface Tracker {
    public boolean         isStarted();
    public TrackerResponse start(long uploaded, long downloaded, long left);
    public TrackerResponse update(long uploaded, long downloaded, long left);
    public TrackerResponse stop(long uploaded, long downloaded, long left);
}

interface TrackerResponse {
    public boolean successful();
    public String  failureReason();
    public Peer[]  peers();
}
