/*
 * Copyright (c) 2018 Sean Grimes. All Rights Reserved.
 * License: MIT
 */

package edu.antevorta.dbInteraction.dbcreator.hollywood.movies

import edu.antevorta.configs.DBLocator
import edu.antevorta.configs.RawDataLocator
import edu.antevorta.dbInteraction.dbcreator.CSVFacilitator

abstract class AbstractMoviesFacilitator : CSVFacilitator() {
    final override fun buildDBPaths()                       = DBLocator.buildHollywoodDBPaths()
    final override fun getDBAbsolutePaths()                 = DBLocator.hollywoodAbsolutePaths()
    final override fun getDBDirectoryPaths()                = DBLocator.getHollywoodDBDirectoryPaths()
    final override fun getDataAbsolutePathsForNewData()     = RawDataLocator.getEmptyArrayList()
}