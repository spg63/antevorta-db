/*
 * Copyright (c) 2018 Sean Grimes. All Rights Reserved.
 * License: MIT
 */

@file:Suppress("MemberVisibilityCanBePrivate")

package edu.antevortadb.configs

import javalibs.FileUtils
import javalibs.Logic

@Suppress("RemoveCurlyBracesFromTemplate")
/**
 * The different paths to the DB, Json, and CSV data depending on which machine is running
 * the code and whether or not testing_mode has been enabled
 */
object DataPaths{
    val SEP: String                 = FileUtils.get().sep()
    var CPUTESTINGMODE              = false

    /* ---------- The path to the local data folders ---------------------------------- */
    var LOCAL_DATA_ROOT: String = when(Finals.SYSTEM_USER) {
        // The M1 user
        Finals.M1_USER ->
            "${SEP}Users${SEP}${Finals.SYSTEM_USER}${SEP}git${SEP}_data_${SEP}"
        // Ripper
        Finals.RIPPER_USER -> "" // Ripper doesn't use the LOCAL_DATA_ROOT path at all
        // Mac Mini
        Finals.MINI_USER ->
            "${SEP}Users${SEP}${Finals.SYSTEM_USER}${SEP}git${SEP}_DATA_${SEP}"
        Finals.MBP_USER ->
            "${SEP}Users${SEP}${Finals.SYSTEM_USER}${SEP}git${SEP}_data_${SEP}"
        Finals.NOTCH_USER ->
            "${SEP}users${SEP}${Finals.SYSTEM_USER}${SEP}git${SEP}_data_${SEP}"
        Finals.WIN_USER ->
            "C:${SEP}Users${SEP}seang${SEP}wingit${SEP}_data_${SEP}"
        else -> {
            if(Finals.IGNORE_DB_DATA_AND_USER_CHECKS) {
                if(Finals.otherUserDataPath.isNotEmpty())
                    Finals.otherUserDataPath
                else {
                    Logic.get().dieFrom("Finals.otherUserDataPath empty")
                    ""
                }
            }
            else {
                Logic.get().dieFrom("Unknown user")
                ""
            }
        }
    }

    /* - File paths when running in 'TESTING_MODE' (i.e. on my MBP with
    limited data are LOCAL) ----------------------------------------------------------- */
    val LOCAL_PATH                  = "${LOCAL_DATA_ROOT}LocalData${SEP}raw${SEP}"
    val LOCAL_REDDIT_SUB_DATA       = "${LOCAL_PATH}reddit${SEP}submissions${SEP}"
    val LOCAL_REDDIT_COM_DATA       = "${LOCAL_PATH}reddit${SEP}comments${SEP}"
    val LOCAL_TMDB_CREDITS          = "${LOCAL_PATH}tmdb${SEP}credits.csv"
    val LOCAL_TMDB_MOVIES           = "${LOCAL_PATH}tmdb${SEP}movies.csv"
    val LOCAL_W2VEC_PATH            = "${LOCAL_PATH}word2vec${SEP}"
    val LOCAL_EESI_PATH             = "${LOCAL_PATH}EESI${SEP}"
    val LOCAL_STATS_PATH            = "${LOCAL_PATH}stats${SEP}"
    val LOCAL_LENS_GENOME_SCORES    = "${LOCAL_PATH}movielens${SEP}genome_scores.csv"
    val LOCAL_LENS_GENOME_TAGS      = "${LOCAL_PATH}movielens${SEP}genome_tags.csv"
    val LOCAL_LENS_LINK             = "${LOCAL_PATH}movielens${SEP}link.csv"
    val LOCAL_LENS_MOVIE            = "${LOCAL_PATH}movielens${SEP}movie.csv"
    val LOCAL_LENS_RATING           = "${LOCAL_PATH}movielens${SEP}rating.csv"
    val LOCAL_LENS_TAG              = "${LOCAL_PATH}movielens${SEP}tag.csv"

    // Local paths for Mark Zarella's breast cancer data
    val LOCAL_BREAST_CANCER_PATH    = "${LOCAL_PATH}breastCancer${SEP}active${SEP}"
    val LOCAL_BC_ORIG_FILE          = "${LOCAL_BREAST_CANCER_PATH}${Finals.BC_ORIG_CSV}"
    val LOCAL_BC_CLEANED_FILE       = "${LOCAL_BREAST_CANCER_PATH}${Finals.BC_WORKING_CSV}"

    // Directory paths where new Reddit JSON data will be stored in 'TESTING_MODE'
    val LOCAL_NEW_REDDIT_SUB_DATA   = "${LOCAL_PATH}new${SEP}submissions${SEP}"
    val LOCAL_NEW_REDDIT_COM_DATA   = "${LOCAL_PATH}new${SEP}comments${SEP}"

    /* ---------- File paths when running on the research machine --------------------- */
    val RESEARCH_ARRAY_DATA_ROOT    = "${SEP}mnt${SEP}arrayVault${SEP}Data${SEP}"
    val RESEARCH_RAW_DATA_ROOT      = "${RESEARCH_ARRAY_DATA_ROOT}Uncompressed${SEP}"
    val RIPPER_MODELS_DIR           = "TrainedModels${SEP}"

    val REDDIT_DATA_ROOT            = "${RESEARCH_RAW_DATA_ROOT}Reddit${SEP}"
    val REDDIT_SUB_DATA_PATH        = "${REDDIT_DATA_ROOT}Submissions${SEP}"
    val REDDIT_COM_DATA_PATH        = "${REDDIT_DATA_ROOT}Comments${SEP}"

    val W2VEC_PATH                  = "${RESEARCH_RAW_DATA_ROOT}word2vec${SEP}"
    val EESI_PATH                   = "${RESEARCH_RAW_DATA_ROOT}EESI${SEP}"
    val STATS_PATH                  = "${RESEARCH_RAW_DATA_ROOT}stats${SEP}"

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

    // Paths for Mark Zarella's breast cancer data
    val BCancerPath                 = "${RESEARCH_RAW_DATA_ROOT}breastCancer${SEP}active${SEP}"
    val BC_ORIG_FILE                = "${BCancerPath}${Finals.BC_ORIG_CSV}"
    val BC_CLEANED_FILE             = "${BCancerPath}${Finals.BC_WORKING_CSV}"

    // Directory paths when running on the research machine for new JSON data
    val NEW_REDDIT_SUB_DATA_PATH    = "${REDDIT_DATA_ROOT}New${SEP}Submissions${SEP}"
    val NEW_REDDIT_COM_DATA_PATH    = "${REDDIT_DATA_ROOT}New${SEP}Comments${SEP}"

    /* ---------- Path to TESTING_MODE db files --------------------------------------- */
    val LOCAL_DB_PATH               = "${LOCAL_DATA_ROOT}LocalDB${SEP}"
    val LOCAL_REDDIT_SUB_DB_PATH    = "${LOCAL_DB_PATH}RedditSubs${SEP}"
    val LOCAL_REDDIT_COM_DB_PATH    = "${LOCAL_DB_PATH}RedditComs${SEP}"
    val LOCAL_HOLLYWOOD_DB_PATH     = "${LOCAL_DB_PATH}Hollywood${SEP}"
    val LOCAL_TRAINED_MODELS_DB     = "${LOCAL_DB_PATH}TrainedModels${SEP}"

    /* ---------- Pieces of information to build DB paths on research machine --------- */
    const val REDDIT_SUB_DB         = "RS_DB"
    const val REDDIT_COM_DB         = "RC_DB"
    const val HOLLYWOOD_DB          = "HOLLYWOOD"
    const val TRAINED_MODELS_DB     = "TRAINED_MODELS"

    // The DB file extension for sqlite3 files
    const val DBEXT                 = Finals.DB_TYPE_EXT

    /* ---------- Path to the location of the server configuration file --------------- */
    val LOCAL_SERVER_CONFIG_FILE    = "${LOCAL_DATA_ROOT}serverConfig${SEP}config.json"
    val LOCAL_CLIENT_CONFIG_FILE    = "${LOCAL_DATA_ROOT}clientConfig${SEP}config.json"
    val LOCAL_SERVER_CONFIG_PATH    = "${LOCAL_DATA_ROOT}clientConfig${SEP}"

    val DOLIUS_CONFIG_PATH          = "${SEP}mnt${SEP}arrayVault${SEP}DoliusConfigs${SEP}"
    val SERVER_CONFIG_FILE          = "${DOLIUS_CONFIG_PATH}serverConfig${SEP}config.json"
    val CLIENT_CONFIG_FILE          = "${DOLIUS_CONFIG_PATH}clientConfig${SEP}config.json"
    val SERVER_CONFIG_PATH          = "${DOLIUS_CONFIG_PATH}serverConfig${SEP}"

    /* ---------- Paths to the DeepLearning4Java example data ------------------------- */
    val LOCAL_DL4J_ROOT             = "${LOCAL_DATA_ROOT}DL4J${SEP}"
    val RESEARCH_DL4J_ROOT          = "${SEP}mnt${SEP}arrayVault${SEP}Data${SEP}DL4J${SEP}"
    val RESEARCH_AGENT_ROOT         = "${SEP}mnt${SEP}arrayVault${SEP}Data${SEP}agent${SEP}"
    val DL4J_CPU_ISSUE_ROOT         = "data${SEP}DL4J${SEP}"
    val HW_LOCAL_AGENT_ROOT         = "${LOCAL_DATA_ROOT}agent${SEP}hollywood${SEP}"
    val BC_LOCAL_AGENT_ROOT         = "${LOCAL_DATA_ROOT}agent${SEP}breastCancer${SEP}"
    val ONCO_LOCAL_AGENT_ROOT       = "${LOCAL_DATA_ROOT}agent${SEP}onco${SEP}"
    val HW_RESEARCH_AGENT_ROOT      = "${RESEARCH_AGENT_ROOT}hollywood${SEP}"
    val BC_RESEARCH_AGENT_ROOT      = "${RESEARCH_AGENT_ROOT}breastCancer${SEP}"
    val ONCO_RESEARCH_AGENT_ROOT    = "${RESEARCH_AGENT_ROOT}onco${SEP}"
    val AIRLINE_LOCAL_AGENT_ROOT    = "${LOCAL_DATA_ROOT}agent${SEP}airline${SEP}"
    val AIRLINE_RESEARCH_AGENT_ROOT = "${RESEARCH_AGENT_ROOT}airline${SEP}"
}
