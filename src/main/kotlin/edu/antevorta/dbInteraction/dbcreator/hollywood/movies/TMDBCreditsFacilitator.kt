/*
 * Copyright (c) 2018 Sean Grimes. All Rights Reserved.
 * License: MIT
 */

package edu.antevorta.dbInteraction.dbcreator.hollywood.movies

import edu.antevorta.configs.Finals
import edu.antevorta.configs.RawDataLocator
import edu.antevorta.configs.columnsAndKeys.TMDBCredits
import edu.antevorta.dbInteraction.dbcreator.CSVPusher

class TMDBCreditsFacilitator: AbstractMoviesFacilitator {
    constructor(): super()

    override fun getDataFileAbsolutePaths()         = listOf(RawDataLocator.tmdbCreditsCSVAbsolutePath())
    override fun getDataKeysOfInterest()            = TMDBCredits.CSVKeys()
    override fun getColumnNames()                   = TMDBCredits.columnNames()
    override fun getDataTypes()                     = TMDBCredits.dataTypes()
    override fun getTableName()                     = Finals.TMDB_CREDITS_TABLE
    override fun getDataAbsolutePathsForNewData()   = RawDataLocator.getEmptyArrayList()

    override fun populateCSVWorkers(): List<CSVPusher> {
        val workers = ArrayList<CSVPusher>()
        for(i in 0 until Finals.DB_SHARD_NUM)
            workers.add(TMDBCreditsPusher())
        return workers
    }

    override fun createIndices() {
        logger_.info("No indices to create for ${this.tableName_}")
    }

    override fun dropIndices() {
        logger_.warn("No indices to drop for ${this.tableName_}")
    }
}