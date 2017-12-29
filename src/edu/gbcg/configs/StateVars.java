package edu.gbcg.configs;

/**
 * StateVars is a class to hold variables related to program state. Items like the database
 * driver being used, if testing mode is enabled, database batch size limits, number of database
 * shards, etc...
 */
public class StateVars {
    // True when working locally on MBP, false when working on full dataset
    public static boolean TESTING_MODE = true;
    // Will drop the DBs
    public static boolean START_FRESH = true;

    // DB related defaults
    public static final String DB_DRIVER = "org.sqlite.JDBC";
    public static final String DB_URL_PREFIX = "jdbc:sqlite:";
    public static final String DB_TYPE_EXT = ".sqlite3";
    public static final int DB_BATCH_LIMIT = 1000;
    public static final int DB_SHARD_NUM = 6;

    // Table names
    public static final String SUB_TABLE_NAME = "submission_attrs";
}
