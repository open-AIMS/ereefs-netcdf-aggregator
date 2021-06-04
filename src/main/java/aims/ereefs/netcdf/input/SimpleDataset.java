package aims.ereefs.netcdf.input;

import java.util.List;

/**
 * A value object representing a simple dataset, such as could be represented by a CSV file.
 *
 * @author Aaron Smith
 */
public final class SimpleDataset {

    protected String[] fieldNames;

    public String[] getFieldNames() {
        return this.fieldNames;
    }

    /**
     * The data of the {@link SimpleDataset}, implemented as a {@code List} of {@code String}
     * arrays. Each entry in the {@code String} array represents the
     * value for the corresponding field from {@link #fieldNames}. Each {@code String} array
     * represents a record/row in the dataset.
     */
    protected List<String[]> data;

    public List<String[]> getData() {
        return this.data;
    }

    public SimpleDataset(String[] fieldNames,
                         List<String[]> data) {

        this.fieldNames = fieldNames;
        this.data = data;
    }

}