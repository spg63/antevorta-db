/*
 * Copyright (c) 2018 Sean Grimes. All Rights Reserved.
 * License: MIT
 */

@file:Suppress("MemberVisibilityCanBePrivate")

package edu.antevortadb.configs

import java.io.File

/**
 * The different paths to the DB, Json, and CSV data depending on which machine is running the
 * code and whether or not testing_mode has been enabled
 */
object DataPaths{
    /* ---------- The path to the local data folders ------------------------------------------------------ */
    const val LOCAL_DATA_ROOT_PATH = "/Users/hades/Git/_DATA_/"

    /* ---------- File paths when running in 'TESTING_MODE' (i.e. on my MBP with limited data are LOCAL) -- */
    const val LOCAL_PATH = "${LOCAL_DATA_ROOT_PATH}LocalData/raw/"
    const val LOCAL_REDDIT_SUB_DATA     = "${LOCAL_PATH}submissions/"
    const val LOCAL_REDDIT_COM_DATA     = "${LOCAL_PATH}comments/"
    const val LOCAL_TMDB_CREDITS        = "${LOCAL_PATH}tmdb/credits.csv"
    const val LOCAL_TMDB_MOVIES         = "${LOCAL_PATH}tmdb/movies.csv"
    const val LOCAL_LENS_GENOME_SCORES  = "${LOCAL_PATH}movielens/genome_scores.csv"
    const val LOCAL_LENS_GENOME_TAGS    = "${LOCAL_PATH}movielens/genome_tags.csv"
    const val LOCAL_LENS_LINK           = "${LOCAL_PATH}movielens/link.csv"
    const val LOCAL_LENS_MOVIE          = "${LOCAL_PATH}movielens/movie.csv"
    const val LOCAL_LENS_RATING         = "${LOCAL_PATH}movielens/rating.csv"
    const val LOCAL_LENS_TAG            = "${LOCAL_PATH}movielens/tag.csv"

    // Directory paths where new JSON data will be stored in 'TESTING_MODE'
    const val LOCAL_NEW_REDDIT_SUB_DATA = "${LOCAL_PATH}new/submissions/"
    const val LOCAL_NEW_REDDIT_COM_DATA = "${LOCAL_PATH}new/comments/"

    /* ---------- File paths when running on the research machine ----------------------------------------- */
    const val RESEARCH_PATH = "A:/Data/Uncompressed/"
    const val REDDIT_SUB_DATA_PATH      = "${RESEARCH_PATH}Reddit/Submissions/"
    const val REDDIT_COM_DATA_PATH      = "${RESEARCH_PATH}Reddit/Comments/"
    const val TMDB_CREDITS              = "${RESEARCH_PATH}tmdb/credits.csv"
    const val TMDB_MOVIES               = "${RESEARCH_PATH}tmdb/movies.csv"
    const val MOVIELENS_GENOME_SCORES   = "${RESEARCH_PATH}movielens/genome_scores.csv"
    const val MOVIELENS_GENOME_TAGS     = "${RESEARCH_PATH}movielens/genome_tags.csv"
    const val MOVIELENS_LINK            = "${RESEARCH_PATH}movielens/link.csv"
    const val MOVIELENS_MOVIE           = "${RESEARCH_PATH}movielens/movie.csv"
    const val MOVIELENS_RATING          = "${RESEARCH_PATH}movielens/rating.csv"
    const val MOVIELENS_TAG             = "${RESEARCH_PATH}movielens/tag.csv"

    // Directory paths when running on the research machine for new JSON data
    const val NEW_REDDIT_SUB_DATA_PATH  = "${RESEARCH_PATH}Reddit/New/Submissions/"
    const val NEW_REDDIT_COM_DATA_PATH  = "${RESEARCH_PATH}Reddit/New/Comments/"

    /* ---------- Path to TESTING_MODE db files ------------------------------------------------- */
    const val LOCAL_DB_PATH = "${LOCAL_DATA_ROOT_PATH}LocalDB/"
    const val LOCAL_REDDIT_SUB_DB_PATH  = "${LOCAL_DB_PATH}RedditSubs/"
    const val LOCAL_REDDIT_COM_DB_PATH  = "${LOCAL_DB_PATH}RedditComs/"
    const val LOCAL_HOLLYWOOD_DB_PATH   = "${LOCAL_DB_PATH}Hollywood/"

    /* ---------- Pieces of information to build DB paths on research machine -------------------- */
    const val REDDIT_SUB_DB             = "RS_DB"
    const val REDDIT_COM_DB             = "RC_DB"
    const val HOLLYWOOD_DB              = "HOLLYWOOD"

    // The DB file extension for sqlite3 files
    const val DBEXT                     = Finals.DB_TYPE_EXT

    /* ---------- Path to the location of the server configuration file ------------------------- */
    const val LOCAL_SERVER_CONFIG_FILE  = "${LOCAL_DATA_ROOT_PATH}serverConfig/config.json"
    const val LOCAL_CLIENT_CONFIG_FILE  = "${LOCAL_DATA_ROOT_PATH}clientConfig/config.json"
    const val LOCAL_SERVER_CONFIG_PATH  = "${LOCAL_DATA_ROOT_PATH}clientConfig/"

    val DOLIUS_CONFIG_PATH        = "A:/DoliusConfigs${File.separator}"
    val SERVER_CONFIG_FILE        = "${DOLIUS_CONFIG_PATH}serverConfig${File.separator}config.json"
    val CLIENT_CONFIG_FILE        = "${DOLIUS_CONFIG_PATH}clientConfig${File.separator}config.json"
    val SERVER_CONFIG_PATH        = "${DOLIUS_CONFIG_PATH}serverConfig${File.separator}"
}
