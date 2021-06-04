package aims.ereefs.netcdf.output.netcdf;

import io.prometheus.client.Gauge;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.nc2.*;

import java.io.IOException;
import java.util.List;

/**
 * A simplifying layer around the output Netcdf data file.
 *
 * @see #writer
 *
 * @author Aaron Smith
 */
public class OutputDataset {

    protected static final Gauge apmDatasetWriteBytes = Gauge.build()
        .name("ncaggregate_dataset_write_bytes")
        .help("Total size (bytes) of data written to a NetCDF dataset.")
        .register();
    protected static final Gauge apmDatasetWriteDuration = Gauge.build()
        .name("ncaggregate_dataset_write_duration_seconds")
        .help("Time taken (seconds) to write data to a NetCDF dataset.")
        .register();


    /**
     * Cached reference to the root <code>Group</code>.
     */
    protected Group rootGroup;

    /**
     * Cached reference to the underlying <code>NetcdfFileWriter</code>.
     */
    protected NetcdfFileWriter writer;

    /**
     * A cache of information relating to the output dataset.
     */
    protected OutputDatasetInfo outputDatasetInfo = new OutputDatasetInfo();
    public OutputDatasetInfo getOutputDatasetInfo() {
        return this.outputDatasetInfo;
    }

    /**
     * Constructor.
     */
    public OutputDataset(NetcdfFileWriter writer,
                         Group rootGroup) {
        this.writer = writer;
        this.rootGroup = rootGroup;
    }

    /**
     * Wrap the <code>addGroupAttribute</code> method of the {@link #writer}.
     *
     * @param attribute the attribute to add.
     */
    public void addGroupAttribute(Attribute attribute) {
        this.writer.addGroupAttribute(this.rootGroup, attribute);
    }

    /**
     * Wrap the <code>addUnlimitedDimension</code> method of the {@link #writer}.
     *
     * @param name the <code>name</code> of the unlimited dimension to add.
     * @return the <code>Dimension</code> created by the add.
     */
    public Dimension addUnlimitedDimension(String name) {
        return this.writer.addUnlimitedDimension(name);
    }

    /**
     * Wrap the <code>addDimension</code> method of the {@link #writer}.
     *
     * @param name the <code>name</code> of the dimension to add.
     * @return the <code>Dimension</code> created by the add.
     */
    public Dimension addDimension(String name, int length) {
        return this.writer.addDimension(this.rootGroup, name, length);
    }

    /**
     * Wrap the <code>addVariable</code> method of the {@link #writer}.
     */
    public Variable addVariable(String shortName, DataType dataType, List<Dimension> dimensions) {
        return this.writer.addVariable(this.rootGroup, shortName, dataType, dimensions);
    }

    /**
     * Wrap the <code>close</code> method of the {@link #writer}.
     */
    public void close() {
        try {
            this.writer.close();
        } catch (IOException e) {
            throw new RuntimeException("Failed to close the output dataset.", e);
        }
    }

    /**
     * Wrap the <code>flush</code> method of the {@link #writer}.
     */
    public void flush() {
        try {
            this.writer.flush();
        } catch (IOException e) {
            throw new RuntimeException("Failed to flush the output dataset.", e);
        }
    }

    /**
     * Wrap the <code>write</code> method of the {@link #writer}.
     */
    public void write(Variable variable, Array array) {
        try {
            Gauge.Timer durationTimer = apmDatasetWriteDuration.startTimer();
            this.writer.write(variable, array);
            durationTimer.setDuration();
            apmDatasetWriteBytes.inc(array.getDataType().getSize() * array.getSize());
        } catch(Throwable t) {
            new RuntimeException("Failed to write the data for variable \"" +
                variable.getShortName() + "\".", t);
        }
    }

    /**
     * Wrap the <code>write</code> method of the {@link #writer}.
     */
    public void write(Variable variable, int[] offset, Array array) {
        try {
            Gauge.Timer durationTimer = apmDatasetWriteDuration.startTimer();
            this.writer.write(variable, offset, array);
            durationTimer.setDuration();
            apmDatasetWriteBytes.inc(array.getDataType().getSize() * array.getSize());
        } catch(Throwable t) {
            new RuntimeException("Failed to write the data for variable \"" +
                variable.getShortName() + "\" at offset " + offset + ".", t);
        }
    }

}
