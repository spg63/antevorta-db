/*
 * Copyright (c) 2018 Sean Grimes. All Rights Reserved.
 * License: MIT
 */

package edu.gbcg.dbInteraction.dbcreator

import edu.gbcg.configs.Finals
import edu.gbcg.utils.FileUtils
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.IOException

abstract class JsonFacilitator: Facilitator {
    protected val jsonKeysOfInterest_: List<String> // JSON keys we care about grabbing for the DB

    constructor(): super(){
        this.jsonKeysOfInterest_ = getJsonKeysOfInterest()
    }

    // Json specific functions
    protected abstract fun getJsonKeysOfInterest(): List<String>
    protected abstract fun populateJsonWorkers(): List<JsonPusher>

    override fun pushDataIntoDBs() {
        // Early exit if we're not pushing data
        if(!Finals.START_FRESH && !Finals.ADD_NEW_DATA) return

        if (this.dbAbsolutePaths_.isEmpty())
            this.dbAbsolutePaths_ = getDBAbsolutePaths()

        if (this.dataAbsolutePaths_.isEmpty()) {
            when {
                Finals.START_FRESH -> this.dataAbsolutePaths_ = getDataFileAbsolutePaths()
                Finals.ADD_NEW_DATA -> this.dataAbsolutePaths_ = getDataAbsolutePathsForNewData()
                else -> logger_.logAndKill("Facilitator.pushDataIntoDBs -- Not START_FRESH or ADD_NEW_DATA")
            }
        }

        // Just for some logging when adding new data to the DB shards
        if(Finals.ADD_NEW_DATA)
            this.dataAbsolutePaths_.forEach{logger_.info("Pulling new data from $it")}

        // For each json file, read it line by line, while reading start processing the data
        // Each iteration of the loop adds a line to a new worker thread to evenly share the data across all DB shards
        for(json in this.dataAbsolutePaths_){
            val f = File(json)
            logger_.info("Counting number of lines in ${f.name}")
            val total_lines = FileUtils.get().lineCount(json)
            val total_lines_in_file = total_lines.toDouble()
            logger_.info("Reading ${f.name}")
            logger_.info("${f.name} has ${numberFormat_.format(total_lines_in_file)} lines")

            var br: BufferedReader? = null
            val dbDumpLimit = Finals.DB_SHARD_NUM * Finals.DB_BATCH_LIMIT
            var lineReadCounter = 0
            var total_lines_read: Long = 0
            var write_total_lines_read = 1
            var whichWorker = 0
            val linesList: ArrayList<ArrayList<String>> = ArrayList()
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

                        if(write_total_lines_read % 25 == 0) {
                            val readLines = total_lines_read.toDouble()
                            val currentComplete = readLines / total_lines_in_file
                            val percentComplete = (currentComplete * 100).toInt()
                            logger_.info("$percentComplete% done ${f.name}")
                        }
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

        // Create the indices on all shards. This happens on table creation and after batch inserts for new data
        createIndices()
    }

    override fun letWorkersFly(lines: List<List<String>>) {
        val workers: List<JsonPusher> = populateJsonWorkers()
        for(i in 0 until Finals.DB_SHARD_NUM){
            workers[i].DB = dbAbsolutePaths_[i]
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