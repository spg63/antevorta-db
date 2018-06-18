/*
 * Copyright (c) 2018 Sean Grimes. All Rights Reserved.
 * License: MIT
 */

package edu.gbcg.dbInteraction.dbcreator

import edu.gbcg.configs.Finals
import edu.gbcg.dbInteraction.DBCommon
import edu.gbcg.dbInteraction.DBWorker
import edu.gbcg.utils.FileUtils
import edu.gbcg.utils.TSL
import java.sql.Connection
import java.text.NumberFormat

@Suppress("LeakingThis", "ConvertSecondaryConstructorToPrimary")
abstract class Facilitator {
    protected var dbAbsolutePaths_: List<String>        // Path to the DBs once they exist
    protected val dbDirectoryPaths_: List<String>       // Path to the directory / directories that hold the DB shards
    protected val columnNames_: List<String>            // Names of the columns in the DB
    protected val dataTypes_: List<String>              // Type of data stored in the DB columns
    protected val dbPaths_: List<String>                // Paths to the DBs when they don't yet exist
    protected var dataAbsolutePaths_: List<String>      // Paths to the data files
    protected val tableName_: String                    // The name of the table in the DB
    protected val logger_: TSL = TSL.get()              // Instance of the logger
    protected val numberFormat_: NumberFormat           // Format number output for easier viewing
    protected val dataNamesOfInterest_: List<String>    // Name of csv column of json key we need

    constructor(){
        this.dbAbsolutePaths_               = getDBAbsolutePaths()
        this.dbDirectoryPaths_              = getDBDirectoryPaths()
        this.columnNames_                   = getColumnNames()
        this.dataTypes_                     = getDataTypes()
        this.dbPaths_                       = buildDBPaths()
        this.dataAbsolutePaths_             = getDataFileAbsolutePaths()
        this.tableName_                     = getTableName()
        this.numberFormat_                  = NumberFormat.getInstance()
        this.numberFormat_.isGroupingUsed   = true
        this.dataNamesOfInterest_           = getDataKeysOfInterest()
    }

    protected abstract fun buildDBPaths(): List<String>
    protected abstract fun getDataFileAbsolutePaths(): List<String>
    protected abstract fun getDBAbsolutePaths(): List<String>
    protected abstract fun getDBDirectoryPaths(): List<String>
    protected abstract fun getDataTypes(): List<String>
    protected abstract fun getColumnNames(): List<String>
    protected abstract fun getTableName(): String
    protected abstract fun createIndices()
    protected abstract fun dropIndices()
    protected abstract fun getDataAbsolutePathsForNewData(): List<String>
    protected abstract fun getDataKeysOfInterest(): List<String>

    abstract fun pushDataIntoDBs()
    // Creates a list of DataPushers (json, csv, other), passes a list of lines that pusher is supposed to parse
    // and push into the DB, starts each pusher in a thread, lets them work, joins them.
    protected abstract fun letWorkersFly(lines: List<List<String>>)

    // Used by pushDataIntoDBs(), regardless of data type, to check if the function should continue.
    // It will shortcirtuit pushData if the data already exists or START_FRESH is false and ADD_NEW_DATA is false
    // NOTE: The idea really is the stuff going on in this function needs to be enforced across all sub
    // Facilitators so this fucntion moves these important checks into the base Facilitator class
    protected fun shouldFunctionReturnEarly(): Boolean {
        // Early exit if we're not pushing data into the DBs
        if(!Finals.START_FRESH && !Finals.ADD_NEW_DATA) return true
        if(this.dbAbsolutePaths_.isEmpty())
            this.dbAbsolutePaths_ = getDBAbsolutePaths()

        if(this.dataAbsolutePaths_.isEmpty()){
            when{
                Finals.START_FRESH -> this.dataAbsolutePaths_ = getDataFileAbsolutePaths()
                Finals.ADD_NEW_DATA -> this.dataAbsolutePaths_ = getDataAbsolutePathsForNewData()
                else -> logger_.logAndKill("Facilitator.shouldFunctionReturnEarly -- Not START_FRESH or ADD_NEW_DATA")
            }
        }

        // Used for logging when adding new data to the DB shards
        if(Finals.ADD_NEW_DATA)
            this.dataAbsolutePaths_.forEach{logger_.info("Pulling new data from $it")}

        return false
    }

    fun createDBs() {
        if(!Finals.START_FRESH)
            logger_.logAndKill("Called createDBs when Finals.START_FRESH was false. Check your logic.")

        // Check if the DBs exist.
        if(this.dbAbsolutePaths_.isEmpty())
            this.dbAbsolutePaths_ = ArrayList()
        val dbs_exist = this.dbAbsolutePaths_.size == Finals.DB_SHARD_NUM

        // The DBs exist but we want to start fresh, get rid of them
        if(dbs_exist){
            logger_.warn("DB shards exist && starting fresh")
            logger_.info("Dropping table(s) in DB shards")
            val sql = "drop table if exists ${this.tableName_};"
            // For each db in the list, drop the table
            this.dbAbsolutePaths_.forEach{ DBCommon.delete(it, sql) }
        }

        // The DBs don't exist, create the empty sqlite files
        else if(!dbs_exist){
            logger_.info("Creating DB shard paths and initializing DB files")
            // Create the directories that hold the DBs
            this.dbDirectoryPaths_.forEach{ FileUtils.get().checkAndCreateDir(it) }
            // Build the paths to the Dbs so they can be created
            this.dbPaths_.forEach{
                val conn = DBCommon.connect(it)
                DBCommon.disconnect(conn)
            }
            // Now that the DBs exist, populate the absolute paths list
            this.dbAbsolutePaths_ = getDBAbsolutePaths()
        }
        else
            logger_.logAndKill("createDBs; somehow the DBs exist and don't exist. What's that cat joke?")

        // Create the table schema
        val sb = StringBuilder()
        sb.append("create table if not exists ${this.tableName_}(")
        for(i in 0 until this.columnNames_.size){
            sb.append(this.columnNames_[i])
            sb.append(this.dataTypes_[i])
        }
        sb.append(");")
        val sql = sb.toString()

        logger_.info("Writing table to DB shards")
        this.dbAbsolutePaths_.forEach{ DBCommon.insert(it, sql) }
        logger_.info("DBs have been created")
    }

    fun createDBIndex(columnName: String, indexName: String) {
        logger_.info("Creating $indexName on column $columnName")
        val workers = ArrayList<Thread>()
        val conns = ArrayList<Connection>()
        val sql = DBCommon.getDBIndexSQLStatement(this.tableName_, columnName, indexName)
        if(this.dbAbsolutePaths_.isEmpty())
            this.dbAbsolutePaths_ = getDBAbsolutePaths()

        // For each db in dbAbsolutePaths_ create a connection and put it in conns
        this.dbAbsolutePaths_.mapTo(conns) { DBCommon.connect(it) }

        for(i in 0 until conns.size){
            workers.add(Thread(DBWorker(conns[i], sql)))
            workers[i].start()
        }
        try{
            for(i in 0 until workers.size)
                workers[i].join()
        }
        catch(e: InterruptedException){
            logger_.exception(e)
        }

        conns.forEach{ DBCommon.disconnect(it) }
    }

    fun dropDBIndices(indexName: String){
        logger_.info("Dropping $indexName from ${this.tableName_}")
        // Get a connection to each DB shard
        val conns = ArrayList<Connection>()

        // The DB workers, run it in parallel
        val workers = ArrayList<Thread>()

        // Get a connection to each DB and put the connections in conns list
        this.dbAbsolutePaths_.mapTo(conns){DBCommon.connect(it)}

        // Get the SQL statement
        val sql = DBCommon.getDropDBIndexSQLStatement(indexName)

        // Launch the workers
        for(i in 0 until conns.size){
            workers.add(Thread(DBWorker(conns[i], sql)))
            workers[i].start()
        }
        try{
            for(i in 0 until workers.size)
                workers[i].join()
        }
        catch(e: InterruptedException){
            logger_.exception(e)
        }

        // Close all the connections
        conns.forEach{ DBCommon.disconnect(it) }
    }

    // Push new data into the DB shards
    fun pushNewData(){
        // Batch inserting large data on tables with a lot of indices is slow. Drop them and re-create at the end
        dropIndices()

        // Clear the existing list. Note: clear can't be called on a "List" so just replace it with a new one
        this.dataAbsolutePaths_ = ArrayList()

        // Get the path(s) to the new json file(s)
        this.dataAbsolutePaths_ = getDataAbsolutePathsForNewData()

        // Now that the paths have been reset the new data can be pushed in the DB shards
        pushDataIntoDBs()
    }
}
