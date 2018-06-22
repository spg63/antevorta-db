/*
 * Copyright (c) 2018 Sean Grimes. All Rights Reserved.
 * License: MIT
 */

package edu.antevorta.dbInteraction.dbcreator.hollywood.movies

import edu.antevorta.configs.Finals
import edu.antevorta.configs.RawDataLocator
import edu.antevorta.configs.columnsAndKeys.MovielensGenomeScores
import edu.antevorta.dbInteraction.dbcreator.CSVPusher

class MovielensGenomeScoresFacilitator: AbstractMoviesFacilitator {
    constructor(): super()

    override fun getDataFileAbsolutePaths() = listOf(RawDataLocator.movielensGenomeScoresAbsolutePath())
    override fun getDataKeysOfInterest() = MovielensGenomeScores.CSVKeys()
    override fun getColumnNames() = MovielensGenomeScores.columnNames()
    override fun getDataTypes() = MovielensGenomeScores.dataTypes()
    override fun getTableName() = Finals.ML_GENOME_SCORES_TABLE
    override fun getDataAbsolutePathsForNewData() = RawDataLocator.getEmptyArrayList()

    override fun populateCSVWorkers(): List<CSVPusher> {
        val workers = ArrayList<CSVPusher>()
        for(i in 0 until Finals.DB_SHARD_NUM)
            workers.add(MovielensGenomeScoresPusher())
        return workers
    }

    override fun createIndices() {
        createDBIndex("tagid", "tagid_dex")
        createDBIndex("relevance", "relevance_dex")
    }

    override fun dropIndices() {
        dropDBIndices("tagid_dex")
        dropDBIndices("relevance_dex")
    }
}