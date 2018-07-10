/*
 * Copyright (c) 2018 Sean Grimes. All Rights Reserved.
 * License: MIT
 */

package edu.antevorta.dbInteraction.dbcreator.hollywood.movies

import edu.antevorta.configs.Finals
import edu.antevorta.configs.RawDataLocator
import edu.antevorta.dbInteraction.columnsAndKeys.MovielensIndividualRatings
import edu.antevorta.dbInteraction.columnsAndKeys.MovielensIndividualTags
import edu.antevorta.dbInteraction.dbcreator.CSVPusher

class MovielensIndividualRatingsFacilitator: AbstractMoviesFacilitator {
    private val useridIDX = "individual_ratings_userid_idx"
    private val ratingIDX = "individual_ratings_rating_idx"
    private val timeIDX = "individual_ratings_timestamp_idx"

    constructor(): super()

    override fun getDataFileAbsolutePaths()         = listOf(RawDataLocator.movielensRatingAbsolutePath())
    override fun getDataKeysOfInterest()            = MovielensIndividualRatings.CSVKeys()
    override fun getColumnNames()                   = MovielensIndividualRatings.columnNames()
    override fun getDataTypes()                     = MovielensIndividualRatings.dataTypes()
    override fun getTableName()                     = Finals.ML_INDIVIDUAL_RATING_TABLE

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
