/*
 * Copyright (c) 2018 Sean Grimes. All Rights Reserved.
 * License: MIT
 */

package edu.antevortadb.dbInteraction.dbcreator.hollywood

import edu.antevortadb.configs.Finals
import edu.antevortadb.configs.RawDataLocator
import edu.antevortadb.dbInteraction.columnsAndKeys.TMDBCredits
import edu.antevortadb.dbInteraction.dbcreator.CSVPusher
import org.apache.commons.csv.CSVFormat

@Suppress("ConvertSecondaryConstructorToPrimary")
class TMDBCreditsFacilitator: AbstractMoviesFacilitator {
    // Reset the format for this dataset due to commas within the json data
    constructor(): super() { this.parseFormat = CSVFormat.DEFAULT }

    override fun getDataFileAbsolutePaths() =
            listOf(RawDataLocator.tmdbCreditsCSVAbsolutePath())
    override fun getDataKeysOfInterest()    = TMDBCredits.csvKeys()
    override fun getColumnNames()           = TMDBCredits.columnNames()
    override fun getDataTypes()             = TMDBCredits.dataTypes()
    override fun getTableName()             = Finals.TMDB_CREDITS_TABLE

    override fun populateCSVWorkers(): List<CSVPusher> {
        val workers = ArrayList<CSVPusher>()
        for(i in 0 until Finals.DB_SHARD_NUM)
            workers.add(TMDBCreditsPusher())
        return workers
    }

    override fun createIndices() {
        logger.info("No indices to create for ${this.dbTableName}")
    }

    override fun dropIndices() {
        logger.warn("No indices to drop for ${this.dbTableName}")
    }
}
