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
    private val mlcol   = "movielents_movieid"

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

        if(res.isEmpty())
            logger_.logAndKill("Unable to locate $from for $selectCol value of $selectVal")

        if(res.size > 1)
            logger_.logAndKill("Multiple results for $from for $selectCol value of $selectVal")

        return res[0].getInt(from)
    }
}
