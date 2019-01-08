/*
 * Copyright (c) 2018 Sean Grimes. All Rights Reserved.
 * License: MIT
 */

@file:Suppress("MayBeConstant")

package edu.antevortadb.configs

import edu.antevortadb.utils.TSL
import java.io.File
import java.lang.RuntimeException

/**
 * Finals is a class to hold variables related to program state. Items like the database
 * driver being used, if testing mode is enabled, database batch size limits, number of database
 * shards, etc...
 */
object Finals{
    /* ---------- Program control --------------------------------------------------------------- */
    // user.name of the current user running this software
    val SYSTEM_USER = initUser()
    // True if windows, else false
    val IS_WINDOWS = System.getProperty("os.name").contains("win")
                     || System.getProperty("os.name").contains("Win")
    // True when working locally on MBP, false when working on full dataset, changes data & db paths
    val TESTING_MODE = !isResearchMachine()
    // Drops the DBs if they exist and reads in the data again
    const val START_FRESH = false
    // Simple check to make sure we really want to add new data to the DBs
    const val ADD_NEW_DATA = false


    /* ---------- Database control -------------------------------------------------------------- */
    const val DB_DRIVER = "org.sqlite.JDBC"
    const val DB_URL_PREFIX = "jdbc:sqlite:"
    const val DB_TYPE_EXT = ".sqlite3"
    var enableForeignKeys = false
    // Larger batch size performs better on research machine with individual HDDs for each DB shard
    private const val RESEARCH_BATCH_SIZE = 10000
    // Performs better on single laptop SSD
    private const val LAPTOP_BATCH_SIZE = 1000
    val DB_BATCH_LIMIT = if(isResearchMachine()) RESEARCH_BATCH_SIZE else LAPTOP_BATCH_SIZE
    // Turns off sqlite synchronous mode, faster batch insertions
    const val SYNC_MODE_OFF = true
    // There are 6 available HDDs for data storage on research machine, use all of them
    const val DB_SHARD_NUM = 6
    // Reddit table names
    const val REDDIT_SUB_TABLE              = "submission_attrs"
    const val REDDIT_COM_TABLE              = "comment_attrs"
    const val ML_LINK_TABLE                 = "links_table"
    const val ML_MOVIES_TABLE               = "movielens_movies"
    const val TMDB_CREDITS_TABLE            = "tmdb_credits"
    const val ML_GENOME_TAGS_TABLE          = "genome_tags"
    const val ML_GENOME_SCORES_TABLE        = "genome_scores"
    const val ML_INDIVIUDAL_TAGS_TABLE      = "individual_tags"
    const val ML_INDIVIDUAL_RATING_TABLE    = "individual_ratings"
    const val TMDB_MOVIES_TABLE             = "movies"


    /* ---------- Server control ---------------------------------------------------------------- */
    const val SERVER_SOCKET = 3383
    // NOTE: These columns are common to most DB types and are named here for
    // consistency across insertions
    // and selection from various data sources. It will allow for further generalization
    // in higher levels of code

    // Used to identify the name or username of a poster
    const val AUTHOR = "author"

    // Used to identify the content of a post or comment, if it exists
    const val BODY = "body"

    // Used to identify the SQLite compatible datetime string associated with post
    // creation date / time
    const val CREATED_DT = "created_dt"

    // Used to identify the ID of a post, if it exists
    const val POST_ID = "pid"

    // Used to identify a link to the post, if it exists
    const val PERMALINK = "permalink"

    // Used to identify the SQLite compatible date-time string associated with
    // post scraped on date / time
    const val SCRAPED_DT = "scraped_on"

    // Used to identify a score associated with a post, if it exists
    const val SCORE = "score"

    // Used to identify a title of a post, if it exists
    const val TITLE = "post_title"

    // Used for the autoincrement key
    const val ID = "ID"

    // Hollywood movie id for TMDB
    const val TMDB_ID = "tmdb_movieid"

    // Hollywood movie id for IMDB
    const val IMDB_ID = "imdb_movieid"

    // Hollywood movie id for Movielens
    const val ML_ID = "movielens_movieid"

    // User ID column (not user name! A numeric ID!)
    const val USER_ID = "userid"


    /* ---------- Helper functions ---------------------------------------------------- */
    // Function to force-init the SYSTEM_USER variable
    fun initUser(): String = System.getProperty("user.name")

    // Very basic, needs to be more robust but works now on my known machines. Will almost
    // certainly fail at some point in the future with unexpected hardware and I won't
    // have a damn clue why and it'll take me a few hours to find this again. Future
    // me: sorry.
    fun isResearchMachine(): Boolean {
        val linuxUser = "ripper"
        val windowsUser = "Osiris"
        val macUser = "hades"

        if(SYSTEM_USER == linuxUser)
            return true
        if(SYSTEM_USER == windowsUser || SYSTEM_USER == macUser)
            return false

        TSL.get().err("USER: $SYSTEM_USER")
        TSL.get().logAndKill("Unknown hardware / user. Datapaths will likely be " +
                "incorrect. Contact spg63@drexel.edu. Quitting.")

        // We'll never get here, but the compiler doesn't know that
        return false
        /*
        // Check if this is a windows machine, and if it's my SB2
        if(System.getProperty("os.name").toLowerCase().contains("win")){
            if(System.getProperty("user.name") != "Osiris")
                TSL.get().logAndKill("We're on Windows but not on my machine, where are" +
                        " we? I don't know, but the data paths won't be correct.")

            return false
        }

        // See if we're on linux
        if(System.getProperty("os.name").toLowerCase().contains("linux")){
            // See if we're on ripper, as expected
            if(System.getProperty("user.name").toLowerCase() != "ripper") {
                TSL.get().logAndKill("Running on linux, but not on ripper, data configurations " +
                        "will not be correct. Please contact spg63@drexel.edu for instruction")
            }
            // We're on linux, and we're on ripper, so it's the research machine
            return true
        }

        // We're on my laptop,
        if(System.getProperty("user.name").toLowerCase() != "hades"){
            TSL.get().logAndKill("We're not on linux, not on windows, and apparently not on my " +
                    "laptop. Not sure where we are, so we're going to run away and die.")
        }

        // Seems we're on my laptop. This is fine.
        return false
        */
    }

    /* ---------- Random constants ---------------------------------------------------------------*/
    const val NOTAGLINE = "NO TAGLINE"
}
