package edu.gbcg.configs;

import java.io.File;

/**
 * The different paths to the DB, Json, and CSV data depending on which machine is running the
 * code and whether or not testing_mode has been enabled
 */
public class DataPaths {
    // File paths when running in 'TESTING_MODE' (i.e. on my MBP with limited data are LOCAL)
    public static final String LOCAL_DATA_PATH      =
            "LocalData"+File.separator+"raw"+File.separator;
    public static final String DATA_PATH            = "NULL_FUCKER";

    public static final String LOCAL_SUB_DB_PATH    =
            "LocalDB"+File.separator+"RedditSubs"+File.separator;
    public static final String LOCAL_COM_DB_PATH    =
            "LocalDB"+File.separator+"RedditComs"+File.separator;
    public static final String SUB_DB_PATH          = "NULL_FUCKER:";
    public static final String COM_DB_PATH          = "NULL_FUCKER:";

    public static final String SUB_DB_PREFIX        = "RS_DB_";
    public static final String COM_DB_PREFIX        = "RC_DB_";
    public static final String DB_POSTFIX           = StateVars.DB_TYPE_EXT;

}
