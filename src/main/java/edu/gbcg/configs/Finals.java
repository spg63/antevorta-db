/*
 * Copyright (c) 2018 Sean Grimes. All rights reserved.
 * License: MIT License
 */

package edu.gbcg.configs;

/**
 * Finals is a class to hold variables related to program state. Items like the database
 * driver being used, if testing mode is enabled, database batch size limits, number of database
 * shards, etc...
 */
public class Finals {
    /*-------------------- Program control --------------------*/
    // True when working locally on MBP, false when working on full dataset, changes data / db paths
    public static final boolean TESTING_MODE = isWindows() ? false : true;
    // Drops the DBs if they exist and reads in the data again
    public static boolean START_FRESH = false;


    /*-------------------- Database control --------------------*/
    public static final String DB_DRIVER = "org.sqlite.JDBC";
    public static final String DB_URL_PREFIX = "jdbc:sqlite:";
    public static final String DB_TYPE_EXT = ".sqlite3";
    public static final boolean ENABLE_FOREIGN_KEYS = false;
    // Larger batch size performs better on research machine with individual HDDs for each DB shard
    public static final int RESEARCH_BATCH_SIZE = 7500;
    // Performs better on single laptop SSD
    public static final int LAPTOP_BATCH_SIZE = 1000;
    public static final int DB_BATCH_LIMIT = TESTING_MODE ? LAPTOP_BATCH_SIZE : RESEARCH_BATCH_SIZE;
    // Turns off sqlite synchronous mode, faster batch insertions
    public static final boolean SYNC_MODE_OFF = true;
    // There are 6 available HDDs for data storage on the research machine, use all of them
    public static final int DB_SHARD_NUM = 6;
    // Table names
    public static final String SUB_TABLE_NAME = "submission_attrs";
    public static final String COM_TABLE_NAME = "comment_attrs";

    // Very basic, needs to be more robust but works now on my known machines. Will almost
    // certainly fail at some point in the future with unexpected hardware and I won't have a
    // damn clue why and it'll take me a few hours to find this again. Future me: sorry bro.
    public static boolean isWindows(){
        String osString = System.getProperty("os.name").toLowerCase();
        if(osString.contains("win"))
            return true;
        return false;
    }

    /*-------------------- Database column names --------------------*/
/*
     NOTE: These columns are common to all DB types and are named here for consistency across insertions and
     selection from various data sources. It will allow for further generalization in higher levels of code
*/

    // Used to identify the name or username of a poster
    public static final String AUTHOR = "author";

    // Used to identify the content of a post or comment, if it exists
    public static final String BODY = "body";

    // Used to identify the SQLite compatible date-time string associated with post creation date / time
    public static final String CREATED_DT = "created_dt";

    // Used to identify the ID of a post, if it exists
    public static final String POST_ID = "pid";

    // Used to identify a link to the post, if it exists
    public static final String PERMALINK = "permalink";

    // Used to identify the SQLite compatible date-time string associated with post scraped on date / time
    public static final String SCRAPED_DT = "scraped_on";

    // Used to identify a score associated with a post, if it exists
    public static final String SCORE = "score";

    // Used to identify a title of a post, if it exists
    public static final String TITLE = "post_title";
}
