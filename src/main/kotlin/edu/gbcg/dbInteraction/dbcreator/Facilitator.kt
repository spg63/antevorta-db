/*
 * Copyright (c) 2018 Sean Grimes. All Rights Reserved.
 * License: MIT
 */

package edu.gbcg.dbInteraction.dbcreator

import edu.gbcg.configs.Finals
import edu.gbcg.dbInteraction.DBCommon
import edu.gbcg.dbInteraction.DBWorker
import edu.gbcg.dbInteraction.dbcreator.reddit.JsonPusher
import edu.gbcg.utils.FileUtils
import edu.gbcg.utils.TSL
import java.io.*
import java.sql.Connection
import java.text.NumberFormat

abstract class Facilitator {
    protected var DBAbsolutePaths_: List<String>        // Path to the DBs once they exist
    protected val DBDirectoryPaths_: List<String>       // Path to the directory / directories that hold the DB shards
    protected val columnNames_: List<String>            // Names of the columns in the DB
    protected val dataTypes_: List<String>              // Type of data stored in the DB columns
    protected val DBPaths_: List<String>                // Paths to the DBs when they don't yet exist
    protected var jsonAbsolutePaths_: List<String>      // Paths to the json files
    protected val jsonKeysOfInterest_: List<String>     // JSON keys we care about grabbing for the DB
    protected val tableName_: String                    // The name of the table in the DB
    protected val logger_ = TSL.get()                   // Instance of the logger
    protected val numberFormat_: NumberFormat           // Format number output for easier viewing

    constructor(){
        this.DBAbsolutePaths_               = getDBAbsolutePaths()
        this.DBDirectoryPaths_              = getDBDirectoryPaths()
        this.columnNames_                   = getColumnNames()
        this.dataTypes_                     = getDataTypes()
        this.DBPaths_                       = buildDBPaths()
        this.jsonAbsolutePaths_             = getJsonAbsolutePaths()
        this.jsonKeysOfInterest_            = getJsonKeysOfInterest()
        this.tableName_                     = getTableName()
        this.numberFormat_                  = NumberFormat.getInstance()
        this.numberFormat_.isGroupingUsed   = true
    }

    protected abstract fun buildDBPaths(): List<String>
    protected abstract fun getJsonAbsolutePaths(): List<String>
    protected abstract fun getDBAbsolutePaths(): List<String>
    protected abstract fun getDBDirectoryPaths(): List<String>
    protected abstract fun getDataTypes(): List<String>
    protected abstract fun populateJsonWorkers(): List<JsonPusher>
    protected abstract fun getJsonKeysOfInterest(): List<String>
    protected abstract fun getColumnNames(): List<String>
    protected abstract fun getTableName(): String
    protected abstract fun createIndices()

    fun createDBs() {
        // Check if the DBs exist.
        if(this.DBAbsolutePaths_ == null)
            this.DBAbsolutePaths_ = ArrayList()
        val dbs_exist = this.DBAbsolutePaths_.size == Finals.DB_SHARD_NUM

        // Early exit if the DBs exist and we don't want to start fresh
        if(dbs_exist && !Finals.START_FRESH)
            return

        // The DBs exist but we want to start fresh, get rid of them
        if(dbs_exist && Finals.START_FRESH){
            logger_.warn("DB shards exist && starting fresh")
            logger_.info("Dropping table in DB shards")
            val sql = "drop table if exists ${this.tableName_};"
            // For each db in the list, drop the table
            this.DBAbsolutePaths_.forEach{ DBCommon.delete(it, sql) }
        }

        // The DBs don't exist, create the empty sqlite files
        if(!dbs_exist){
            logger_.info("Creating DB shard paths and initializing DB files")
            // Create the directories that hold the DBs
            this.DBDirectoryPaths_.forEach{ FileUtils.get().checkAndCreateDir(it) }
            // Build the paths to the Dbs so they can be created
            this.DBPaths_.forEach{
                val conn = DBCommon.connect(it)
                DBCommon.disconnect(conn)
            }
            // Now that the DBs exist, populate the absolute paths list
            this.DBAbsolutePaths_ = getDBAbsolutePaths()
        }

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
        this.DBAbsolutePaths_.forEach{ DBCommon.insert(it, sql) }
        logger_.info("DBs have been created")
    }

    fun pushJSONDataIntoDBs() {
        // Early exit if we're not pushing data
        if(!Finals.START_FRESH) return

        if(this.DBAbsolutePaths_.isEmpty())
            this.DBAbsolutePaths_ = getDBAbsolutePaths()
        if(this.jsonAbsolutePaths_.isEmpty())
            this.jsonAbsolutePaths_ = getJsonAbsolutePaths()

        // For each json file, read it line by line, while reading start processing the data
        // Each iteration of the loop adds a line to a new worker thread to evenly share the data across all DB shards
        for(json in this.jsonAbsolutePaths_){
            val f = File(json)
            logger_.info("Reading ${f.name}")

            var br: BufferedReader? = null
            val dbDumpLimit = Finals.DB_SHARD_NUM * Finals.DB_BATCH_LIMIT
            var lineReadCounter = 0
            var total_lines_read: Long = 0
            var write_total_lines_read = 1
            var whichWorker = 0
            var linesList: ArrayList<ArrayList<String>> = ArrayList()
            for(i in 0 until Finals.DB_SHARD_NUM)
                linesList.add(ArrayList())

            try{
                br = BufferedReader(FileReader(json))
                var line = br.readLine()
                while(line != null){
                    ++lineReadCounter
                    linesList[whichWorker].add(line)

                    // Increment the worker number so we're evenly distributing lines to the workers
                    ++whichWorker
                    if(whichWorker >= Finals.DB_SHARD_NUM)
                        whichWorker = 0

                    // Limit before dumping data to the DB, start the threads and perform the dump
                    if(lineReadCounter >= dbDumpLimit){
                        letWorkersFly(linesList)

                        total_lines_read += lineReadCounter
                        lineReadCounter = 0
                        linesList.clear()

                        if(write_total_lines_read % 40 == 0)
                            logger_.info("${numberFormat_.format(total_lines_read)} lines read from ${f.name}")
                        ++write_total_lines_read

                        // Give the linesList new ArrayLists to store next set of lines
                        for(j in 0 until Finals.DB_SHARD_NUM)
                            linesList.add(ArrayList())
                    }
                    line = br.readLine()
                }

                // There could be leftover json lines that don't get pushed due to not meeting the dbDumpLimit amount
                // of lines. Start up the workers again and push the remaining lines
                logger_.info("Launching final JSON push for ${f.name}")
                total_lines_read += lineReadCounter
                logger_.info("Total lines read ${numberFormat_.format(total_lines_read)} for ${f.name}")
                letWorkersFly(linesList)
            }
            catch(e: IOException){
                logger_.exception(e)
            }
            finally{
                if(br != null){
                    try{
                        br.close()
                    }
                    catch(e: IOException){
                        logger_.exception(e)
                    }
                }
            }
        }
        createIndices()
    }

    fun createDBIndex(columnName: String, indexName: String) {
        logger_.info("Creating $indexName on column $columnName")
        var workers = ArrayList<Thread>()
        val conns = ArrayList<Connection>()
        val sql = DBCommon.getDBIndexSQLStatement(this.tableName_, columnName, indexName)
        if(this.DBAbsolutePaths_ == null)
            this.DBAbsolutePaths_ = getDBAbsolutePaths()

        // For each db in DBAbsolutePaths_ create a connection and put it in conns
        this.DBAbsolutePaths_.mapTo(conns) { DBCommon.connect(it) }

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

    private fun letWorkersFly(lines: List<List<String>>) {
        val workers: List<JsonPusher> = populateJsonWorkers()
        for(i in 0 until Finals.DB_SHARD_NUM){
            workers[i].DB = DBAbsolutePaths_[i]
            workers[i].JSONStrings = lines[i]
            workers[i].columns = columnNames_
            workers[i].tableName = tableName_
        }

        val threads: MutableList<Thread> = ArrayList()
        for(i in 0 until Finals.DB_SHARD_NUM){
            threads.add(Thread(workers[i]))
            threads[i].start()
        }

        for(i in 0 until Finals.DB_SHARD_NUM){
            try{
                threads[i].join()
            }
            catch(e: InterruptedException){
                logger_.exception(e)
            }
        }
    }
}
