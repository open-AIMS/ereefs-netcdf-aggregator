package aims.ereefs.netcdf.input.extraction;

import aims.ereefs.netcdf.regrid.IndexWithDistance;

import java.util.List;

/**
 * A {@code POJO} containing information for a single extraction site.
 *
 * @author Aaron Smith
 */
public class ExtractionSite {

    protected String id;

    public String getId() {
        return this.id;
    }

    protected String siteName;

    public String getSiteName() {
        return this.siteName;
    }

    protected double latitude;

    public double getLatitude() {
        return this.latitude;
    }

    protected double longitude;

    public double getLongitude() {
        return this.longitude;
    }

    protected List<IndexWithDistance> neighbours;

    public List<IndexWithDistance> getNeighbours() {
        return this.neighbours;
    }

    public ExtractionSite(String id,
                          String siteName,
                          double latitude,
                          double longitude,
                          List<IndexWithDistance> neighbours) {
        this.id = id;
        this.siteName = siteName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.neighbours = neighbours;
    }

}
