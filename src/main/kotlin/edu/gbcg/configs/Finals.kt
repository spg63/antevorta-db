/*
 * Copyright (c) 2018 Sean Grimes. All Rights Reserved.
 * License: MIT
 */

package edu.gbcg.configs

import java.io.File

/**
 * Finals is a class to hold variables related to program state. Items like the database
 * driver being used, if testing mode is enabled, database batch size limits, number of database
 * shards, etc...
 */
object Finals{
    /*-------------------- Program control --------------------*/
    // True when working locally on MBP, false when working on full dataset -- changes data & db paths
    @JvmField val TESTING_MODE = !isWindows()
    // Drops the DBs if they exist and reads in the data again
    @JvmField val START_FRESH = false
    // Simple check to make sure we really want to add new data to the DBs
    @JvmField val ADD_NEW_DATA = true


    /*-------------------- Database control --------------------*/
    const val DB_DRIVER = "org.sqlite.JDBC"
    const val DB_URL_PREFIX = "jdbc:sqlite:"
    const val DB_TYPE_EXT = ".sqlite3"
    const val ENABLE_FOREIGN_KEYS = false
    // Larger batch size performs better on research machine with individual HDDs for each DB shard
    const private val RESEARCH_BATCH_SIZE = 7500
    // Performs better on single laptop SSD
    const private val LAPTOP_BATCH_SIZE = 1000
    @JvmField val DB_BATCH_LIMIT = if(isWindows()) RESEARCH_BATCH_SIZE else LAPTOP_BATCH_SIZE
    // Turns off sqlite synchronous mode, faster batch insertions
    const val SYNC_MODE_OFF = true
    // There are 6 available HDDs for data storage on research machine, use all of them
    const val DB_SHARD_NUM = 6
    // Reddit table names
    const val SUB_TABLE_NAME = "submission_attrs"
    const val COM_TABLE_NAME = "comment_attrs"


    // Very basic, needs to be more robust but works now on my known machines. Will almost
    // certainly fail at some point in the future with unexpected hardware and I won't have a
    // damn clue why and it'll take me a few hours to find this again. Future me: sorry bro.
    /*-------------------- Database column names --------------------*/
    @JvmStatic fun isWindows(): Boolean {
        if(System.getProperty("os.name").toLowerCase().contains("win"))
            return true
        return false
    }

    /*-------------------- Server control ---------------------------*/
    const val SERVER_SOCKET = 3383
    const val SERVER_CONFIG_FILE_NAME = "doliusServerConfigsAndUsers.json"
    var CLIENT_CONFIG = "serverConfigFileDir${File.separator}clientConfig.json"

    // NOTE: These columns are common to all DB types and are named here for consistency across insertions and
    // selection from various data sources. It will allow for further generalization in higher levels of code

    // Used to identify the name or username of a poster
    const val AUTHOR = "author"

    // Used to identify the content of a post or comment, if it exists
    const val BODY = "body"

    // Used to identify the SQLite compatible date-time string associated with post creation date / time
    const val CREATED_DT = "created_dt"

    // Used to identify the ID of a post, if it exists
    const val POST_ID = "pid"

    // Used to identify a link to the post, if it exists
    const val PERMALINK = "permalink"

    // Used to identify the SQLite compatible date-time string associated with post scraped on date / time
    const val SCRAPED_DT = "scraped_on"

    // Used to identify a score associated with a post, if it exists
    const val SCORE = "score"

    // Used to identify a title of a post, if it exists
    const val TITLE = "post_title"
}
