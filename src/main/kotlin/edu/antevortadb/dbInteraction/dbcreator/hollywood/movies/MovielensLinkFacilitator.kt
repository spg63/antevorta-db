/*
 * Copyright (c) 2018 Sean Grimes. All Rights Reserved.
 * License: MIT
 */

package edu.antevortadb.dbInteraction.dbcreator.hollywood.movies

import edu.antevortadb.configs.Finals
import edu.antevortadb.configs.RawDataLocator
import edu.antevortadb.dbInteraction.columnsAndKeys.MovielensLink
import edu.antevortadb.dbInteraction.dbcreator.CSVPusher

@Suppress("ConvertSecondaryConstructorToPrimary")
class MovielensLinkFacilitator: AbstractMoviesFacilitator{
    constructor(): super()

    override fun getDataFileAbsolutePaths() = listOf(RawDataLocator.movielensLinkAbsolutePath())
    override fun getDataKeysOfInterest()    = MovielensLink.csvKeys()
    override fun getColumnNames()           = MovielensLink.columnNames()
    override fun getDataTypes()             = MovielensLink.dataTypes()
    override fun getTableName()             = Finals.ML_LINK_TABLE

    override fun populateCSVWorkers(): List<CSVPusher> {
        val workers = ArrayList<CSVPusher>()
        for(i in 0 until Finals.DB_SHARD_NUM)
            workers.add(MovielensLinkPusher())
        return workers
    }

    override fun createIndices() {
        logger.info("No indices to create for ${this.dbTableName}")
    }

    override fun dropIndices() {
        logger.warn("No indices to drop for ${this.dbTableName}")
    }
}
