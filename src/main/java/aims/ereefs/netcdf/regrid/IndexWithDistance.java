package aims.ereefs.netcdf.regrid;

import java.io.Serializable;

/**
 * Created by gcoleman on 6/09/2016.
 * The index refers to the index of the curved grid
 * the distance is the distance from the point corresponding to that index to another point (not specified in this class)
 * A list of these as stored along with a point from a a regular grid
 */
public class IndexWithDistance implements Comparable<IndexWithDistance>, Serializable{

    /**
     * Constant value to use as the weight {see {@link #getWeight()}} when {@link #distance} is
     * zero.
     */
    private static final double INFINITE_WEIGHT = 999.0;

    private final Integer index;
    private final Double distance;

    public IndexWithDistance(Integer index, Double distance) {
        this.index = index;
        this.distance = distance;
    }

    public Integer getIndex() {
        return this.index;
    }

    public Double getDistance() {
        return this.distance;
    }

    public Double getWeight() {
        if (this.distance == 0.0) {
            return IndexWithDistance.INFINITE_WEIGHT;
        } else {
            return 1 / this.distance;
        }
    }

    @Override
    public int compareTo(IndexWithDistance other) {
        return distance.compareTo(other.distance);
    }
}
