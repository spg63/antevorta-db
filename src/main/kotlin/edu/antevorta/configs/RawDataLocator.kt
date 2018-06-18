/*
 * Copyright (c) 2018 Sean Grimes. All Rights Reserved.
 * License: MIT
 */

package edu.antevorta.configs

import edu.antevorta.utils.FileUtils
import javax.xml.crypto.Data

/**
 * Intended to locate the json files for database builds. The location of these files can and
 * will be different depending on which machine this code is running on. The idea here is to
 * abstract that away from the end user and library classes. Function calls here will return the
 * proper path based on which machine the code is running on and whether or not testing mode has
 * been enabled.
 */
object RawDataLocator{
    private val futils = FileUtils.get()

    /**
     * Get a list of all raw json files for reddit submission data. This path changes depending
     * on which machine the code is running on.
     * @return List of all reddit submission files if available, otherwise null
     */
    fun redditJsonSubmissionAbsolutePaths(): List<String> {
        val path = if(Finals.TESTING_MODE) DataPaths.LOCAL_REDDIT_SUB_DATA else DataPaths.REDDIT_SUB_DATA_PATH
        return futils.getAllFilePathsInDirWithPrefix("RS", path)
    }

    /**
     * Get a list of all raw json files for reddit comment data. This path changes depending on
     * which machine the code is running on.
     * @return List of all reddit comment files if available, otherwise null
     */
    fun redditJsonCommentAbsolutePaths(): List<String> {
        val path = if(Finals.TESTING_MODE) DataPaths.LOCAL_REDDIT_COM_DATA else DataPaths.REDDIT_COM_DATA_PATH
        return futils.getAllFilePathsInDirWithPrefix("RC", path)
    }

    /**
     * @return TMDB credits csv file
     */
    fun tmdbCreditsCSVAbsolutePath(): String {
        return when(Finals.TESTING_MODE) {
            true -> DataPaths.LOCAL_TMDB_CREDITS
            false -> DataPaths.TMDB_CREDITS
        }
    }

    /**
     * @return TMDB movies csv file
     */
    fun tmdbMoviesCSVAbsolutePath(): String {
        return when(Finals.TESTING_MODE) {
            true -> DataPaths.LOCAL_TMDB_MOVIES
            false -> DataPaths.TMDB_MOVIES
        }
    }

    /**
     * @return movielens gnome scores csv
     */
    fun movielensGenomeScoresAbsolutePath(): String {
        return when(Finals.TESTING_MODE) {
            true -> DataPaths.LOCAL_LENS_GENOME_SCORES
            false -> DataPaths.MOVIELENS_GENOME_SCORES
        }
    }

    /**
     * @return movielens genome tags csv
     */
    fun movielensGenomeTagsAbsolutePath(): String {
        return when(Finals.TESTING_MODE) {
            true -> DataPaths.LOCAL_LENS_GENOME_TAGS
            false -> DataPaths.MOVIELENS_GENOME_TAGS
        }
    }

    /**
     * @return movielens link csv
     */
    fun movielensLinkAbsolutePath(): String {
        return when(Finals.TESTING_MODE) {
            true -> DataPaths.LOCAL_LENS_LINK
            false -> DataPaths.MOVIELENS_LINK
        }
    }

    /**
     * @return movielens movie csv
     */
    fun movielensMovieAbsolutePath(): String {
        return when(Finals.TESTING_MODE) {
            true -> DataPaths.LOCAL_LENS_MOVIE
            false -> DataPaths.MOVIELENS_MOVIE
        }
    }

    /**
     * @return movielens rating csv
     */
    fun movielensRatingAbsolutePath(): String {
        return when(Finals.TESTING_MODE) {
            true -> DataPaths.LOCAL_LENS_RATING
            false -> DataPaths.MOVIELENS_RATING
        }
    }

    /**
     * @return movielens tags csv
     */
    fun movielensTagsAbsolutePath(): String {
        return when(Finals.TESTING_MODE) {
            true -> DataPaths.LOCAL_LENS_TAG
            false -> DataPaths.MOVIELENS_TAG
        }
    }

    /* ----- The below deal with getting paths to new data to add to *EXISTING* DBs --------------------------------- */
    /**
     * Some data is added once without update, however the function to gather new data still needs to be implemented.
     * The below function exists as a fake-out for that limitation in class structure
     */
    fun getEmptyArrayList() = ArrayList<String>()

    /**
     * Get a list of all raw json files for reddit submission data that needs to be added to an existing database.
     * This path changes depending on while machine the code is running on.
     * @return List of all reddit submission files for NEW json data, otherwise null
     */
    fun redditJsonSubmissionAbsolutePathsNewData(): List<String> {
        val path = if(Finals.TESTING_MODE) DataPaths.LOCAL_NEW_REDDIT_SUB_DATA else DataPaths.NEW_REDDIT_SUB_DATA_PATH
        return futils.getAllFilePathsInDirWithPrefix("RS", path)
    }

    /**
     * Get a list of all raw json files for reddit comment data that needs to be added to an existing database.
     * This path changes depending on while machine the code is running on.
     * @return List of all reddit comment files for NEW json data, otherwise null
     */
    fun redditJsonCommentAbsolutePathsNewData(): List<String> {
        val path = if(Finals.TESTING_MODE) DataPaths.LOCAL_NEW_REDDIT_COM_DATA else DataPaths.NEW_REDDIT_COM_DATA_PATH
        return futils.getAllFilePathsInDirWithPrefix("RC", path)
    }
}
































