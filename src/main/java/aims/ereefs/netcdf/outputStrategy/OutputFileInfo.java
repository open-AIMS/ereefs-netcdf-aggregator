package aims.ereefs.netcdf.outputStrategy;

import java.io.File;

/**
 * Created by gcoleman on 8/11/2016.
 */
public class OutputFileInfo {

    private final String directory;
    private final String outputFilename;

    private File outputFile;
    private File tempFile;

    /**
     * Constructor.
     */
    public OutputFileInfo(String directory, String outputFilename) {
        // TODO: Refactor this out.
        this.directory = directory;
        this.outputFilename = outputFilename;
    }

    public String getOutputFilename() {
        return outputFilename;
    }


    @Override
    public String toString() {
        return "OutputFileInfo{outputFilename='" + outputFilename + "'}";
    }

    public File getOutputFile() {
        return outputFile;
    }


    public File getTempFile() {
        return tempFile;
    }


    public void initFiles() {
        File outputDirectory = new File(this.directory);
        outputFile = new File(outputDirectory, getOutputFilename());
        File tempDirectory = new File(outputDirectory, "temp");
        tempDirectory.mkdirs();
        tempFile = new File(tempDirectory, getOutputFilename());

    }
}
