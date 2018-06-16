/*
 * Copyright (c) 2018 Sean Grimes. All Rights Reserved.
 * License: MIT
 */

package edu.gbcg.configs

import edu.gbcg.utils.FileUtils
import javax.xml.crypto.Data

/**
 * Similar concept to RawDataLocator. The location of the databases will be different depending
 * on which machine this code is running on and whether or not testing_mode is enabled. This
 * class abstracts away the paths to the DB files.
 *
 * NOTE: Functions below are split into groupings of however many different DBs exist. Each type of DB needs these
 * functions for access. However many paths are found below in "Paths to the directories...." will tell you how many
 * different functions should be in each grouping.
 */
object DBLocator {
    private val futils = FileUtils.get()
    /* ----- List of drive letters on the research machine that stores the db shards -------------------------------- */
    private val DRIVES = arrayOf("F", "G", "H", "I", "J", "K")

    /* ----- Paths to the directories that hold the DB shards without the drive letter prefix ----------------------- */
    private const val REDDIT_SUB_DB_DIR_PATH    = ":/DBs/Reddit/Submissions/"
    private const val REDDIT_COM_DB_DIR_PATH    = ":/DBs/Reddit/Comments/"
    private const val HOLLYWOOD_DB_DIR_PATH     = ":/DBs/Hollywood/"

    /* ----- Paths to the DB shards without the drive letter prefix ------------------------------------------------- */
    private const val REDDIT_SUB_SHARD  = "$REDDIT_SUB_DB_DIR_PATH${DataPaths.REDDIT_SUB_DB}${DataPaths.DBEXT}"
    private const val REDDIT_COM_SHARD  = "$REDDIT_COM_DB_DIR_PATH${DataPaths.REDDIT_COM_DB}${DataPaths.DBEXT}"
    private const val HOLLYWOOD_SHARD   = "$HOLLYWOOD_DB_DIR_PATH${DataPaths.HOLLYWOOD_DB}${DataPaths.DBEXT}"

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Get a list of absolute file paths to all reddit submission DBs
     * @return List of file paths to submission DBs if they exist, otherwise null
     */
    fun redditSubsAbsolutePaths(): List<String> {
        return when(Finals.TESTING_MODE){
            true -> futils.getAllFilePathsInDirWithPrefix(DataPaths.REDDIT_SUB_DB, getSubDBDirectoryPath()[0])
            false -> subDBPathsList()
        }
    }

    /**
     * Get a list of absolute file paths to all reddit comment DBs
     * @return List of file paths to comment DBs if they exist, otherwise null
     */
    fun redditComsAbsolutePaths(): List<String> {
        return when(Finals.TESTING_MODE){
            true -> futils.getAllFilePathsInDirWithPrefix(DataPaths.REDDIT_COM_DB, getComDBDirectoryPath()[0])
            false -> comDBPathsList()
        }
    }

    /**
     * Get a list of absolute file paths to all hollywood DBs
     * $return List of file paths to the hollywood DBs if they exist, otherwise null
     */
    fun hollywoodAbsolutePaths(): List<String> {
        return when(Finals.TESTING_MODE){
            true -> futils.getAllFilePathsInDirWithPrefix(DataPaths.HOLLYWOOD_DB, getHollywoodDBDirectoryPaths()[0])
            false -> hollywoodDBPathsList()
        }
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Get the path to the directory that holds the submission DBs. This path changes depending on
     * which machine this code is running on.
     * @return Absolute file path to the directories holding the submission databases
     */
    fun getSubDBDirectoryPath(): List<String> {
        return when(Finals.TESTING_MODE){
            true -> listOf(DataPaths.LOCAL_REDDIT_SUB_DB_PATH)
            false -> {
                val paths = ArrayList<String>()
                DRIVES.mapTo(paths) { it + REDDIT_SUB_DB_DIR_PATH }
                paths
            }
        }
    }

    /**
     * Get the path to the directory that holds the comment DBs. This path changes depending on
     * which machine this code is running on.
     * @return Absolute file path to the directories holding the comments databases
     */
    fun getComDBDirectoryPath(): List<String> {
        return when(Finals.TESTING_MODE){
            true -> listOf(DataPaths.LOCAL_REDDIT_COM_DB_PATH)
            false -> {
                val paths = ArrayList<String>()
                DRIVES.mapTo(paths) { it + REDDIT_COM_DB_DIR_PATH }
                paths
            }
        }
    }

    /**
     * Get a path to the directory that holds the hollywood DBs. This path changes depending on which
     * machine this code is running on.
     * #return Absolute file path to the directories holding the hollywood databases
     */
    fun getHollywoodDBDirectoryPaths(): List<String> {
        return when(Finals.TESTING_MODE){
            true -> listOf(DataPaths.LOCAL_HOLLYWOOD_DB_PATH)
            false -> {
                val paths = ArrayList<String>()
                DRIVES.mapTo(paths) { it + HOLLYWOOD_DB_DIR_PATH }
                paths
            }
        }
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Build paths to the submission databases. Intended to be used when the DBs don't yet exist
     * but a path to the DBs is needed for creation.
     * @return List of absolute paths to DBs (that don't yet exist)
     */
    fun buildSubDBPaths(): List<String> {
        return when(Finals.TESTING_MODE){
            true -> buildDBPaths(getSubDBDirectoryPath()[0], DataPaths.REDDIT_SUB_DB)
            false -> subDBPathsList()
        }
    }

    /**
     * Build paths to the comment databases. Intended to be used when the DBs don't yet exist but
     * a path to the DBs is needed for creation.
     * @return List of absolute paths to the DBs (that don't yet exist)
     */
    fun buildComDBPaths(): List<String> {
        return when(Finals.TESTING_MODE){
            true -> buildDBPaths(getComDBDirectoryPath()[0], DataPaths.REDDIT_COM_DB)
            false -> comDBPathsList()
        }
    }

    /**
     * Build paths to the hollywood databases. intended to be used when the DBs don't yet exist but
     * a path to the DBs is needed for creation.
     * @return List of absolute paths to the DBs (that don't yet exist)
     */
    fun buildHollywoodDBPaths(): List<String> {
        return when(Finals.TESTING_MODE){
            true -> buildDBPaths(getHollywoodDBDirectoryPaths()[0], DataPaths.HOLLYWOOD_DB)
            false -> hollywoodDBPathsList()
        }
    }

    // -----------------------------------------------------------------------------------------------------------------

    /*
        ** NO JAVADOC **
        * Actually builds the paths to the DBs based on requested com/sub dir and their prefix
     */
    private fun buildDBPaths(db_dir: String, db_prefix: String): List<String> {
        val sbs = ArrayList<StringBuilder>()
        for(i in 0 until Finals.DB_SHARD_NUM){
            val sb = StringBuilder()
            sb.append(db_dir)
            sb.append(db_prefix)
            sb.append(i)
            sb.append(DataPaths.DBEXT)
            sbs.add(sb)
        }

        val paths = ArrayList<String>()
        sbs.mapTo(paths) { it.toString() }
        return paths
    }

    private fun subDBPathsList(): List<String> {
        val re = ArrayList<String>()
        // for-loop replacement, for each item in DRIVES, add item + REDDIT_SUB_SHARD to 're'
        DRIVES.mapTo(re) { it + REDDIT_SUB_SHARD }
        return re
    }

    private fun comDBPathsList(): List<String> {
        val re = ArrayList<String>()
        DRIVES.mapTo(re) { it + REDDIT_COM_SHARD }
        return re
    }

    private fun hollywoodDBPathsList(): List<String> {
        val re = ArrayList<String>()
        DRIVES.mapTo(re) { it + HOLLYWOOD_SHARD }
        return re
    }
}
