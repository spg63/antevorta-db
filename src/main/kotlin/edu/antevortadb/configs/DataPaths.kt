/*
 * Copyright (c) 2018 Sean Grimes. All Rights Reserved.
 * License: MIT
 */

@file:Suppress("MemberVisibilityCanBePrivate")

package edu.antevortadb.configs

import java.io.File

@Suppress("RemoveCurlyBracesFromTemplate")
/**
 * The different paths to the DB, Json, and CSV data depending on which machine is running
 * the code and whether or not testing_mode has been enabled
 */
object DataPaths{
    val SEP: String                 = File.separator
    var CPUTESTINGMODE              = false

    /* ---------- The path to the local data folders ---------------------------------- */
    val LOCAL_DATA_ROOT = when(Finals.IS_WINDOWS) {
        // The SB2 running windows
        true -> "C:${SEP}Users${SEP}${Finals.SYSTEM_USER}${SEP}git${SEP}_DATA_${SEP}"
        // MBP & Mac Mini, only difference is the SYSTEM_USER name
        false -> "${SEP}Users${SEP}${Finals.SYSTEM_USER}${SEP}git${SEP}_DATA_${SEP}"
    }

    /* - File paths when running in 'TESTING_MODE' (i.e. on my MBP with
    limited data are LOCAL) ----------------------------------------------------------- */
    val LOCAL_PATH                  = "${LOCAL_DATA_ROOT}LocalData${SEP}raw${SEP}"
    val LOCAL_REDDIT_SUB_DATA       = "${LOCAL_PATH}submissions${SEP}"
    val LOCAL_REDDIT_COM_DATA       = "${LOCAL_PATH}comments${SEP}"
    val LOCAL_TMDB_CREDITS          = "${LOCAL_PATH}tmdb${SEP}credits.csv"
    val LOCAL_TMDB_MOVIES           = "${LOCAL_PATH}tmdb${SEP}movies.csv"
    val LOCAL_LENS_GENOME_SCORES    = "${LOCAL_PATH}movielens${SEP}genome_scores.csv"
    val LOCAL_LENS_GENOME_TAGS      = "${LOCAL_PATH}movielens${SEP}genome_tags.csv"
    val LOCAL_LENS_LINK             = "${LOCAL_PATH}movielens${SEP}link.csv"
    val LOCAL_LENS_MOVIE            = "${LOCAL_PATH}movielens${SEP}movie.csv"
    val LOCAL_LENS_RATING           = "${LOCAL_PATH}movielens${SEP}rating.csv"
    val LOCAL_LENS_TAG              = "${LOCAL_PATH}movielens${SEP}tag.csv"

    // Directory paths where new JSON data will be stored in 'TESTING_MODE'
    val LOCAL_NEW_REDDIT_SUB_DATA   = "${LOCAL_PATH}new${SEP}submissions${SEP}"
    val LOCAL_NEW_REDDIT_COM_DATA   = "${LOCAL_PATH}new${SEP}comments${SEP}"

    /* ---------- File paths when running on the research machine --------------------- */
    val RESEARCH_ARRAY_DATA_ROOT    = "${SEP}mnt${SEP}vault${SEP}Data${SEP}"
    val RESEARCH_RAW_DATA_ROOT      = "${RESEARCH_ARRAY_DATA_ROOT}Uncompressed${SEP}"

    val REDDIT_DATA_ROOT            = "${RESEARCH_RAW_DATA_ROOT}Reddit${SEP}"
    val REDDIT_SUB_DATA_PATH        = "${REDDIT_DATA_ROOT}Submissions${SEP}"
    val REDDIT_COM_DATA_PATH        = "${REDDIT_DATA_ROOT}Comments${SEP}"

    val TMDB_DATA_ROOT              = "${RESEARCH_RAW_DATA_ROOT}tmdb${SEP}"
    val TMDB_CREDITS                = "${TMDB_DATA_ROOT}credits.csv"
    val TMDB_MOVIES                 = "${TMDB_DATA_ROOT}movies.csv"

    val MOVIELENS_DATA_ROOT         = "${RESEARCH_RAW_DATA_ROOT}movielens${SEP}"
    val MOVIELENS_GENOME_SCORES     = "${MOVIELENS_DATA_ROOT}genome_scores.csv"
    val MOVIELENS_GENOME_TAGS       = "${MOVIELENS_DATA_ROOT}genome_tags.csv"
    val MOVIELENS_LINK              = "${MOVIELENS_DATA_ROOT}link.csv"
    val MOVIELENS_MOVIE             = "${MOVIELENS_DATA_ROOT}movie.csv"
    val MOVIELENS_RATING            = "${MOVIELENS_DATA_ROOT}rating.csv"
    val MOVIELENS_TAG               = "${MOVIELENS_DATA_ROOT}tag.csv"

    // Directory paths when running on the research machine for new JSON data
    val NEW_REDDIT_SUB_DATA_PATH    = "${REDDIT_DATA_ROOT}New${SEP}Submissions${SEP}"
    val NEW_REDDIT_COM_DATA_PATH    = "${REDDIT_DATA_ROOT}New${SEP}Comments${SEP}"

    /* ---------- Path to TESTING_MODE db files --------------------------------------- */
    val LOCAL_DB_PATH               = "${LOCAL_DATA_ROOT}LocalDB${SEP}"
    val LOCAL_REDDIT_SUB_DB_PATH    = "${LOCAL_DB_PATH}RedditSubs${SEP}"
    val LOCAL_REDDIT_COM_DB_PATH    = "${LOCAL_DB_PATH}RedditComs${SEP}"
    val LOCAL_HOLLYWOOD_DB_PATH     = "${LOCAL_DB_PATH}Hollywood${SEP}"

    /* ---------- Pieces of information to build DB paths on research machine --------- */
    const val REDDIT_SUB_DB         = "RS_DB"
    const val REDDIT_COM_DB         = "RC_DB"
    const val HOLLYWOOD_DB          = "HOLLYWOOD"

    // The DB file extension for sqlite3 files
    const val DBEXT                 = Finals.DB_TYPE_EXT

    /* ---------- Path to the location of the server configuration file --------------- */
    val LOCAL_SERVER_CONFIG_FILE    = "${LOCAL_DATA_ROOT}serverConfig${SEP}config.json"
    val LOCAL_CLIENT_CONFIG_FILE    = "${LOCAL_DATA_ROOT}clientConfig${SEP}config.json"
    val LOCAL_SERVER_CONFIG_PATH    = "${LOCAL_DATA_ROOT}clientConfig${SEP}"

    val DOLIUS_CONFIG_PATH          = "${SEP}mnt${SEP}vault${SEP}DoliusConfigs${SEP}"
    val SERVER_CONFIG_FILE          = "${DOLIUS_CONFIG_PATH}serverConfig${SEP}config.json"
    val CLIENT_CONFIG_FILE          = "${DOLIUS_CONFIG_PATH}clientConfig${SEP}config.json"
    val SERVER_CONFIG_PATH          = "${DOLIUS_CONFIG_PATH}serverConfig${SEP}"

    /* ---------- Paths to the DeepLearning4Java example data ------------------------- */
    val LOCAL_DL4J_ROOT             = "${LOCAL_DATA_ROOT}DL4J${SEP}"
    val RESEARCH_DL4J_ROOT          = "${SEP}mnt${SEP}vault${SEP}Data${SEP}DL4J${SEP}"
    val DL4J_CPU_ISSUE_ROOT         = "data${SEP}DL4J${SEP}"
    val LOCAL_AGENT_SPACE_ROOT      = "${LOCAL_DATA_ROOT}agent${SEP}"
    val RESEARCH_AGENT_SPACE_ROOT   = "${SEP}mnt${SEP}vault${SEP}Data${SEP}agent${SEP}"
}
