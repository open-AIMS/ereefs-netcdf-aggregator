package aims.ereefs.netcdf.regrid;

/**
 * A value object encapsulating a specific coordinate ({@code latitude}/{@code longitude}
 * combination).
 *
 * @author Greg Coleman
 */
public class Coordinate {
    private final double latitude;
    private final double longitude;

    public Coordinate(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof Coordinate) && (((Coordinate) obj).getLatitude() == this.latitude)
            && (((Coordinate) obj).getLongitude() == this.longitude);
    }

    @Override
    public String toString() {
        return "Coordinate{" +
            "latitude=" + latitude +
            ", longitude=" + longitude +
            '}';
    }
}
