/*
 * Copyright (c) 2018 Sean Grimes. All Rights Reserved.
 * License: MIT
 */

package edu.antevortadb.dbInteraction.dbcreator.hollywood.movies

import edu.antevortadb.configs.DBLocator
import edu.antevortadb.configs.RawDataLocator
import edu.antevortadb.dbInteraction.dbcreator.CSVFacilitator

abstract class AbstractMoviesFacilitator : CSVFacilitator() {
    final override fun buildDBPaths()                       = DBLocator.buildHollywoodDBPaths()
    final override fun getDBAbsolutePaths()                 = DBLocator.hollywoodAbsolutePaths()
    final override fun getDBDirectoryPaths()                = DBLocator.getHollywoodDBDirectoryPaths()
    final override fun getDataAbsolutePathsForNewData()     = RawDataLocator.getEmptyArrayList()
}
