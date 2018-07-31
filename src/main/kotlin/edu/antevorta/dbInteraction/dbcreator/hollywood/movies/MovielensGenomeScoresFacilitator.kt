/*
 * Copyright (c) 2018 Sean Grimes. All Rights Reserved.
 * License: MIT
 */

package edu.antevorta.dbInteraction.dbcreator.hollywood.movies

import edu.antevorta.configs.Finals
import edu.antevorta.configs.RawDataLocator
import edu.antevorta.dbInteraction.columnsAndKeys.MovielensGenomeScores
import edu.antevorta.dbInteraction.dbcreator.CSVPusher

class MovielensGenomeScoresFacilitator: AbstractMoviesFacilitator {
    private val tagIDX = "genome_scores_tagid_idx"
    private val relevanceIDX = "genome_scores_relevance_idx"

    constructor(): super()

    override fun getDataFileAbsolutePaths()         = listOf(RawDataLocator.movielensGenomeScoresAbsolutePath())
    override fun getDataKeysOfInterest()            = MovielensGenomeScores.csvKeys()
    override fun getColumnNames()                   = MovielensGenomeScores.columnNames()
    override fun getDataTypes()                     = MovielensGenomeScores.dataTypes()
    override fun getTableName()                     = Finals.ML_GENOME_SCORES_TABLE

    override fun populateCSVWorkers(): List<CSVPusher> {
        val workers = ArrayList<CSVPusher>()
        for(i in 0 until Finals.DB_SHARD_NUM)
            workers.add(MovielensGenomeScoresPusher())
        return workers
    }

    override fun createIndices() {
        createDBIndex("tagid", tagIDX)
        createDBIndex("relevance", relevanceIDX)
    }

    override fun dropIndices() {
        dropDBIndex(tagIDX)
        dropDBIndex(relevanceIDX)
    }
}