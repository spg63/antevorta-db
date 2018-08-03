/*
 * Copyright (c) 2018 Sean Grimes. All Rights Reserved.
 * License: MIT
 */

package edu.antevortadb.dbInteraction.dbcreator

import edu.antevortadb.configs.Finals
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.IOException

@Suppress("ConvertSecondaryConstructorToPrimary")
abstract class JsonFacilitator: Facilitator {

    constructor(): super()

    // Json specific functions
    protected abstract fun populateJsonWorkers(): List<JsonPusher>

    override fun pushDataIntoDBs() {
        // Check if START_FRESH or ADD_NEW_DATA is set to true, if so continue, else return. Also check if
        // the proper data paths have been set, if not, set them.
        if(shouldFunctionReturnEarly()) return

        // For each json file, read it line by line, while reading start processing the data
        // Each iteration of the below while loop adds a line to a new worker thread to evenly share the data
        // across all DB shards
        for(json in this.dataAbsolutePaths){
            val totalLinesInFile = printFileInformationReturnTotalLinesInFile(json)

            var br: BufferedReader? = null
            val dbDumpLimit = Finals.DB_SHARD_NUM * Finals.DB_BATCH_LIMIT
            val f = File(json)
            var lineReadCounter = 0
            var totalLinesRead: Long = 0
            var writeTotalLinesRead = 1
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

                        totalLinesRead += lineReadCounter
                        lineReadCounter = 0
                        linesList.clear()

                        if(writeTotalLinesRead % 25 == 0) {
                            val readLines = totalLinesRead.toDouble()
                            val currentComplete = readLines / totalLinesInFile
                            val percentComplete = (currentComplete * 100).toInt()
                            logger.info("$percentComplete% done ${f.name}")
                        }
                        ++writeTotalLinesRead

                        // Give the linesList new ArrayLists to store next set of lines
                        for(j in 0 until Finals.DB_SHARD_NUM)
                            linesList.add(ArrayList())
                    }
                    line = br.readLine()
                }

                // There could be leftover json lines that don't get pushed due to not meeting the dbDumpLimit
                // amount of lines. Start up the workers again and push the remaining lines
                logger.info("Launching final JSON push for ${f.name}")
                totalLinesRead += lineReadCounter
                logger.info("Total lines read ${numberFormat.format(totalLinesRead)} for ${f.name}")
                letWorkersFly(linesList)
            }
            catch(e: IOException){
                logger.exception(e)
            }
            finally{
                if(br != null){
                    try{
                        br.close()
                    }
                    catch(e: IOException){
                        logger.exception(e)
                    }
                }
            }
        }

        // Create the indices on all shards. This happens on table creation and after batch inserts
        // for new data
        createIndices()
    }

    private fun letWorkersFly(lines: List<List<String>>) {
        val workers: List<JsonPusher> = populateJsonWorkers()
        for(i in 0 until Finals.DB_SHARD_NUM){
            workers[i].DB = dbAbsolutePaths[i]
            workers[i].jsonStrings = lines[i]
            workers[i].columns = dbColumnNames
            workers[i].tableName = dbTableName
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
                logger.exception(e)
            }
        }
    }
}
