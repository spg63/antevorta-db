/*
 * Copyright (c) 2018 Sean Grimes. All Rights Reserved.
 * License: MIT
 */

package edu.antevortadb.dbInteraction.dbSelector.hollywood

import edu.antevortadb.configs.DBLocator
import edu.antevortadb.configs.Finals
import edu.antevortadb.dbInteraction.columnsAndKeys.MovielensLink
import edu.antevortadb.dbInteraction.dbSelector.DBSelector
import edu.antevortadb.dbInteraction.dbSelector.RSMapper
import edu.antevortadb.dbInteraction.dbSelector.SelectionWorker
import edu.antevortadb.dbInteraction.dbSelector.Selector
import java.util.concurrent.ConcurrentHashMap

// "Static" memoization maps shared between all objects
val mlMemoMap = ConcurrentHashMap<Int, Pair<Int, Int>>()
val tmdbMemoMap = ConcurrentHashMap<Int, Pair<Int, Int>>()
val imdbMemoMap = ConcurrentHashMap<Int, Pair<Int, Int>>()

@Suppress("unused")
class MLLinksSelector: Selector() {
    private val tmdbcol = Finals.TMDB_ID
    private val imdbcol = Finals.IMDB_ID
    private val mlcol   = Finals.ML_ID

    init {
        this.tableName = Finals.ML_LINK_TABLE
        this.listOfColumns = MovielensLink.columnNames()
    }

    override fun generalSelection(SQLStatement: String): List<RSMapper> {
        val dbs = DBLocator.hollywoodAbsolutePaths()
        verifyDBsExist(dbs)

        val workers = ArrayList<SelectionWorker>()
        for(i in 0 until dbs.size)
            workers.add(SelectionWorker(dbs[i], SQLStatement, MLLinksSetMapper()))
        return genericSelect(workers, SQLStatement)
    }

    /**
     * Get the tmdb_movieid and imdb_movieid values from a movielens_movieid value
     * @return Pair(TMDB_ID, IMDB_ID) or Pair(-1, -1) if values couldn't be located or otherwise
     * errored
     */
    fun getIMDBandTMDBFromMovielensMovieID(mlID: Int) = selectBothIDs(tmdbcol, imdbcol, mlcol, mlID)
    fun getIMDBandMLIDFromTMDBMovieID(tmdbID: Int) = selectBothIDs(imdbcol, mlcol, tmdbcol, tmdbID)
    fun getTMDBandMLIDFromIMDBMovieID(imdbID: Int) = selectBothIDs(tmdbcol, mlcol, imdbcol, imdbID)

    /**
     * Get individual IDs
     */
    fun getIMDBMovieIDFromMovielensMovieID(mlID: Int) = selectValFromSpecificCol(mlcol, mlID, imdbcol)
    fun getTMDBMovieIDFromMovielensMovieID(mlID: Int) = selectValFromSpecificCol(mlcol, mlID, tmdbcol)
    fun getMLMovieIDFromIMDBMovieID(imdbID: Int) = selectValFromSpecificCol(imdbcol, imdbID, mlcol)
    fun getMLMovieIDFromTMDBMovieID(tmdbID: Int) = selectValFromSpecificCol(tmdbcol, tmdbID, mlcol)

    private fun selectBothIDs(firstCol: String, secondCol: String,
                              fromCol: String, fromColID: Int): Pair<Int, Int> {
        // Check to see if this value exists in the memoization map of columns
        val memoResult = whichMemoMap(fromCol)[fromColID]
        if(memoResult != null)
            return memoResult

        val dbsql = DBSelector()
                .column(firstCol)
                .column(secondCol)
                .from(this.tableName)
                .where("$fromCol = $fromColID")
        val res = this.generalSelection(dbsql.sql())
        if(res.isEmpty()){
            return Pair(-1, -1)
        }

        if(res.size > 1){
            logger.err("Multiple results for $fromCol with value $fromColID")
            return Pair(-1, -1)
        }

        var firstVal = res[0].getInt(firstCol)
        if(firstVal == 0) firstVal = -1
        var secondVal = res[0].getInt(secondCol)
        if(secondVal == 0) secondVal = -1

        val thePair = Pair(firstVal, secondVal)

        // Values didn't exist in the memomap, add it to the map now
        addToMemoMaps(fromCol, fromColID, firstVal, secondVal)

        return thePair
    }

    // Adds IDs to all maps based on a single selection
    private fun addToMemoMaps(fromCol: String, fromColID: Int, firstVal: Int, secondVal: Int){
        when(fromCol){
            mlcol -> {
                if(!mlMemoMap.contains(fromColID))      mlMemoMap[fromColID]    = Pair(firstVal, secondVal)
                if(!imdbMemoMap.contains(firstVal))     imdbMemoMap[firstVal]   = Pair(secondVal, fromColID)
                if(!tmdbMemoMap.contains(secondVal))    tmdbMemoMap[secondVal]  = Pair(firstVal, fromColID)
            }

            tmdbcol -> {
                // tmdb, imdb, ml
                if(!tmdbMemoMap.contains(fromColID))    tmdbMemoMap[fromColID]  = Pair(firstVal, secondVal)
                if(!imdbMemoMap.contains(firstVal))     imdbMemoMap[firstVal]   = Pair(fromColID, secondVal)
                if(!mlMemoMap.contains(secondVal))      mlMemoMap[secondVal]    = Pair(firstVal, fromColID)
            }

            imdbcol -> {
                // imdb, tmdb, ml
                if(!imdbMemoMap.contains(fromColID))    imdbMemoMap[fromColID]  = Pair(firstVal, secondVal)
                if(!tmdbMemoMap.contains(firstVal))     tmdbMemoMap[firstVal]   = Pair(fromColID, secondVal)
                if(!mlMemoMap.contains(secondVal))      mlMemoMap[secondVal]    = Pair(fromColID, firstVal)
            }
        }
    }

    private fun whichMemoMap(getFromColumn: String): MutableMap<Int, Pair<Int, Int>>{
        return when(getFromColumn){
            mlcol -> mlMemoMap
            tmdbcol -> tmdbMemoMap
            imdbcol -> imdbMemoMap
            else -> {
                logger.logAndKill("MLLinksSelector.whichMemoMap: no matching getFromColumn")
                HashMap()   // NOTE: This isn't ever returned, logger kills the program
            }
        }
    }

    private fun selectValFromSpecificCol(selectCol: String, selectVal: Int, from: String): Int {
        val dbsql = DBSelector()
                .column(from)
                .from(Finals.ML_LINK_TABLE)
                .where("$selectCol = $selectVal")
        val res = this.generalSelection(dbsql.sql())    // This should produce a single result, ONLY!

        if(res.isEmpty()) {
            logger.warn("Unable to locate $from for $selectCol value of $selectVal")
            return -1
        }


        if(res.size > 1) {
            logger.err("Multiple results for $from for $selectCol value of $selectVal")
            return -1
        }
        return res[0].getInt(from)
    }
}
