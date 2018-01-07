/*
 * Copyright (c) 2018 Sean Grimes. All rights reserved.
 * License: MIT License
 */

package edu.gbcg.configs;

/**
 * The different paths to the DB, Json, and CSV data depending on which machine is running the
 * code and whether or not testing_mode has been enabled
 */
public class DataPaths {
    // File paths when running in 'TESTING_MODE' (i.e. on my MBP with limited data are LOCAL)
    //public static final String LOCAL_SUB_DATA_PATH = "LocalData/compressed/submissions/";
    //public static final String LOCAL_COM_DATA_PATH = "LocalData/compressed/comments/";
    public static final String LOCAL_SUB_DATA_PATH = "LocalData/raw/submissions/";
    public static final String LOCAL_COM_DATA_PATH = "LocalData/raw/comments/";

    //public static final String SUB_DATA_PATH = "A:/Data/Compressed/Reddit/Submissions/";
    //public static final String COM_DATA_PATH = "A:/Data/Compressed/Reddit/Comments/";
    public static final String SUB_DATA_PATH = "A:/Data/Uncompressed/Reddit/Submissions/";
    public static final String COM_DATA_PATH = "A:/Data/uncompressed/Reddit/Comments/";

    public static final String LOCAL_SUB_DB_PATH = "LocalDB/RedditSubs/";
    public static final String LOCAL_COM_DB_PATH = "LocalDB/RedditComs/";

    public static final String SUB_DB_PREFIX        = "RS_DB";
    public static final String COM_DB_PREFIX        = "RC_DB";
    public static final String DB_POSTFIX           = Finals.DB_TYPE_EXT;

}
