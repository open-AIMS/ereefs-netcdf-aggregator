package aims.ereefs.netcdf.util;

import java.io.File;

/**
 * Utilities for manipulating files and paths.
 *
 * @author Aaron Smith
 */
public class FileUtils {

    /**
     * Delete the directory specified by {@code pathname}, along with all child directories and any
     * files.
     */
    static public void delete(String pathname) {
        if (pathname != null) {
            delete(new File(pathname));
        }
    }

    /**
     * Delete the directory , along with all child directories and any files.
     */
    static public void delete(File path) {
        if (path.isDirectory()) {
            for (final File file : path.listFiles()) {
                delete(file);
            }
        }
        path.delete();
    }
}
