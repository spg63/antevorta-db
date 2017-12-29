package edu.gbcg.configs;

/**
 * StateVars is a class to hold variables related to program state. Items like the database
 * driver being used, if testing mode is enabled, database batch size limits, number of database
 * shards, etc...
 */
public class StateVars {
    /*-------------------- Program control --------------------*/
    // True when working locally on MBP, false when working on full dataset, changes data / db paths
    public static final boolean TESTING_MODE = isWindows() ? false : true;
    // Drops the DBs if they exist and reads in the data again
    public static final boolean START_FRESH = false;


    /*-------------------- Database control --------------------*/
    public static final String DB_DRIVER = "org.sqlite.JDBC";
    public static final String DB_URL_PREFIX = "jdbc:sqlite:";
    public static final String DB_TYPE_EXT = ".sqlite3";
    // Larger batch size performs better on research machine with individual HDDs for each DB shard
    public static final int RESEARCH_BATCH_SIZE = 3000;
    // Performs better on single laptop SSD
    public static final int LAPTOP_BATCH_SIZE = 1000;
    public static final int DB_BATCH_LIMIT = TESTING_MODE ? LAPTOP_BATCH_SIZE : RESEARCH_BATCH_SIZE;
    // Turns off sqlite synchronous mode, faster batch insertions
    public static final boolean SYNC_MODE_OFF = true;
    // There are 6 available HDDs for data storage on the research machine, use all of them
    public static final int DB_SHARD_NUM = 6;
    // Table names
    public static final String SUB_TABLE_NAME = "submission_attrs";

    // Very basic, needs to be more robust but works now on my known machines. Will almost
    // certainly fail at some point in the future with unexpected hardware and I won't have a
    // damn clue why and it'll take me a few hours to find this again. Future me: sorry bro.
    private static boolean isWindows(){
        String osString = System.getProperty("os.name").toLowerCase();
        if(osString.contains("win"))
            return true;
        return false;
    }
}
