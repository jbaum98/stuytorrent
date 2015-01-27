import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicReference;

public class Interval {
    private TreeSet<Boundary> boundaries;

    public Interval(int size) {
        boundaries = new TreeSet<Boundary>();
        boundaries.add(new Boundary(size));
    }

    public int size() {
        return last().location;
    }

    private Boundary last() {
        return boundaries.last();
    }

    public Status status(int location) {
        return ceiling(location).status();
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
        if (lo == 0 || (!is(lo, status) && (!is(hi, status)) || hasBoundaryAt(hi)) ) {
            addBoundary(lo);
            addBoundary(hi, status);
            Boundary above;
            while ((above = higher(lo)).location < hi) {
                boundaries.remove(above);
            }
        } else if (is(lo, status)) {
            Boundary below = lower(lo);
            int new_lo = below == null ? 0 : below.location;
            set(new_lo, hi, status);
        } else if (is(hi, status)) {
            Boundary above = ceiling(hi);
            int new_hi = above.location;
            set(lo, new_hi, status);
        }
    }

    private void addBoundary(int location) {
        addBoundary(location, status(location));
    }

    private void addBoundary(int location, Status status) {
        if (location == 0) return;
        Boundary n = new Boundary(location, status);
        boundaries.remove(n);
        boundaries.add(n);
    }

    public String toString() {
        String out = new String("<");
        for (Boundary b : boundaries) {
            for (int i = 0; i < 5; i++) out += b.status().c;
            out += b;
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
        i.set(0, 100, Status.PRESENT);
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

    public void set(Status status) {
        this.status.set(status);
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
