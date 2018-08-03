/*
 * Copyright (c) 2018 Sean Grimes. All Rights Reserved.
 * License: MIT
 */

package edu.antevortadb.dbInteraction.dbcreator.hollywood.movies

import edu.antevortadb.configs.Finals
import edu.antevortadb.configs.RawDataLocator
import edu.antevortadb.dbInteraction.columnsAndKeys.MovielensMovies
import edu.antevortadb.dbInteraction.dbcreator.CSVPusher

@Suppress("ConvertSecondaryConstructorToPrimary")
class MovielensMoviesFacilitator: AbstractMoviesFacilitator {
    constructor(): super()

    override fun getDataFileAbsolutePaths()     = listOf(RawDataLocator.movielensMovieAbsolutePath())
    override fun getDataKeysOfInterest()        = MovielensMovies.csvKeys()
    override fun getColumnNames()               = MovielensMovies.columnNames()
    override fun getDataTypes()                 = MovielensMovies.dataTypes()
    override fun getTableName()                 = Finals.ML_MOVIES_TABLE

    override fun populateCSVWorkers(): List<CSVPusher> {
        val workers = ArrayList<CSVPusher>()
        for(i in 0 until Finals.DB_SHARD_NUM)
            workers.add(MovielensMoviesPusher())
        return workers
    }

    override fun createIndices(){
        logger.info("No indices to create for ${this.dbTableName}")
    }

    override fun dropIndices() {
        logger.warn("No indices to drop for ${this.dbTableName}")
    }
}
