/*
 * Copyright (c) 2018 Sean Grimes. All Rights Reserved.
 * License: MIT
 */

package edu.gbcg.dbInteraction.dbcreator.hollywood.movies

import edu.gbcg.configs.DBLocator
import edu.gbcg.dbInteraction.dbcreator.CSVFacilitator

abstract class AbstractMoviesFacilitator : CSVFacilitator() {
    final override fun buildDBPaths()             = DBLocator.buildHollywoodDBPaths()
    final override fun getDBAbsolutePaths()       = DBLocator.hollywoodAbsolutePaths()
    final override fun getDBDirectoryPaths()      = DBLocator.getHollywoodDBDirectoryPaths()
}