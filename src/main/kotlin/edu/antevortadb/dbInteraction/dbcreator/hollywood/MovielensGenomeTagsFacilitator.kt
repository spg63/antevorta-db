/*
 * Copyright (c) 2018 Sean Grimes. All Rights Reserved.
 * License: MIT
 */

package edu.antevortadb.dbInteraction.dbcreator.hollywood

import edu.antevortadb.configs.Finals
import edu.antevortadb.configs.RawDataLocator
import edu.antevortadb.dbInteraction.columnsAndKeys.MovielensGenomeTags
import edu.antevortadb.dbInteraction.dbcreator.CSVPusher
import org.apache.commons.csv.CSVFormat

@Suppress("ConvertSecondaryConstructorToPrimary")
class MovielensGenomeTagsFacilitator: AbstractMoviesFacilitator {
    constructor(): super() { this.parseFormat = CSVFormat.DEFAULT }
    private val tagsIDX = "genome_tags_tag_idx"

    override fun getDataFileAbsolutePaths() = listOf(RawDataLocator.movielensGenomeTagsAbsolutePath())
    override fun getDataKeysOfInterest()    = MovielensGenomeTags.csvKeys()
    override fun getColumnNames()           = MovielensGenomeTags.columnNames()
    override fun getDataTypes()             = MovielensGenomeTags.dataTypes()
    override fun getTableName()             = Finals.ML_GENOME_TAGS_TABLE

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
