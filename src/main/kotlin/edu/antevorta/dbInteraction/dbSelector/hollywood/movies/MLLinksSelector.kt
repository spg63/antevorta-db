/*
 * Copyright (c) 2018 Sean Grimes. All Rights Reserved.
 * License: MIT
 */

package edu.antevorta.dbInteraction.dbSelector.hollywood.movies

import edu.antevorta.configs.DBLocator
import edu.antevorta.configs.Finals
import edu.antevorta.configs.columnsAndKeys.MovielensLink
import edu.antevorta.dbInteraction.dbSelector.DBSelector
import edu.antevorta.dbInteraction.dbSelector.RSMapper
import edu.antevorta.dbInteraction.dbSelector.SelectionWorker
import edu.antevorta.dbInteraction.dbSelector.Selector

class MLLinksSelector: Selector() {
    private val tmdbcol = "tmdb_movieid"
    private val imdbcol = "imdb_movieid"
    private val mlcol   = "movielens_movieid"

    init {
        this.tableName = Finals.ML_LINK_TABLE
        this.listOfColumns = MovielensLink.columnNames()
    }

    override fun generalSelection(SQLStatement: String): List<RSMapper> {
        val DBs = DBLocator.hollywoodAbsolutePaths()
        verifyDBsExist(DBs)

        val workers = ArrayList<SelectionWorker>()
        for(i in 0 until DBs.size)
            workers.add(SelectionWorker(DBs[i], SQLStatement, MLLinksSetMapper()))
        return genericSelect(workers, SQLStatement)
    }

    /**
     * Get the tmdb_movieid and imdb_movieid values from a movielens_movieid value
     * @return Pair(TMDB_ID, IMDB_ID) or Pair(-1, -1) if values couldn't be located or otherwise errored
     */
    fun getIMDBandTMDBFromMovielensMovieID(mlID: Int): Pair<Int, Int>{
        val dbsql = DBSelector()
                .column(tmdbcol)
                .column(imdbcol)
                .from(this.tableName)
                .where("$mlcol = $mlID")
        val res = this.generalSelection(dbsql.sql())
        if(res.isEmpty()){
            logger_.err("Unable to locate values for $mlcol value of $mlID")
            return Pair(-1, -1)
        }

        if(res.size > 1){
            logger_.err("Multiple results from $mlcol vslue of $mlID")
            return Pair(-1, -1)
        }

        var tmdb_val = res[0].getInt(tmdbcol)
        if(tmdb_val == 0) tmdb_val = -1
        var imdb_val = res[0].getInt(imdbcol)
        if(imdb_val == 0) imdb_val = -1

        return Pair(tmdb_val, imdb_val)
    }

    fun getIMDBMovieIDFromMovielensMovieID(mlID: Int) = selectValFromSpecificCol(mlcol, mlID, imdbcol)
    fun getTMDBMovieIDFromMovielensMovieID(mlID: Int) = selectValFromSpecificCol(mlcol, mlID, tmdbcol)
    fun getMLMovieIDFromIMDBMovieID(imdbID: Int) = selectValFromSpecificCol(imdbcol, imdbID, mlcol)
    fun getMLMovieIDFromTMDBMovieID(tmdbID: Int) = selectValFromSpecificCol(tmdbcol, tmdbID, mlcol)


    private fun selectValFromSpecificCol(selectCol: String, selectVal: Int, from: String): Int {
        val dbsql = DBSelector()
                .column(from)
                .from(Finals.ML_LINK_TABLE)
                .where("$selectCol = $selectVal")
        val res = this.generalSelection(dbsql.sql())    // This should produce a single result, ONLY!

        if(res.isEmpty()) {
            logger_.err("Unable to locate $from for $selectCol value of $selectVal")
            return -1
        }


        if(res.size > 1) {
            logger_.err("Multiple results for $from for $selectCol value of $selectVal")
            return -1
        }
        return res[0].getInt(from)
    }

}
