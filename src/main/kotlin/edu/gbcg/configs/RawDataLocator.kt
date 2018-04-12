/*
 * Copyright (c) 2018 Sean Grimes. All Rights Reserved.
 * License: MIT
 */

package edu.gbcg.configs

import edu.gbcg.utils.FileUtils

/**
 * Intended to locate the json files for database builds. The location of these files can and
 * will be different depending on which machine this code is running on. The idea here is to
 * abstract that away from the end user and library classes. Function calls here will return the
 * proper path based on which machine the code is running on and whether or not testing mode has
 * been enabled.
 */
object RawDataLocator{

    /**
     * Get a list of all raw json files for reddit submission data. This path changes depending
     * on which machine the code is running on.
     * @return List of all reddit submission files if available, otherwise null
     */
    @JvmStatic fun redditJsonSubmissionAbsolutePaths(): List<String> {
        val path = if(Finals.TESTING_MODE) DataPaths.LOCAL_SUB_DATA_PATH else DataPaths.SUB_DATA_PATH
        return FileUtils.get().getAllFilePathsInDirWithPrefix("RS", path)
    }

    /**
     * Get a list of all raw json files for reddit comment data. This path changes depending on
     * which machine the code is running on.
     * @return List of all reddit comment files if available, otherwise null
     */
    @JvmStatic fun redditJsonCommentAbsolutePaths(): List<String> {
        val path = if(Finals.TESTING_MODE) DataPaths.LOCAL_COM_DATA_PATH else DataPaths.COM_DATA_PATH
        return FileUtils.get().getAllFilePathsInDirWithPrefix("RC", path)
    }

    /**
     * Get a list of all raw json files for reddit submission data that needs to be added to an existing database.
     * This path changes depending on while machine the code is running on.
     * @return List of all reddit submission files for NEW json data, otherwise null
     */
    @JvmStatic fun redditJsonSubmissionAbsolutePathsNewData(): List<String> {
        val path = if(Finals.TESTING_MODE) DataPaths.LOCAL_NEW_SUB_DATA_PATH else DataPaths.NEW_SUB_DATA_PATH
        return FileUtils.get().getAllFilePathsInDirWithPrefix("RS", path)
    }

    /**
     * Get a list of all raw json files for reddit comment data that needs to be added to an existing database.
     * This path changes depending on while machine the code is running on.
     * @return List of all reddit comment files for NEW json data, otherwise null
     */
    @JvmStatic fun redditJsonCommentAbsolutePathsNewData(): List<String> {
        val path = if(Finals.TESTING_MODE) DataPaths.LOCAL_NEW_COM_DATA_PATH else DataPaths.NEW_COM_DATA_PATH
        return FileUtils.get().getAllFilePathsInDirWithPrefix("RC", path)
    }
}




