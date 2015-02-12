import java.util.TreeSet;

/**
 * represents a list of length locations from 0 to length-1,
 * where each location has a status.
 * Avoids storing data on each location by instead
 * storing a {@link Set} of {@link Boundary}>s where
 * the status of a given location is determined by the
 * first {@link Boundary} strictly above that location.
 */

public class Interval {
    private TreeSet<Boundary> boundaries;

    public Interval(int length) {
        this(length,Status.ABSENT);
    }

    /**
     * creates a {@link Boundary} at length
     * because the highest boundary determines the length
     */
    public Interval(int length, Status defaultStatus) {
        boundaries = new TreeSet<Boundary>();
        boundaries.add(new Boundary(length, defaultStatus));
    }

    public int length() {
        return boundaries.last().location;
    }

    /** {@see TreeSet#higher} */
    private Boundary higher(int i) {
        return boundaries.higher(new Boundary(i));
    }

    /** {@see TreeSet#ceiling} */
    private Boundary ceiling(int i) {
        return boundaries.ceiling(new Boundary(i));
    }

    /** {@see TreeSet#floor} */
    private Boundary floor(int i) {
        return boundaries.floor(new Boundary(i));
    }

    public Status statusAt(int location) {
        return higher(location).status();
    }

    /**
     * Set all locations in the interval [lo, hi) to status.
     * Similar to slice notation in that hi is not included,
     * and that the maximum value for hi is length even though
     * the highest location is length-1
     */
    public void set(int lo, int hi, Status status) {
        if ( 0 > lo || lo > hi || hi >= length() ) return; // sanity checks
        Boundary low = addOrFetchBelow(lo, status);
        Boundary high = addOrFetchAbove(hi+1, status);
        clearBetween(low, high);
    }

    /**
     * Creates a boundary with status at location,
     * unless that location is already of the correct status
     * in which case we return the higher boundary of the same status.
     * @param location location of the boundary
     * @param status   status of the boundary
     * @return a Boundary at or higher than location with status
     */
    private Boundary addOrFetchAbove(int location, Status status) {
        Boundary above = ceiling(location);
        if (above.is(status)) {
            return above;
        } else if (above.location == location) {                // there exists a boundary at location of incorrect status
            Boundary next_above = boundaries.higher(above);
            if (next_above != null && next_above.is(status)) {  // if there is a correct and higher boundary
                return next_above;                              // above will be deleted later in clearBetween
            } else {                                            // there is no higher correct boundary
                above.setStatus(status);                        // modify existing boundary
                return above;
            }
        } else {
            return addBoundary(location, status);
        }
    }

    /**
     * creates a boundary at location, preserving it's status
     * unless that location is already of the new status,
     * in which case we return the next lowest boundary.
     * @param location location of the boundary
     * @param status   status we want to set the locations above this
     * @return a Boundary at or lower than location
     */
    private Boundary addOrFetchBelow(int location, Status status) {
        if (statusAt(location) == status) {
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
     * @param location should be greater than 0
     * @param status   the {@link Status} of the new {@link Boundary}
     */
    private Boundary addBoundary(int location, Status status) {
        Boundary b = new Boundary(location, status);
        boundaries.remove(b); // make sure status is overwritten
        boundaries.add(b);
        return b;
    }

    /**
     * add a boundary at location of the same status as location's current status
     * @param location should be greater than 0
     */
    private Boundary addBoundary(int location) {
        Boundary b = new Boundary(location, statusAt(location));
        boundaries.add(b);
        return b;
    }

    /*public Tuple getAbsentInterval(int max_size) {
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
    }*/

    public String toString() {
        String out = new String("<");
        for (int i = 0; i < length(); i++) {
            out += statusAt(i);
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

/**
 * represents a boundary such that all locations below the boundary
 * have a status set by the boundary
 */

class Boundary implements Comparable<Boundary> {
    public final int location;
    private Status status;

    public Boundary(int location) {
        this(location, Status.ABSENT);
    }

    public Boundary(int location, Status status) {
        this.location = location;
        this.status = status;
    }

    public Status status() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public boolean is(Status status) {
        return this.status() == status;
    }

    /**
     * Comparison depends only on location
     * and not on status, so that {@link Interval#boundaries}
     * orders correctly
     */
    public int compareTo(Boundary other) {
        return this.location - other.location;
    }

    /**
     * Equality depends only on location
     * and not on status, so that {@link Interval#boundaries}
     * orders correctly
     */
    public boolean equals(Boundary other) {
        return this.location == other.location;
    }

    /**
     * Equality depends only on location
     * and not on status, so that {@link Interval#boundaries}
     * orders correctly
     */
    public int hashCode() {
        return location;
    }

    public String toString() {
        return "|" + location + "|";
    }
}

/**
 * represents the status of a location
 */

enum Status {
    ABSENT('_'),
    PRESENT('*'),
    PENDING('?');

    /** the character used to represent this status */
    public final char c;
    public String toString() { return ""+c;}

    Status(char c) {
        this.c = c;
    }
}
