/*
 * Copyright (c) 2018 Sean Grimes. All Rights Reserved.
 * License: MIT
 */

package edu.antevortadb.configs

import javalibs.FileUtils
import javalibs.Logic

/**
 * Intended to locate the json files for database builds. The location of these files can
 * and will be different depending on which machine this code is running on. The idea here
 * is to abstract that away from the end user and library classes. Function calls here
 * will return the proper path based on which machine the code is running on and whether
 * or not testing mode has been enabled.
 */
object RawDataLocator{
    private val futils = FileUtils.get()

    /**
     * Returns the path to the server config file
     */
    fun serverConfigFile(): String {
        return when(Finals.TESTING_MODE){
            true -> DataPaths.LOCAL_SERVER_CONFIG_FILE
            false -> DataPaths.SERVER_CONFIG_FILE
        }
    }

    /**
     * Returns the path to the client config file
     */
    fun clientConfigFile(): String {
        return when(Finals.TESTING_MODE){
            true -> DataPaths.LOCAL_CLIENT_CONFIG_FILE
            false -> DataPaths.CLIENT_CONFIG_FILE
        }
    }

    /**
     * Get the path to the Word2Vec data storage location
     */
    fun word2VecPath(): String {
        return when(Finals.TESTING_MODE){
            true -> DataPaths.LOCAL_W2VEC_PATH
            false -> DataPaths.W2VEC_PATH
        }
    }

    /**
     * Get the path to the EESI data storage location
     */
    fun EESIPath(): String {
        return when(Finals.TESTING_MODE){
            true -> DataPaths.LOCAL_EESI_PATH
            false -> DataPaths.EESI_PATH
        }
    }

    /**
     * Get the path to the stats data storage location
     */
    fun STATSPath(): String {
        return when(Finals.TESTING_MODE){
            true -> DataPaths.LOCAL_STATS_PATH
            false -> DataPaths.STATS_PATH
        }
    }

    /**
     * Returns the path to the server config directory
     */
    fun serverConfigPath(): String {
        return when(Finals.TESTING_MODE){
            true -> DataPaths.LOCAL_SERVER_CONFIG_PATH
            false -> DataPaths.SERVER_CONFIG_PATH
        }
    }

    /**
     * Get a list of all raw json files for reddit submission data. This path changes
     * depending on which machine the code is running on.
     * @return List of all reddit submission files if available, otherwise null
     */
    fun redditJsonSubmissionAbsolutePaths(): List<String> {
        val path = if(Finals.TESTING_MODE)
            DataPaths.LOCAL_REDDIT_SUB_DATA
        else
            DataPaths.REDDIT_SUB_DATA_PATH
        return futils.getAllFilePathsInDirWithPrefix("RS", path)
    }

    /**
     * Get a list of all raw json files for reddit comment data. This path changes
     * depending on which machine the code is running on.
     * @return List of all reddit comment files if available, otherwise null
     */
    fun redditJsonCommentAbsolutePaths(): List<String> {
        val path = if(Finals.TESTING_MODE)
            DataPaths.LOCAL_REDDIT_COM_DATA
        else
            DataPaths.REDDIT_COM_DATA_PATH
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

    /**
     * @return Root to the DL4J example data
     */
    fun dl4jDataRoot(): String {
        if(DataPaths.CPUTESTINGMODE){
            return DataPaths.DL4J_CPU_ISSUE_ROOT
        }
        return when(Finals.TESTING_MODE) {
            true -> DataPaths.LOCAL_DL4J_ROOT
            false -> DataPaths.RESEARCH_DL4J_ROOT
        }
    }

    /**
     * @return Root of the disk area hollywood agents can make / modify files
     */
    fun HWAgentSpace(): String {
        Logic.get().require(Finals.TESTING_MODE, "Is agent space correct on array???")
        return when(Finals.TESTING_MODE) {
            true -> DataPaths.HW_LOCAL_AGENT_ROOT
            false -> DataPaths.HW_RESEARCH_AGENT_ROOT
        }
    }

    fun BCAgentSpace(): String {
        return when(Finals.TESTING_MODE) {
            true -> DataPaths.BC_LOCAL_AGENT_ROOT
            //false -> DataPaths.BC_RESEARCH_AGENT_ROOT
            false -> "agentTMPDIR/"
        }
    }

    fun ONCOAgentSpace(): String {
        return when(Finals.TESTING_MODE) {
            true -> DataPaths.ONCO_LOCAL_AGENT_ROOT
            false -> DataPaths.ONCO_RESEARCH_AGENT_ROOT
        }
    }

    fun airlineAgentSpace(): String {
        return when(Finals.TESTING_MODE) {
            true -> DataPaths.AIRLINE_LOCAL_AGENT_ROOT
            false -> DataPaths.AIRLINE_RESEARCH_AGENT_ROOT
        }
    }

    /**
     * @return Path to the data directory for the breast cancer files
     */
    fun bcDirPath(): String {
        return when(Finals.TESTING_MODE) {
            true -> DataPaths.LOCAL_BREAST_CANCER_PATH
            false -> DataPaths.BCancerPath
        }
    }

    /**
     * @return Mark Zarella's original CSV
     * NOTE: This call is generally just used for cleaning up the CSV to get it into
     * working order
     */
    fun bcOriginalCSVAbsolutePath(): String {
        return when(Finals.TESTING_MODE) {
            true -> DataPaths.LOCAL_BC_ORIG_FILE
            false -> DataPaths.BC_ORIG_FILE
        }
    }

    /**
     * @return The cleaned breast cancer CSV, after organizing the stains and dealing
     * with the mitotic score / count issue
     */
    fun bcCSVAbsolutePath(): String {
        return when(Finals.TESTING_MODE) {
            true -> DataPaths.LOCAL_BC_CLEANED_FILE
            false -> DataPaths.BC_CLEANED_FILE
        }
    }

    /* ----- The below deal with getting paths to new data to add to *EXISTING* DBs --- */
    /**
     * Some data is added once without update, however the function to gather new data
     * still needs to be implemented. The below function exists as a fake-out for that
     * limitation in class structure
     */
    fun getEmptyArrayList() = ArrayList<String>()

    /**
     * Get a list of all raw json files for reddit submission data that needs to be added
     * to an existing database. This path changes depending on while machine the code is
     * running on.
     * @return List of all reddit submission files for NEW json data, otherwise null
     */
    fun redditJsonSubmissionAbsolutePathsNewData(): List<String> {
        val path = if(Finals.TESTING_MODE) DataPaths.LOCAL_NEW_REDDIT_SUB_DATA
                    else DataPaths.NEW_REDDIT_SUB_DATA_PATH
        return futils.getAllFilePathsInDirWithPrefix("RS", path)
    }

    /**
     * Get a list of all raw json files for reddit comment data that needs to be added to
     * an existing database. This path changes depending on while machine the code is
     * running on.
     * @return List of all reddit comment files for NEW json data, otherwise null
     */
    fun redditJsonCommentAbsolutePathsNewData(): List<String> {
        val path = if(Finals.TESTING_MODE) DataPaths.LOCAL_NEW_REDDIT_COM_DATA
                    else DataPaths.NEW_REDDIT_COM_DATA_PATH
        return futils.getAllFilePathsInDirWithPrefix("RC", path)
    }
}
