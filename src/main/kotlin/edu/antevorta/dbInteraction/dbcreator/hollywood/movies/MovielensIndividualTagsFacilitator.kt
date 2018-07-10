/*
 * Copyright (c) 2018 Sean Grimes. All Rights Reserved.
 * License: MIT
 */

package edu.antevorta.dbInteraction.dbcreator.hollywood.movies

import edu.antevorta.configs.Finals
import edu.antevorta.configs.RawDataLocator
import edu.antevorta.dbInteraction.columnsAndKeys.MovielensIndividualTags
import edu.antevorta.dbInteraction.dbcreator.CSVPusher
import org.apache.commons.csv.CSVFormat

class MovielensIndividualTagsFacilitator: AbstractMoviesFacilitator {
    constructor(): super() //{ this.parseFormat = CSVFormat.DEFAULT }

    private val userIDX = "individual_tags_userid_idx"
    private val tagIDIDX = "individual_tags_tagid_idx"
    private val tagIDX = "individual_tags_tag_idx"
    private val timeIDX = "individual_tags_time_idx"

    override fun getDataFileAbsolutePaths()         = listOf(RawDataLocator.movielensTagsAbsolutePath())
    override fun getDataKeysOfInterest()            = MovielensIndividualTags.CSVKeys()
    override fun getColumnNames()                   = MovielensIndividualTags.columnNames()
    override fun getDataTypes()                     = MovielensIndividualTags.dataTypes()
    override fun getTableName()                     = Finals.ML_INDIVIUDAL_TAGS_TABLE

    override fun populateCSVWorkers(): List<CSVPusher> {
        val workers = ArrayList<CSVPusher>()
        for(i in 0 until Finals.DB_SHARD_NUM)
            workers.add(MovielensIndividualTagsPusher())
        return workers
    }

    override fun createIndices(){
        createDBIndex(Finals.USER_ID, userIDX)
        createDBIndex("tagid", tagIDIDX)
        createDBIndex("tag", tagIDX)
        createDBIndex(Finals.CREATED_DT, timeIDX)
    }

    override fun dropIndices() {
        dropDBIndex(userIDX)
        dropDBIndex(tagIDIDX)
        dropDBIndex(tagIDX)
        dropDBIndex(timeIDX)
    }
}