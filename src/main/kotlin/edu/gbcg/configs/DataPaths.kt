/*
 * Copyright (c) 2018 Sean Grimes. All Rights Reserved.
 * License: MIT
 */

package edu.gbcg.configs

import java.io.File

/**
 * The different paths to the DB, Json, and CSV data depending on which machine is running the
 * code and whether or not testing_mode has been enabled
 */
object DataPaths{
    /* ---------- File paths when running in 'TESTING_MODE' (i.e. on my MBP with limited data are LOCAL) ------------ */
    const val localPath = "LocalData/raw/"
    const val LOCAL_REDDIT_SUB_DATA     = "${localPath}submissions/"
    const val LOCAL_REDDIT_COM_DATA     = "${localPath}comments/"
    const val LOCAL_TMDB_CREDITS        = "${localPath}tmdb/credits.csv"
    const val LOCAL_TMDB_MOVIES         = "${localPath}tmdb/movies.csv"
    const val LOCAL_LENS_GENOME_SCORES  = "${localPath}movielens/genome_scores.csv"
    const val LOCAL_LENS_GENOME_TAGS    = "${localPath}movielens/genome_tags.csv"
    const val LOCAL_LENS_LINK           = "${localPath}movielens/link.csv"
    const val LOCAL_LENS_MOVIE          = "${localPath}movielens/movie.csv"
    const val LOCAL_LENS_RATING         = "${localPath}movielens/rating.csv"
    const val LOCAL_LENS_TAG            = "${localPath}movielens/tag.csv"

    // Directory paths where new JSON data will be stored in 'TESTING_MODE'
    const val LOCAL_NEW_REDDIT_SUB_DATA = "${localPath}new/submissions/"
    const val LOCAL_NEW_REDDIT_COM_DATA = "${localPath}new/comments/"

    /* ---------- File paths when running on the research machine --------------------------------------------------- */
    const val researchPath = "A:/Data/Uncompressed/"
    const val REDDIT_SUB_DATA_PATH      = "${researchPath}Reddit/Submissions/"
    const val REDDIT_COM_DATA_PATH      = "${researchPath}Reddit/Comments/"
    const val TMDB_CREDITS              = "${researchPath}tmdb/credits.csv"
    const val TMDB_MOVIES               = "${researchPath}tmdb/movies.csv"
    const val MOVIELENS_GENOME_SCORES   = "${researchPath}movielens/genome_scores.csv"
    const val MOVIELENS_GENOME_TAGS     = "${researchPath}movielens/genome_tags.csv"
    const val MOVIELENS_LINK            = "${researchPath}movielens/link.csv"
    const val MOVIELENS_MOVIE           = "${researchPath}movielens/movie.csv"
    const val MOVIELENS_RATING          = "${researchPath}movielens/rating.csv"
    const val MOVIELENS_TAG             = "${researchPath}movielens/tag.csv"

    // Directory paths when running on the research machine for new JSON data
    const val NEW_REDDIT_SUB_DATA_PATH  = "${researchPath}Reddit/New/Submissions/"
    const val NEW_REDDIT_COM_DATA_PATH  = "${researchPath}Reddit/New/Comments/"

    /* ---------- Path to TESTING_MODE db files --------------------------------------------------------------------- */
    const val LOCAL_REDDIT_SUB_DB_PATH  = "LocalDB/RedditSubs/"
    const val LOCAL_REDDIT_COM_DB_PATH  = "LocalDB/RedditComs/"
    const val LOCAL_MOVIES_DB_PATH      = "LocalDB/Hollywood/"
    const val LOCAL_ACTORS_DB_PATH      = "LocalDB/Hollywood/"

    /* ---------- Pieces of information to build DB paths on research machine --------------------------------------- */
    const val REDDIT_SUB_DB_PREFIX      = "RS_DB"
    const val REDDIT_COM_DB_PREFIX      = "RC_DB"
    const val MOVIES_DB_PREFIX          = "MOVIES"
    const val ACTORS_DB_PREFIX          = "ACTORS"
    const val DB_POSTFIX                = Finals.DB_TYPE_EXT

    /* ---------- Path to the location of the server configuration file --------------------------------------------- */
    val DB_CONFIG_PATH                  = "dbConfigs${File.separator}"
}
