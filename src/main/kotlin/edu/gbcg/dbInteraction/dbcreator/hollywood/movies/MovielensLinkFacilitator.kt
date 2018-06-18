/*
 * Copyright (c) 2018 Sean Grimes. All Rights Reserved.
 * License: MIT
 */

package edu.gbcg.dbInteraction.dbcreator.hollywood.movies

import edu.gbcg.configs.Finals
import edu.gbcg.configs.RawDataLocator
import edu.gbcg.configs.columnsAndKeys.MovielensLink
import edu.gbcg.dbInteraction.dbcreator.CSVPusher

@Suppress("ConvertSecondaryConstructorToPrimary")
class MovielensLinkFacilitator: AbstractMoviesFacilitator{
    constructor(): super()

    override fun getDataFileAbsolutePaths() = listOf(RawDataLocator.movielensLinkAbsolutePath())
    override fun getDataKeysOfInterest() = MovielensLink.CSVKeys()
    override fun getColumnNames() = MovielensLink.columnNames()
    override fun getDataTypes() = MovielensLink.dataTypes()
    override fun getTableName() = Finals.MOVIE_LENS_LINK_TABLE
    override fun getDataAbsolutePathsForNewData() = RawDataLocator.getEmptyArrayList()

    override fun populateCSVWorkers(): List<CSVPusher> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun createIndices() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun dropIndices() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


}