import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicReference;

public class Interval {
    private TreeSet<Boundary> boundaries;

    public Interval(int length) {
        this(length,Status.ABSENT);
    }

    public Interval(int length, Status defaultStatus) {
        boundaries = new TreeSet<Boundary>();
        boundaries.add(new Boundary(length, defaultStatus));
    }

    public int length() {
        return lastBoundary().location;
    }

    private Boundary lastBoundary() {
        return boundaries.last();
    }

    public Status status(int location) {
        return higher(location).status();
    }

    public boolean is(int location, Status status) {
        return status(location) == status;
    }

    private Boundary higher(int i) {
        return boundaries.higher(new Boundary(i));
    }

    private Boundary lower(int i) {
        return boundaries.lower(new Boundary(i));
    }

    private Boundary ceiling(int i) {
        return boundaries.ceiling(new Boundary(i));
    }

    private Boundary floor(int i) {
        return boundaries.floor(new Boundary(i));
    }

    private boolean hasBoundaryAt(int i) {
        return boundaries.contains(new Boundary(i));
    }

    public void set(int lo, int hi, Status status) {
        if ( 0 > lo || lo > hi || hi >= length() ) return; // sanity checks
        Boundary low = addOrFetchBelow(lo, status);
        Boundary high = addOrFetchAbove(hi+1, status);
        clearBetween(low, high);
    }

    private Boundary addOrFetchAbove(int location, Status status) {
        Boundary above = ceiling(location);
        if (above.is(status)) {
            return above;
        } else if (above.location == location) {
            Boundary next_above = boundaries.higher(above);
            if (next_above != null && next_above.is(status)) {
                boundaries.remove(above);
                return next_above;
            } else {
                above.setStatus(status);
                return above;
            }
        } else {
            return addBoundary(location, status);
        }
    }

    private Boundary addOrFetchBelow(int location, Status status) {
        if (status(location) == status) {
            return floor(location); // might be null
        } else {
            return addBoundary(location);
        }
    }

    private void clearBetween(Boundary low, Boundary high) {
        Boundary above;
        while ( (above = above(low)).compareTo(high) < 0 ) {
            boundaries.remove(above);
        }
    }

    private Boundary above(Boundary b) {
        return boundaries.higher(b);
    }

    /**
     * add a boundary at location of the same status as location's current status
     * @param location should be greater than 0
     */
    private Boundary addBoundary(int location) {
        Boundary b = new Boundary(location, status(location));
        boundaries.add(b);
        return b;
    }

    /**
     * @param location should be greater than 0
     * @param status   the {@link Status} of the new {@link Boundary}
     */
    private Boundary addBoundary(int location, Status status) {
        Boundary b = new Boundary(location, status);
        boundaries.remove(b); // make sure status is overwritten
        boundaries.add(b);
        return b;
    }

    public Tuple getAbsentInterval(int max_size) {
        for (Boundary b : boundaries) {
            if (b.is(Status.ABSENT)) {
                Boundary below = boundaries.lower(b);
                int lo;
                if (b.location - below.location > max_size) {
                    lo = b.location - max_size;
                } else {
                    lo = below.location;
                }
                return new Tuple(lo, b.location);
            }
        }
        return null;
    }

    public String toString() {
        String out = new String("<");
        for (int i = 0; i < length(); i++) {
            out += status(i).c;
        }
        return out+">";
    }

    public static void main(String[] args) {
        System.out.println("true: " + (new Boundary(30).equals(new Boundary(30, Status.PENDING))));
        Interval i = new Interval(100);
        System.out.println(i);
        i.set(40,50, Status.PRESENT);
        System.out.println(i);
        i.set(30,60, Status.PRESENT);
        System.out.println(i);
        i.set(30,70, Status.PRESENT);
        System.out.println(i);
        i.set(40,80, Status.PRESENT);
        System.out.println(i);
        System.out.println("30: " + i.is(30, Status.PRESENT));
        System.out.println("70: " + i.is(70, Status.PRESENT));
        System.out.println("80: " + i.is(80, Status.PRESENT));
        i.set(40,60, Status.PRESENT);
        System.out.println(i);
        System.out.println("//////////////////////");
        i = new Interval(100);
        i.set(10, 60, Status.PRESENT);
        i.set(15, 60, Status.PENDING);
        i.set(40, 60, Status.ABSENT);
        System.out.println(i);
        i.set(0, 99, Status.PRESENT);
        System.out.println(i);
    }
}

class Boundary implements Comparable<Boundary> {
    public final int location;
    private final AtomicReference<Status> status;

    public Boundary(int location) {
        this(location, Status.ABSENT);
    }

    public Boundary(int location, Status status) {
        this.location = location;
        this.status = new AtomicReference<Status>(status);
    }

    public Status status() {
        return status.get();
    }

    public void setStatus(Status newStatus) {
        status.set(newStatus);
    }

    public boolean is(Status status) {
        return this.status() == status;
    }

    public int compareTo(Boundary other) {
        return this.location - other.location;
    }

    public boolean equals(Boundary other) {
        return this.location == other.location;
    }

    public int hashCode() {
        return location;
    }

    public boolean strictEquals(Boundary other) {
        return this.location == other.location && this.status == other.status;
    }

    public String toString() {
        return "|" + location + "|";
    }
}

enum Status {
    ABSENT('_'),
    PRESENT('*'),
    PENDING('?');

    public final char c;
    public String toString() { return ""+c;}

    Status(char c) {
        this.c = c;
    }
}
