/*
 * Copyright (c) 2018 Sean Grimes. All Rights Reserved.
 * License: MIT
 */

package edu.antevorta.dbInteraction.dbcreator.hollywood.movies

import edu.antevorta.configs.Finals
import edu.antevorta.configs.RawDataLocator
import edu.antevorta.configs.columnsAndKeys.MovielensLink
import edu.antevorta.dbInteraction.dbcreator.CSVPusher

@Suppress("ConvertSecondaryConstructorToPrimary")
class MovielensLinkFacilitator: AbstractMoviesFacilitator{
    constructor(): super()

    override fun getDataFileAbsolutePaths()         = listOf(RawDataLocator.movielensLinkAbsolutePath())
    override fun getDataKeysOfInterest()            = MovielensLink.CSVKeys()
    override fun getColumnNames()                   = MovielensLink.columnNames()
    override fun getDataTypes()                     = MovielensLink.dataTypes()
    override fun getTableName()                     = Finals.MOVIE_LENS_LINK_TABLE
    override fun getDataAbsolutePathsForNewData()   = RawDataLocator.getEmptyArrayList()

    override fun populateCSVWorkers(): List<CSVPusher> {
        val workers = ArrayList<CSVPusher>()
        for(i in 0 until Finals.DB_SHARD_NUM)
            workers.add(MovielensLinkPusher())
        return workers
    }

    override fun createIndices() {
        createDBIndex("tmdb_movieid", "tmdb_index")
        createDBIndex("imdb_movieid", "imdb_index")
        createDBIndex("movielens_movieid", "lens_index")
    }

    override fun dropIndices() {
        dropDBIndices("tmdb_index")
        dropDBIndices("imdb_index")
        dropDBIndices("lens_index")
    }
}