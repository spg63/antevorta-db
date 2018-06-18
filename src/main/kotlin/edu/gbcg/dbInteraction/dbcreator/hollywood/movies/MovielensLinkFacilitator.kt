/*
 * Copyright (c) 2018 Sean Grimes. All Rights Reserved.
 * License: MIT
 */

package edu.gbcg.dbInteraction.dbcreator.hollywood.movies

import edu.gbcg.configs.DBLocator
import edu.gbcg.configs.RawDataLocator
import edu.gbcg.dbInteraction.dbcreator.CSVFacilitator

class MovielensLinkFacilitator: AbstractMoviesFacilitator{
    constructor(): super()

    override fun getDataFileAbsolutePaths() = listOf(RawDataLocator.movielensLinkAbsolutePath())
//    override fun getDataKeysOfInterest() = gettingTheKeysFromANewColumnsAndKeys
//    override fun getColumnNames = seeAbove
//    override fun getDataTypes = seeAbove
//    override fun getTableName() = separateForEachTypeWithMultipleTables
    override fun getDataAbsolutePathsForNewData() = RawDataLocator.getEmptyArrayList()


}