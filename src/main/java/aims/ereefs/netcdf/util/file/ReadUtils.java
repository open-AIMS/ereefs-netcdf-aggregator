package aims.ereefs.netcdf.util.file;

import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

/**
 * Utilities related to reading data from a text file.
 *
 * @author Aaron Smith
 */
public class ReadUtils {

    /**
     * Read an entire text file at once, returning the contents as a <code>String</code>.
     *
     * @param filename the full name of the file to read.
     * @return a <code>String</code> representing the contents of the file.
     */
    static public String readTextFileAsString(String filename) throws IOException {
        return readTextFileAsString(new File(filename));
    }

    /**
     * Read an entire text file at once, returning the contents as a <code>String</code>.
     *
     * @param file the file to read.
     * @return a <code>String</code> representing the contents of the file.
     */
    static public String readTextFileAsString(File file) throws IOException {
        StringBuilder sb = new StringBuilder();
        for (String line : readTextFileAsList(file)) {
            sb.append(line).append("\n");
        }
        return sb.toString();
    }

    /**
     * Read an entire text file at once, returning the contents as a list of <code>String</code>s.
     *
     * @param filename the full name of the file to read.
     * @return a list of <code>String</code> objects, where each <code>String</code> represents a
     * row/line in the file.
     */
    static public List<String> readTextFileAsList(String filename) throws IOException {
        return readTextFileAsList(new File(filename));
    }

    /**
     * Read an entire text file at once, returning the contents as a list of <code>String</code>s.
     *
     * @param file the file to read.
     * @return a list of <code>String</code> objects, where each <code>String</code> represents a
     * row/line in the file.
     */
    static public List<String> readTextFileAsList(File file) throws IOException {
        List<String> lines = new ArrayList<>();

        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(file)))) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                lines.add(line);
            }
        }
        return lines;
    }

    static public String generateMd5Checksum(String filename)
        throws IOException, NoSuchAlgorithmException {

        MessageDigest md = MessageDigest.getInstance("MD5");
        FileInputStream fis = new FileInputStream(filename);

        byte[] dataBytes = new byte[1024];

        int nread = 0;
        while ((nread = fis.read(dataBytes)) != -1) {
            md.update(dataBytes, 0, nread);
        };
        byte[] mdbytes = md.digest();
        return DatatypeConverter.printHexBinary(mdbytes).toUpperCase();
    }

}
