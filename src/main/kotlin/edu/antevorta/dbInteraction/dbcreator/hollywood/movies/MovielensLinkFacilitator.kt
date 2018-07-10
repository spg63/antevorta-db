/*
 * Copyright (c) 2018 Sean Grimes. All Rights Reserved.
 * License: MIT
 */

package edu.antevorta.dbInteraction.dbcreator.hollywood.movies

import edu.antevorta.configs.Finals
import edu.antevorta.configs.RawDataLocator
import edu.antevorta.dbInteraction.columnsAndKeys.MovielensLink
import edu.antevorta.dbInteraction.dbcreator.CSVPusher

@Suppress("ConvertSecondaryConstructorToPrimary")
class MovielensLinkFacilitator: AbstractMoviesFacilitator{
    constructor(): super()

    override fun getDataFileAbsolutePaths()         = listOf(RawDataLocator.movielensLinkAbsolutePath())
    override fun getDataKeysOfInterest()            = MovielensLink.CSVKeys()
    override fun getColumnNames()                   = MovielensLink.columnNames()
    override fun getDataTypes()                     = MovielensLink.dataTypes()
    override fun getTableName()                     = Finals.ML_LINK_TABLE

    override fun populateCSVWorkers(): List<CSVPusher> {
        val workers = ArrayList<CSVPusher>()
        for(i in 0 until Finals.DB_SHARD_NUM)
            workers.add(MovielensLinkPusher())
        return workers
    }

    override fun createIndices() {
        logger_.info("No indices to create for ${this.tableName_}")
    }

    override fun dropIndices() {
        logger_.warn("No indices to drop for ${this.tableName_}")
    }
}