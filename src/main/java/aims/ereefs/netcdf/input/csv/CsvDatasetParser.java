package aims.ereefs.netcdf.input.csv;

import aims.ereefs.netcdf.input.SimpleDataset;
import aims.ereefs.netcdf.util.file.ReadUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Parse a {@code CSV} file to instantiate a {@link SimpleDataset} object.
 *
 * @author Aaron Smith
 */
public class CsvDatasetParser {

    static public SimpleDataset parse(File file,
                                      String[] fieldNames) {

        // Download successful, to parse the file.
        try {
            List<String> lines = ReadUtils.readTextFileAsList(file);
            List<String[]> data = new ArrayList<>();
            for (String line : lines) {
                String[] fields = line.split(",");

                // Remove starting/ending quotes.
                for (int fieldIndex = 0; fieldIndex < fields.length; fieldIndex++) {
                    String field = fields[fieldIndex];
                    if (field.startsWith("\"")) {
                        field = field.substring(1);
                    }
                    if (field.endsWith("\"")) {
                        field = field.substring(0, field.length() - 1);
                    }
                    fields[fieldIndex] = field;
                }
                data.add(fields);
            }
            // Ensure the number of fields is consistent.
            int fieldCount = fieldNames.length;
            Optional<String[]> any = data.parallelStream().filter(row -> row.length != fieldCount).findAny();
            if (any.isPresent()) {
                throw new RuntimeException("One or more rows do not match expected field count. " +
                    "Expected " + fieldCount + "; Found " + any.get().length + "; filename: " +
                    file.getName() + ".");
            }

            return new SimpleDataset(fieldNames, data);

        } catch (Throwable throwable) {
            throw new RuntimeException("Failed to parse the CSV file.", throwable);
        }

    }

}
