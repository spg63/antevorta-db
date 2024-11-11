/*
 * Copyright (c) 2018 Sean Grimes. All Rights Reserved.
 * License: MIT
 */

@file:Suppress("MayBeConstant")

package edu.antevortadb.configs

import javalibs.Logic
import javalibs.SysHelper
import javalibs.TSL

/**
 * Finals is a class to hold variables related to program state. Items like the database
 * driver being used, if testing mode is enabled, database batch size limits, number of
 * database shards, etc...
 */
object Finals{
    /* ---------- Variables that need to be set dynamically in a function ------------- */
    val sysUtils: SysHelper                 = SysHelper.get()
    val SYSTEM_USER: String
    val TESTING_MODE: Boolean
    val IS_WINDOWS: Boolean
    const val BAD_DATA: Int                 = -1337

    /* ---------- Program control ----------------------------------------------------- */
    val M1_USER                             = "amun-ra"
    val MINI_USER                           = "anubis"
    val RIPPER_USER                         = "ripper"
    val MBP_USER                            = "osiris"
    val WIN_USER                            = "seang"
    val NOTCH_USER                          = "topnotch"
    val NONRESEARCH_USERS_LIST = listOf(
            M1_USER, MINI_USER, MBP_USER, WIN_USER, NOTCH_USER
    )
    var IGNORE_DB_DATA_AND_USER_CHECKS = false
    lateinit var otherUserDataPath: String

    // Larger batch size performs better on research machine with individual HDDs for each
    // DB shard
    private const val RESEARCH_BATCH_SIZE   = 12000
    // Performs better on single laptop SSD
    private const val LAPTOP_BATCH_SIZE     = 1000

    val DB_BATCH_LIMIT: Int

    var overrideTelemetry: Boolean          = true

    init{
        SYSTEM_USER = initUser()
        TESTING_MODE = !isResearchMachine()
        IS_WINDOWS = isWindowsMachine()
        if(NONRESEARCH_USERS_LIST.contains(WIN_USER) && IS_WINDOWS){
            TSL.get().info("Running on Windows, probably the SP8")
        }
        DB_BATCH_LIMIT = batchLimit()
        telemetry()
    }

    /* ---------- Database control ---------------------------------------------------- */
    // Drops the DBs if they exist and reads in the data again
    const val START_FRESH                   = false
    // Simple check to make sure we really want to add new data to the DBs
    const val ADD_NEW_DATA                  = false
    const val DB_DRIVER                     = "org.sqlite.JDBC"
    const val DB_URL_PREFIX                 = "jdbc:sqlite:"
    const val DB_TYPE_EXT                   = ".sqlite3"
    var enableForeignKeys                   = false
    // Turns off sqlite synchronous mode, faster batch insertions
    const val SYNC_MODE_OFF                 = true
    // There are 6 available HDDs for data storage on research machine, use all of them
    const val DB_SHARD_NUM                  = 6
    // Configuration table
    const val CONFIG_TABLE                  = "configs"
    // Reddit table names
    const val REDDIT_SUB_TABLE              = "submission_attrs"
    const val REDDIT_COM_TABLE              = "comment_attrs"
    // Movielens / TMDB table names
    const val ML_LINK_TABLE                 = "links_table"
    const val ML_MOVIES_TABLE               = "movielens_movies"
    const val TMDB_CREDITS_TABLE            = "tmdb_credits"
    const val ML_GENOME_TAGS_TABLE          = "genome_tags"
    const val ML_GENOME_SCORES_TABLE        = "genome_scores"
    const val ML_INDIVIUDAL_TAGS_TABLE      = "individual_tags"
    const val ML_INDIVIDUAL_RATING_TABLE    = "individual_ratings"
    const val TMDB_MOVIES_TABLE             = "movies"
    // Model storage
    const val TRAINED_MODELS_TABLE          = "classifier_models"
    // Breast Cancer constants
    const val BC_WORKING_CSV                = "BC_working.csv"
    const val BC_ORIG_CSV                   = "BC03_17.csv"

    /* ---------- Server control ------------------------------------------------------ */
    const val SERVER_SOCKET_PORT            = 3383
    const val SERVER_SOCKET_HOST            = "corticus.us"
    // NOTE: These columns are common to most DB types and are named here for
    // consistency across insertions and selection from various data sources. It will
    // allow for further generalization in higher levels of code

    // Used to identify the name or username of a poster
    const val AUTHOR                        = "author"

    // Used to identify the content of a post or comment, if it exists
    const val BODY                          = "body"

    // Used to identify the SQLite compatible datetime string associated with post
    // creation date / time
    const val CREATED_DT                    = "created_dt"

    // Used to identify the ID of a post, if it exists
    const val POST_ID                       = "pid"

    // Used to identify a link to the post, if it exists
    const val PERMALINK                     = "permalink"

    // Used to identify the SQLite compatible date-time string associated with
    // post scraped on date / time
    const val SCRAPED_DT                    = "scraped_on"

    // Used to identify a score associated with a post, if it exists
    const val SCORE                         = "score"

    // Used to identify a title of a post, if it exists
    const val TITLE                         = "post_title"

    // Used for the autoincrement key
    const val ID                            = "ID"

    // Hollywood movie id for TMDB
    const val TMDB_ID                       = "tmdb_movieid"

    // Hollywood movie id for IMDB
    const val IMDB_ID                       = "imdb_movieid"

    // Hollywood movie id for Movielens
    const val ML_ID                         = "movielens_movieid"

    // User ID column (not user name! A numeric ID!)
    const val USER_ID                       = "userid"

    /* ---------- Helper functions ---------------------------------------------------- */
    // Function to force-init the SYSTEM_USER variable
    fun initUser(): String {
        return if(IGNORE_DB_DATA_AND_USER_CHECKS)
            ""
        else
            sysUtils.userName()
    }

    fun telemetry() {
        // Allow telemetry collection to be overriden when using a known user account
        // and flag is set to skip
        if((overrideTelemetry && !TESTING_MODE) ||
                (overrideTelemetry && NONRESEARCH_USERS_LIST.contains(SYSTEM_USER)))
            return
        println("override: ${overrideTelemetry}")
        println("contains: ${NONRESEARCH_USERS_LIST.contains(SYSTEM_USER)}")
        println("not skipped")
        val telemetry = Telemetry()
        TSL.get().trace("Finals initialization complete, gathering telemetry")
        telemetry.push()
    }

    // Very basic, needs to be more robust but works now on my known machines. Will almost
    // certainly fail at some point in the future with unexpected hardware and I won't
    // have a fucking clue why and it'll take me a few hours to find this again.
    // Future me: sorry.
    fun isResearchMachine(): Boolean {
        // Special mode enabled for people to run without complete data-path setup
        if(IGNORE_DB_DATA_AND_USER_CHECKS) return false

        // Running on ripper, the research machine
        if(SYSTEM_USER == RIPPER_USER) return true

        // Not ripper, and not special mode, user must exist in the users list to continue
        // If the user doesn't exist this check will kill the program
        Logic.get().require(NONRESEARCH_USERS_LIST.contains(SYSTEM_USER),
                "Unknown hardware / user. Data paths wil be incorrect. Contact " +
                        "sean@seanpgrimes.com or properly enable special access mode " +
                        "following the instructions you were given.")

        // The user exists in the NONRESEARCH_USERS_LIST, return false
        return false
    }

    // Function only necessary to dynamically set this at runtime while retaining val
    // instead of var properties
    fun isWindowsMachine(): Boolean {
        val osName = sysUtils.osName()
        return osName.toLowerCase().contains("win")
    }

    // Function only necessary to dynamically set this at runtime while retaining val
    // instead of var properties
    fun batchLimit(): Int {
        if(isResearchMachine())
            return RESEARCH_BATCH_SIZE
        return LAPTOP_BATCH_SIZE
    }

    /* ---------- Random constants -----------------------------------------------------*/
    const val NOTAGLINE = "NO TAGLINE"
}
