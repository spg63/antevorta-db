package edu.gbcg.configs;

/**
 * StateVars is a class to hold variables related to program state. Items like the database
 * driver being used, if testing mode is enabled, database batch size limits, number of database
 * shards, etc...
 */
public class StateVars {
    public static boolean TESTING_MODE = true;
    public static final String DB_DRIVER = "org.sqlite.JDBC";
    public static final String DB_URL_PREFIX = "jdbc:sqlite:";
    public static final String DB_TYPE_EXT = ".sqlite3";
    public static final int DB_BATCH_LIMIT = 1000;
    public static boolean START_FRESH = true;
    public static final int DB_SHARD_NUM = 8;
}
