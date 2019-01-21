/*
 * Copyright (c) 2018 Sean Grimes. All Rights Reserved.
 * License: MIT
 */

@file:Suppress("MayBeConstant", "MemberVisibilityCanBePrivate")

package edu.antevortadb.configs

import javalibs.FileUtils
import java.io.File

/**
 * Similar concept to RawDataLocator. The location of the databases will be different depending
 * on which machine this code is running on and whether or not testing_mode is enabled. This
 * class abstracts away the paths to the DB files.
 *
 * NOTE: Functions below are split into groupings of however many different DBs exist.
 * Each type of DB needs these functions for access. However many paths are found below
 * in "Paths to the directories...." will tell you how many different functions should
 * be in each grouping.
 */
object DBLocator {
    val futils = FileUtils.get()
    val SEP: String = File.separator
    /* ----- List of drive letters on the research machine that stores the db shards -- */
    val DRIVES = arrayOf("/mnt/DBA/", "/mnt/DBB/",
            "/mnt/DBC/", "/mnt/DBD/",
            "/mnt/DBE/", "/mnt/DBF/")

    /* ----- Paths to the directories that hold the DB shards without the drive letter
    prefix ---------------------------------------------------------------------------- */
    val REDDIT_SUB_DB_DIR_PATH = "DBs${SEP}Reddit${SEP}Submissions${SEP}"
    val REDDIT_COM_DB_DIR_PATH = "DBs${SEP}Reddit${SEP}Comments${SEP}"
    val HOLLYWOOD_DB_DIR_PATH = "DBs${SEP}Hollywood${SEP}"

    /* ----- Paths to the DB shards without the drive letter prefix ------------------- */
    val REDDIT_SUB_SHARD =
            "$REDDIT_SUB_DB_DIR_PATH${DataPaths.REDDIT_SUB_DB}${DataPaths.DBEXT}"
    val REDDIT_COM_SHARD =
            "$REDDIT_COM_DB_DIR_PATH${DataPaths.REDDIT_COM_DB}${DataPaths.DBEXT}"
    val HOLLYWOOD_SHARD =
            "$HOLLYWOOD_DB_DIR_PATH${DataPaths.HOLLYWOOD_DB}${DataPaths.DBEXT}"

    // -----------------------------------------------------------------------------------

    /**
     * Get a list of absolute file paths to all reddit submission DBs
     * @return List of file paths to submission DBs if they exist, otherwise null
     */
    fun redditSubsAbsolutePaths(): List<String> {
        return when (Finals.TESTING_MODE) {
            true -> futils.getAllFilePathsInDirWithPrefix(DataPaths.REDDIT_SUB_DB,
                    getSubDBDirectoryPath()[0])
            false -> subDBPathsList()
        }
    }

    /**
     * Get a list of absolute file paths to all reddit comment DBs
     * @return List of file paths to comment DBs if they exist, otherwise null
     */
    fun redditComsAbsolutePaths(): List<String> {
        return when (Finals.TESTING_MODE) {
            true -> futils.getAllFilePathsInDirWithPrefix(DataPaths.REDDIT_COM_DB,
                    getComDBDirectoryPath()[0])
            false -> comDBPathsList()
        }
    }

    /**
     * Get a list of absolute file paths to all hollywood DBs
     * $return List of file paths to the hollywood DBs if they exist, otherwise null
     */
    fun hollywoodAbsolutePaths(): List<String> {
        return when (Finals.TESTING_MODE) {
            true -> {
                // Necessary to ignore the sqlite3 journal files when a DB is currently
                // in use
                val allFiles = futils.getAllFilePathsInDirWithPrefix(
                        DataPaths.HOLLYWOOD_DB, getHollywoodDBDirectoryPaths()[0])
                val correctFiles = ArrayList<String>()
                for (file in allFiles) {
                    if (file.endsWith(Finals.DB_TYPE_EXT))
                        correctFiles.add(file)
                }

                return correctFiles
            }
            false -> hollywoodDBPathsList()
        }
    }

    // -----------------------------------------------------------------------------------

    /**
     * Get the path to the directory that holds the submission DBs. This path changes
     * depending on which machine this code is running on.
     * @return Absolute file path to the directories holding the submission databases
     */
    fun getSubDBDirectoryPath(): List<String> {
        return when (Finals.TESTING_MODE) {
            true -> listOf(DataPaths.LOCAL_REDDIT_SUB_DB_PATH)
            false -> buildPathsWithDriveLetter(REDDIT_SUB_DB_DIR_PATH)
        }
    }

    /**
     * Get the path to the directory that holds the comment DBs. This path changes
     * depending on which machine this code is running on.
     * @return Absolute file path to the directories holding the comments databases
     */
    fun getComDBDirectoryPath(): List<String> {
        return when (Finals.TESTING_MODE) {
            true -> listOf(DataPaths.LOCAL_REDDIT_COM_DB_PATH)
            false -> buildPathsWithDriveLetter(REDDIT_COM_DB_DIR_PATH)
        }
    }

    /**
     * Get a path to the directory that holds the hollywood DBs. This path changes
     * depending on which machine this code is running on.
     * @return Absolute file path to the directories holding the hollywood databases
     */
    fun getHollywoodDBDirectoryPaths(): List<String> {
        return when (Finals.TESTING_MODE) {
            true -> listOf(DataPaths.LOCAL_HOLLYWOOD_DB_PATH)
            false -> buildPathsWithDriveLetter(HOLLYWOOD_DB_DIR_PATH)
        }
    }

    // -----------------------------------------------------------------------------------

    /**
     * Build paths to the submission databases. Intended to be used when the DBs don't yet
     * exist but a path to the DBs is needed for creation.
     * @return List of absolute paths to DBs (that don't yet exist)
     */
    fun buildSubDBPaths(): List<String> {
        return when (Finals.TESTING_MODE) {
            true -> buildDBPaths(getSubDBDirectoryPath()[0], DataPaths.REDDIT_SUB_DB)
            false -> subDBPathsList()
        }
    }

    /**
     * Build paths to the comment databases. Intended to be used when the DBs don't yet
     * exist but a path to the DBs is needed for creation.
     * @return List of absolute paths to the DBs (that don't yet exist)
     */
    fun buildComDBPaths(): List<String> {
        return when (Finals.TESTING_MODE) {
            true -> buildDBPaths(getComDBDirectoryPath()[0], DataPaths.REDDIT_COM_DB)
            false -> comDBPathsList()
        }
    }

    /**
     * Build paths to the hollywood databases. intended to be used when the DBs don't yet
     * exist but a path to the DBs is needed for creation.
     * @return List of absolute paths to the DBs (that don't yet exist)
     */
    fun buildHollywoodDBPaths(): List<String> {
        return when (Finals.TESTING_MODE) {
            true -> buildDBPaths(getHollywoodDBDirectoryPaths()[0],
                    DataPaths.HOLLYWOOD_DB)
            false -> hollywoodDBPathsList()
        }
    }

    // -----------------------------------------------------------------------------------

    /*
        Just adds the drive letters to the path. This is implemented below with
         buildPathsWithDriveLetter and the above functions just pass along the proper
         path to the shard depending on DB type
    */

    // Build paths for the reddit submission db shards
    private fun subDBPathsList(): List<String> =
            buildPathsWithDriveLetter(REDDIT_SUB_SHARD)

    // Build paths for the reddit comment db shards
    private fun comDBPathsList(): List<String> =
            buildPathsWithDriveLetter(REDDIT_COM_SHARD)

    // Build paths for the hollywood db shards
    private fun hollywoodDBPathsList(): List<String> =
            buildPathsWithDriveLetter(HOLLYWOOD_SHARD)

    // -----------------------------------------------------------------------------------

    /*
        ** NO JAVADOC **
        * Actually builds the paths to the DBs based on requested com/sub dir and their
        * prefix when the DBs don't exist
     */
    private fun buildDBPaths(db_dir: String, db_prefix: String): List<String> {
        val sbs = ArrayList<StringBuilder>()
        for (i in 0 until Finals.DB_SHARD_NUM) {
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

    /*
        ** NO JAVADOC **
        * Returns a list of strings with the drive letters prepended to them. Drive
        * letters come from the above DRIVES list
     */
    private fun buildPathsWithDriveLetter(thePath: String): List<String> {
        val re = ArrayList<String>()
        DRIVES.mapTo(re) { it + thePath }
        return re
    }
}
