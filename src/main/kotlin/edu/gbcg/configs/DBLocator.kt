/*
 * Copyright (c) 2018 Sean Grimes. All Rights Reserved.
 * License: MIT
 */

package edu.gbcg.configs

import edu.gbcg.utils.FileUtils

/**
 * Similar concept to RawDataLocator. The location of the databases will be different depending
 * on which machine this code is running on and whether or not testing_mode is enabled. This
 * class abstracts away the paths to the DB files.
 */
object DBLocator {
    private val drives = arrayOf("F", "G", "H", "I", "J", "K")
    private val subpath = ":/DBs/reddit/Submissions/${DataPaths.SUB_DB_PREFIX}.sqlite3"
    private val compath = ":/DBs/reddit/Comments/${DataPaths.COM_DB_PREFIX}.sqlite3"
    private val SubDBPath = ":/DBs/reddit/Submissions/"
    private val ComDBPath = ":/DBs/reddit/Comments/"

    /**
     * Get a list of absolute file paths to all reddit submission DBs
     * @return List of file paths to submission DBs if they exist, otherwise null
     */
    @JvmStatic fun redditSubsAbsolutePaths(): List<String> {
        return when(Finals.TESTING_MODE){
            true -> FileUtils.get().getAllFilePathsInDirWithPrefix("RS", getSubDBPath()[0])
            false -> subDBPathsList()
        }
    }

    /**
     * Get a list of absolute file paths to all reddit comment DBs
     * @return List of file paths to comment DBs if they exist, otherwise null
     */
    @JvmStatic fun redditComsAbsolutePaths(): List<String> {
        return when(Finals.TESTING_MODE){
            true -> FileUtils.get().getAllFilePathsInDirWithPrefix("RC", getComDBPath()[0])
            false -> comDBPathsList()
        }
    }

    /**
     * Get the path to the directory that holds the submission DBs. This path changes depending on
     * which machine this code is running on.
     * @return Absolute file path to the directories holding the submission databases
     */
    @JvmStatic fun getSubDBPath(): List<String> {
        return when(Finals.TESTING_MODE){
            true -> listOf(DataPaths.LOCAL_SUB_DB_PATH)
            false -> {
                val paths = ArrayList<String>()
                drives.mapTo(paths) { it + SubDBPath }
                paths
            }
        }
    }

    /**
     * Get the path to the directory that holds the comment DBs. This path changes depending on
     * which machine this code is running on.
     * @return Absolute file path to the directories holding the submission databases
     */
    @JvmStatic fun getComDBPath(): List<String> {
        return when(Finals.TESTING_MODE){
            true -> listOf(DataPaths.LOCAL_COM_DB_PATH)
            false -> {
                val paths = ArrayList<String>()
                drives.mapTo(paths) { it + ComDBPath }
                paths
            }
        }
    }

    /**
     * Build paths to the submission databases. Intended to be used when the DBs don't yet exist
     * but a path to the DBs is needed for creation.
     * @return List of absolute paths to DBs (that don't yet exist)
     */
    @JvmStatic fun buildSubDBPaths(): List<String> {
        return when(Finals.TESTING_MODE){
            true -> buildDBPaths(getSubDBPath()[0], DataPaths.SUB_DB_PREFIX)
            false -> subDBPathsList()
        }
    }

    /**
     * Build paths to the comment databases. Intended to be used when the DBs don't yet exist but
     * a path to the DBs is needed for creation.
     * @return List of absolute paths to the DBs (that don't yet exist)
     */
    @JvmStatic fun buildComDBPaths(): List<String> {
        return when(Finals.TESTING_MODE){
            true -> buildDBPaths(getComDBPath()[0], DataPaths.COM_DB_PREFIX)
            false -> comDBPathsList()
        }
    }

    /*
        ** NO JAVADOC **
        * Actually builds the paths to the DBs based on requested com/sub dir and their prefix
     */
    private fun buildDBPaths(db_dir: String, db_prefix: String): List<String> {
        var sbs = ArrayList<StringBuilder>()
        for(i in 0 until Finals.DB_SHARD_NUM){
            var sb = StringBuilder()
            sb.append(db_dir)
            sb.append(db_prefix)
            sb.append(i)
            sb.append(DataPaths.DB_POSTFIX)
            sbs.add(sb)
        }

        val paths = ArrayList<String>()
        sbs.mapTo(paths) { it.toString() }
        return paths
    }

    private fun subDBPathsList(): List<String> {
        val re = ArrayList<String>()
        // for-loop replacement, for each item in drives, add item + subpath to 're'
        drives.mapTo(re) { it + subpath }
        return re
    }

    private fun comDBPathsList(): List<String> {
        val re = ArrayList<String>()
        drives.mapTo(re) { it + compath }
        return re
    }
}
