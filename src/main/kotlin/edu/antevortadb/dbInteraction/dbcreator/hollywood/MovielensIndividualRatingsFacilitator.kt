/*
 * Copyright (c) 2018 Sean Grimes. All Rights Reserved.
 * License: MIT
 */

package edu.antevortadb.dbInteraction.dbcreator.hollywood

import edu.antevortadb.configs.Finals
import edu.antevortadb.configs.RawDataLocator
import edu.antevortadb.dbInteraction.columnsAndKeys.MovielensIndividualRatings
import edu.antevortadb.dbInteraction.dbcreator.CSVPusher

@Suppress("ConvertSecondaryConstructorToPrimary")
class MovielensIndividualRatingsFacilitator: AbstractMoviesFacilitator {
    private val useridIDX = "individual_ratings_userid_idx"
    private val ratingIDX = "individual_ratings_rating_idx"
    private val timeIDX = "individual_ratings_timestamp_idx"

    constructor(): super()

    override fun getDataFileAbsolutePaths() =
            listOf(RawDataLocator.movielensRatingAbsolutePath())
    override fun getDataKeysOfInterest()    = MovielensIndividualRatings.csvKeys()
    override fun getColumnNames()           = MovielensIndividualRatings.columnNames()
    override fun getDataTypes()             = MovielensIndividualRatings.dataTypes()
    override fun getTableName()             = Finals.ML_INDIVIDUAL_RATING_TABLE

    override fun populateCSVWorkers(): List<CSVPusher> {
        val workers = ArrayList<CSVPusher>()
        for(i in 0 until Finals.DB_SHARD_NUM)
            workers.add(MovielensIndividualRatingsPusher())
        return workers
    }

    override fun createIndices() {
        createDBIndex(Finals.USER_ID, useridIDX)
        createDBIndex("rating", ratingIDX)
        createDBIndex(Finals.CREATED_DT, timeIDX)
    }

    override fun dropIndices() {
        dropDBIndex(useridIDX)
        dropDBIndex(ratingIDX)
        dropDBIndex(timeIDX)
    }
}
