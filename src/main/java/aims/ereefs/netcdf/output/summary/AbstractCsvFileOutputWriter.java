package aims.ereefs.netcdf.output.summary;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * An abstract base implementation of {@link OutputWriter} for writing to a CSV file.
 *
 * @author Aaron Smith
 */
abstract public class AbstractCsvFileOutputWriter implements OutputWriter {

    protected FileWriter fileWriter;
    protected Map<String, String[]> idToDetailsMap = new HashMap<>();

    /**
     * Constructor to cache the properties to use for all writes to the single {@code OutputFile}.
     *
     * @param outputFile the file to write to, which will be wrapped by {@link #fileWriter}.
     * @throws IOException thrown if the <code>outputFile</code> cannot be wrapped by the
     *                     {@link #fileWriter}.
     */
    public AbstractCsvFileOutputWriter(File outputFile) throws IOException {

        // Instantiate the writer for subclasses to use.
        this.fileWriter = new FileWriter(outputFile);
    }

    @Override
    public void close() throws IOException {
        this.fileWriter.close();
    }

    @Override
    public void flush() throws IOException {
        this.fileWriter.flush();
    }

}
