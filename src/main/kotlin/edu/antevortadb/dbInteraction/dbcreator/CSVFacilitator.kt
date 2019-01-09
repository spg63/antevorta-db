/*
 * Copyright (c) 2018 Sean Grimes. All Rights Reserved.
 * License: MIT
 */

package edu.antevortadb.dbInteraction.dbcreator

import edu.antevortadb.configs.Finals
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.apache.commons.csv.CSVRecord
import java.io.File
import java.io.FileReader

@Suppress("ConvertSecondaryConstructorToPrimary", "MemberVisibilityCanBePrivate",
        "HasPlatformType")
abstract class CSVFacilitator: Facilitator {
    protected var parseFormat = CSVFormat.DEFAULT!!.withQuote(null)
    protected var parser: CSVParser? = null

    constructor(): super()

    // CSV specific functions
    protected abstract fun populateCSVWorkers(): List<CSVPusher>

    override fun pushDataIntoDBs() {
        if(shouldFunctionReturnEarly()) return

        // For each CSV file, need to read each line of the file, turn it into a record,
        // and give the records to the CSV pushers to parse them and push them into
        // the DBs
        for(csv in this.dataAbsolutePaths){
            val totalLinesInFile = printFileInformationReturnTotalLinesInFile(csv)
            // NOTE: The below code is replaced by the function call above, remove the
            // commented code

            val dbDumpLimit = Finals.DB_SHARD_NUM * Finals.DB_BATCH_LIMIT
            val f = File(csv)
            var lineReadCounter = 0
            var totalLinesRead: Long = 0
            var writeTotalLinesRead = 1
            var whichWorker = 0
            val recordsList: ArrayList<ArrayList<CSVRecord>> = ArrayList()
            for(i in 0 until Finals.DB_SHARD_NUM)
                recordsList.add(ArrayList())

            try{
                parser = CSVParser.parse(FileReader(csv), parseFormat)
                for(record in parser!!){
                    ++lineReadCounter
                    recordsList[whichWorker].add(record)

                    ++whichWorker
                    if(whichWorker >= Finals.DB_SHARD_NUM)
                        whichWorker = 0

                    if(lineReadCounter >= dbDumpLimit){
                        letWorkersFly(recordsList)

                        totalLinesRead += lineReadCounter
                        lineReadCounter = 0
                        recordsList.clear()

                        if(writeTotalLinesRead % 10 == 0){
                            val readLines = totalLinesRead.toDouble()
                            val currentComplete = readLines / totalLinesInFile
                            val percentComplete = (currentComplete * 100).toInt()
                            logger.info("$percentComplete% done ${f.name}")
                        }
                        ++writeTotalLinesRead

                        for(j in 0 until Finals.DB_SHARD_NUM)
                            recordsList.add(ArrayList())
                    }
                }
                // There could be leftover csv records that don't get pushed due to not
                // meeting the dbDumpLimit amount of lines. Start up the workers again and
                // push the remaining lines
                logger.info("Launching final CSV push for ${f.name}")
                totalLinesRead += lineReadCounter
                logger.info("Total lines read " +
                        "${numberFormat.format(totalLinesRead)} for ${f.name}")
                letWorkersFly(recordsList)
            }
            catch(e: Exception){
                logger.logAndKill(e)
            }
        }
        // Create the indices on all shards. This happens on table creation and after
        // batch inserts for new data
        createIndices()
    }

    fun letWorkersFly(records: List<List<CSVRecord>>) {
        val workers: List<CSVPusher> = populateCSVWorkers()
        for(i in 0 until Finals.DB_SHARD_NUM){
            workers[i].DB = dbAbsolutePaths[i]
            workers[i].csvRecords = records[i]
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
