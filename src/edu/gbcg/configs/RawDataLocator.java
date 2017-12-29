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
        return FileUtils.get().getAllFilePathsInDirWithPrefix("RS", getRedditDataPath());
    }

    /**
     * Get a list of all raw json files for reddit comment data. This path changes depending on
     * which machine the code is running on.
     * @return List of all reddit comment files if available, otherwise null
     */
    public static List<String> redditJsonCommentAbsolutePaths(){
        return FileUtils.get().getAllFilePathsInDirWithPrefix("RC", getRedditDataPath());
    }

    /*
        ** NO JAVADOC **
        * Returns the relative path to the json files.
    */
    private static String getRedditDataPath(){
        // Testing mode means we're running on my MBP with limited data
        return StateVars.TESTING_MODE ? DataPaths.LOCAL_DATA_PATH : DataPaths.DATA_PATH;
    }
}
