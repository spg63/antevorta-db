/*
 * Copyright (c) 2018 Sean Grimes. All Rights Reserved.
 * License: MIT
 */

package edu.antevortadb.dbInteraction.dbcreator

import edu.antevortadb.configs.Finals
import edu.antevortadb.dbInteraction.DBCommon
import edu.antevortadb.dbInteraction.DBWorker
import javalibs.FileUtils
import javalibs.TSL
import java.io.File
import java.sql.Connection
import java.text.NumberFormat

@Suppress("LeakingThis", "ConvertSecondaryConstructorToPrimary",
        "MemberVisibilityCanBePrivate", "ConstantConditionIf")
abstract class Facilitator {
    protected var dbAbsolutePaths: List<String>     // Path to the DBs once they exist
    protected val dbDirectoryPaths: List<String>    // Path to directories tht hold shards
    protected val dbColumnNames: List<String>       // Names of the columns in the DB
    protected val columnDataTypes: List<String>     // Type of data stored in DB columns
    protected val dbPaths: List<String>             // Paths to DBs when they don't exist
    protected var dataAbsolutePaths: List<String>   // Paths to the data files
    protected val dbTableName: String               // The name of the table in the DB
    protected val logger: TSL = TSL.get()           // Instance of the logger
    protected val numberFormat: NumberFormat        // Format num output, easier viewing
    protected val dataNamesOfInterest: List<String> // Name of csv column of json key

    constructor(){
        this.dbAbsolutePaths               = getDBAbsolutePaths()
        this.dbDirectoryPaths              = getDBDirectoryPaths()
        this.dbColumnNames                 = getColumnNames()
        this.columnDataTypes               = getDataTypes()
        this.dbPaths                       = buildDBPaths()
        this.dataAbsolutePaths             = getDataFileAbsolutePaths()
        this.dbTableName                   = getTableName()
        this.numberFormat                  = NumberFormat.getInstance()
        this.numberFormat.isGroupingUsed   = true
        this.dataNamesOfInterest           = getDataKeysOfInterest()
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

    // Used by pushDataIntoDBs(), regardless of data type, to check if the function should
    // continue. It will shortcirtuit pushData if the data already exists or START_FRESH
    // is false and ADD_NEW_DATA is false
    // NOTE: The idea really is the stuff going on in this function needs to be enforced
    // across all sub Facilitators so this fucntion moves these important checks into the
    // base Facilitator class
    protected fun shouldFunctionReturnEarly(): Boolean {
        // Early exit if we're not pushing data into the DBs
        if(!Finals.START_FRESH && !Finals.ADD_NEW_DATA) return true
        if(this.dbAbsolutePaths.isEmpty())
            this.dbAbsolutePaths = getDBAbsolutePaths()

        if(this.dataAbsolutePaths.isEmpty()){
            when{
                Finals.START_FRESH -> this.dataAbsolutePaths = getDataFileAbsolutePaths()
                Finals.ADD_NEW_DATA -> this.dataAbsolutePaths =
                        getDataAbsolutePathsForNewData()
                else -> logger.die("Facilitator.shouldFunctionReturnEarly " +
                        "!START_FRESH, !ADD_NEW_DATA")
            }
        }

        // Used for logging when adding new data to the DB shards
        if(Finals.ADD_NEW_DATA)
            this.dataAbsolutePaths.forEach{logger.info("Pulling new data from $it")}

        return false
    }

    fun createDBs() {
        if(!Finals.START_FRESH)
            logger.die("Called createDBs when Finals.START_FRESH was false. " +
                    "Check your logic.")

        // Check if the DBs exist.
        if(this.dbAbsolutePaths.isEmpty())
            this.dbAbsolutePaths = ArrayList()
        var doDBsExist = this.dbAbsolutePaths.size == Finals.DB_SHARD_NUM

        // Add an extra check to make sure they really exist on disk
        if(doDBsExist) {
            val f = File(this.dbAbsolutePaths[0])
            if (!f.exists())
                doDBsExist = false
        }

        // The DBs exist but we want to start fresh, get rid of them
        if(doDBsExist){
            logger.warn("DB shards exist && starting fresh")
            logger.info("Dropping table(s) in DB shards")
            val sql = "drop table if exists ${this.dbTableName};"
            // For each db in the list, drop the table
            this.dbAbsolutePaths.forEach{ DBCommon.delete(it, sql) }
        }

        // The DBs don't exist, create the empty sqlite files
        else if(!doDBsExist){
            logger.info("Creating DB shard paths and initializing DB files")
            // Create the directories that hold the DBs
            this.dbDirectoryPaths.forEach{ FileUtils.get().checkAndCreateDir(it) }
            // Build the paths to the Dbs so they can be created
            this.dbPaths.forEach{
                val conn = DBCommon.connect(it)
                DBCommon.disconnect(conn)
            }
            // Now that the DBs exist, populate the absolute paths list
            this.dbAbsolutePaths = getDBAbsolutePaths()
        }
        else
            logger.die("createDBs; somehow the DBs exist and don't exist. " +
                    "What's that cat joke?")

        createNewTable()
    }

    fun createNewTableInExistingDBs(){
        if(!Finals.START_FRESH)
            logger.die("Called createNewTableInExistingDBs when " +
                    "Finals.START_FRESH was false")

        val doDBsExist = this.dbAbsolutePaths.size == Finals.DB_SHARD_NUM
        if(!doDBsExist)
            logger.die("createNewTableInExistingDBs -- can't find existing DBs")

        createNewTable()
    }

    protected fun createDBIndex(columnName: String, indexName: String) {
        logger.info("Creating $indexName on column $columnName")
        val workers = ArrayList<Thread>()
        val conns = ArrayList<Connection>()
        val sql = DBCommon.getDBIndexSQLStatement(this.dbTableName, columnName, indexName)
        if(this.dbAbsolutePaths.isEmpty())
            this.dbAbsolutePaths = getDBAbsolutePaths()

        // For each db in dbAbsolutePaths create a connection and put it in conns
        this.dbAbsolutePaths.mapTo(conns) { DBCommon.connect(it) }

        for(i in 0 until conns.size){
            workers.add(Thread(DBWorker(conns[i], sql)))
            workers[i].start()
        }
        try{
            for(i in 0 until workers.size)
                workers[i].join()
        }
        catch(e: InterruptedException){
            logger.exception(e)
        }

        conns.forEach{ DBCommon.disconnect(it) }
    }

    protected fun dropDBIndex(indexName: String){
        logger.info("Dropping $indexName from ${this.dbTableName}")
        // Get a connection to each DB shard
        val conns = ArrayList<Connection>()

        // The DB workers, run it in parallel
        val workers = ArrayList<Thread>()

        // Get a connection to each DB and put the connections in conns list
        this.dbAbsolutePaths.mapTo(conns){DBCommon.connect(it)}

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
            logger.exception(e)
        }

        // Close all the connections
        conns.forEach{ DBCommon.disconnect(it) }
    }

    // Push new data into the DB shards
    fun pushNewData(){
        // Batch inserting large data on tables with a lot of indices is slow. Drop them
        // and re-create at the end
        dropIndices()

        // Clear the existing list. Note: clear can't be called on a "List" so just
        // replace it with a new one
        this.dataAbsolutePaths = ArrayList()

        // Get the path(s) to the new json file(s)
        this.dataAbsolutePaths = getDataAbsolutePathsForNewData()

        // Now that the paths have been reset the new data can be pushed in the DB shards
        pushDataIntoDBs()
    }

    private fun createNewTable(){
        // Drop the table if it already exists
        val dropsql = "drop table if exists ${this.dbTableName};"
        // For each db in the list, drop the table
        this.dbAbsolutePaths.forEach{ DBCommon.delete(it, dropsql) }

        // Create the table schema
        val sb = StringBuilder()
        sb.append("create table if not exists ${this.dbTableName}(")
        for(i in 0 until this.dbColumnNames.size){
            sb.append(this.dbColumnNames[i])
            sb.append(this.columnDataTypes[i])
        }
        sb.append(");")
        val sql = sb.toString()
        logger.info("Writing ${this.dbTableName} to DB shards")
        this.dbAbsolutePaths.forEach{ DBCommon.insert(it, sql) }
        logger.info("${this.dbTableName} has been created")
    }

    // Note this returns total lines as a double as the value is used to print % remaining
    // of file, this means that calculation doesn't need to do a cast every time it's made
    protected fun printFileInformationReturnTotalLinesInFile(filename: String): Double {
        val f = File(filename)
        logger.info("Counting number of lines in ${f.name}")
        val totalLines = FileUtils.get().lineCount(filename)
        val totalLinesInFile = totalLines.toDouble()
        logger.info("Reading ${f.name}")
        logger.info("${f.name} has ${numberFormat.format(totalLinesInFile)} lines")
        return totalLinesInFile
    }
}
