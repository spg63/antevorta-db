package edu.gbcg.configs;

import edu.gbcg.utils.FileUtils;

import java.util.List;

/**
 * Intended to locate the json files for database builds. The location of these files can and
 * will be different depending on which machine this code is running on. The idea here is to
 * abstract that away from the end user and library classes. Function calls here will return the
 * proper path based on which machine the code is running on and whether or not testing mode has
 * been enabled.
 */
public class RawDataLocator {
    /**
     * Get a list of all raw json files for reddit submission data. This path changes depending
     * on which machine the code is running on.
     * @return List of all reddit submission files if available, otherwise null
     */
    public static List<String> redditJsonSubmissionAbsolutePaths(){
        String path = StateVars.TESTING_MODE ? DataPaths.LOCAL_SUB_DATA_PATH : DataPaths.SUB_DATA_PATH;
        return FileUtils.get().getAllFilePathsInDirWithPrefix("RS", path);
    }

    /**
     * Get a list of all raw json files for reddit comment data. This path changes depending on
     * which machine the code is running on.
     * @return List of all reddit comment files if available, otherwise null
     */
    public static List<String> redditJsonCommentAbsolutePaths(){
        String path = StateVars.TESTING_MODE ? DataPaths.LOCAL_COM_DATA_PATH : DataPaths.COM_DATA_PATH;
        return FileUtils.get().getAllFilePathsInDirWithPrefix("RC", path);
    }
}
