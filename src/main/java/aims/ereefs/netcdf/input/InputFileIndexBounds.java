package aims.ereefs.netcdf.input;

import aims.ereefs.netcdf.input.netcdf.InputFileInfo;

/**
 * Defines the <code>start</code> and <code>end</code> bounds of an {@link InputFileInfo} object
 * that contributes to an {@link aims.ereefs.netcdf.outputStrategy.OutputFileInfo} instance.
 *
 * @author Aaron Smith
 */
public class InputFileIndexBounds {

    /**
     * Reference to the <code>InputFileInfo</code> object being bound.
     */
    protected InputFileInfo inputFileInfo;
    public InputFileInfo getInputFileInfo() {
        return this.inputFileInfo;
    }

    /**
     * Zero-based <code>start</code> index for the data of interest for the associated
     * {@link InputFileInfo} object.
     */
    protected int startIndex = 0;
    public int getStartIndex() {
        return this.startIndex;
    }

    /**
     * Zero-based <code>end</code> index for the data of interest for the associated
     * {@link InputFileInfo} object.
     */
    public int endIndex = 0;
    public int getEndIndex() {
        return this.endIndex;
    }

    public InputFileIndexBounds(InputFileInfo inputFileInfo, int startIndex, int endIndex) {
        super();
        this.inputFileInfo = inputFileInfo;
        this.startIndex = startIndex;
        this.endIndex = endIndex;
    }

}
