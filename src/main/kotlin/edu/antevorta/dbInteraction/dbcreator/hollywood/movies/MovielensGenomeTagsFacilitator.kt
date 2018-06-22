/*
 * Copyright (c) 2018 Sean Grimes. All Rights Reserved.
 * License: MIT
 */

package edu.antevorta.dbInteraction.dbcreator.hollywood.movies

import edu.antevorta.configs.Finals
import edu.antevorta.configs.RawDataLocator
import edu.antevorta.configs.columnsAndKeys.MovielensGenomeTags
import edu.antevorta.dbInteraction.dbcreator.CSVPusher

@Suppress("ConvertSecondaryConstructorToPrimary")
class MovielensGenomeTagsFacilitator: AbstractMoviesFacilitator {
    private val tagsIDX = "genome_tags_tag_idx"

    constructor(): super()

    override fun getDataFileAbsolutePaths()         = listOf(RawDataLocator.movielensGenomeTagsAbsolutePath())
    override fun getDataKeysOfInterest()            = MovielensGenomeTags.CSVKeys()
    override fun getColumnNames()                   = MovielensGenomeTags.columnNames()
    override fun getDataTypes()                     = MovielensGenomeTags.dataTypes()
    override fun getTableName()                     = Finals.ML_GENOME_TAGS_TABLE
    override fun getDataAbsolutePathsForNewData()   = RawDataLocator.getEmptyArrayList()

    override fun populateCSVWorkers(): List<CSVPusher> {
        val workers = ArrayList<CSVPusher>()
        for(i in 0 until Finals.DB_SHARD_NUM)
            workers.add(MovielensGenomeTagsPusher())
        return workers
    }

    override fun createIndices() {
        createDBIndex("tag", tagsIDX)
    }

    override fun dropIndices() {
        dropDBIndex(tagsIDX)
    }

}