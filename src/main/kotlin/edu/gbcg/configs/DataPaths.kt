/*
 * Copyright (c) 2018 Sean Grimes. All Rights Reserved.
 * License: MIT
 */

package edu.gbcg.configs

import java.io.File

/**
 * The different paths to the DB, Json, and CSV data depending on which machine is running the
 * code and whether or not testing_mode has been enabled
 */
object DataPaths{
    // File paths when running in 'TESTING_MODE' (i.e. on my MBP with limited data are LOCAL)
    const val LOCAL_SUB_DATA_PATH   = "LocalData/raw/submissions/"
    const val LOCAL_COM_DATA_PATH   = "LocalData/raw/comments/"

    // File paths when running on the research machine
    const val SUB_DATA_PATH         = "A:/Data/Uncompressed/Reddit/Submissions/"
    const val COM_DATA_PATH         = "A:/Data/Uncompressed/Reddit/Comments/"

    // Path to TESTING_MODE db files
    const val LOCAL_SUB_DB_PATH     = "LocalDB/RedditSubs/"
    const val LOCAL_COM_DB_PATH     = "LocalDB/RedditComs/"

    var DB_CONFIG_PATH        = "dbConfigs${File.separator}"

    const val SUB_DB_PREFIX         = "RS_DB"
    const val COM_DB_PREFIX         = "RC_DB"
    const val DB_POSTFIX            = Finals.DB_TYPE_EXT
}