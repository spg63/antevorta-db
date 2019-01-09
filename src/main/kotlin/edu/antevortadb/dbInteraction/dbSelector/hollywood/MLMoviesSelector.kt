package edu.antevortadb.dbInteraction.dbSelector.hollywood

import edu.antevortadb.configs.DBLocator
import edu.antevortadb.configs.Finals
import edu.antevortadb.dbInteraction.columnsAndKeys.MovielensMovies
import edu.antevortadb.dbInteraction.dbSelector.DBSelector
import edu.antevortadb.dbInteraction.dbSelector.RSMapper
import edu.antevortadb.dbInteraction.dbSelector.SelectionWorker
import edu.antevortadb.dbInteraction.dbSelector.Selector
import org.json.JSONObject


@Suppress("unused")
class MLMoviesSelector: Selector() {
    init{
        this.tableName = Finals.ML_MOVIES_TABLE
        this.listOfColumns = MovielensMovies.columnNames()
    }

    override fun generalSelection(SQLStatement: String): List<RSMapper> {
        val dbs = DBLocator.hollywoodAbsolutePaths()
        verifyDBsExist(dbs)

        val workers = ArrayList<SelectionWorker>()
        for(i in 0 until dbs.size)
            workers.add(SelectionWorker(dbs[i], SQLStatement, MLMoviesSetMapper()))
        return genericSelect(workers, SQLStatement)
    }

    fun getGenresAndTitleFromTMDBMovieID(tmdbID: Int): Pair<JSONObject, String> {
        val dbsql = DBSelector()
                .from(Finals.ML_MOVIES_TABLE)
                .where("${Finals.TMDB_ID} = $tmdbID")

        val res = this.generalSelection(dbsql.sql())
        if(res.isEmpty())
            return Pair(JSONObject(), String())

        return Pair(JSONObject(res[0].getString("genres")),
                res[0].getString("movielens_title"))
    }

    fun getGenresFromTMDBMovieID(tmdbID: Int): JSONObject {
        val dbsql = DBSelector()
                .column("genres")
                .from(Finals.ML_MOVIES_TABLE)
                .where("${Finals.TMDB_ID} = $tmdbID")

        val res = this.generalSelection(dbsql.sql())
        if(res.isEmpty())
            return JSONObject()

        // NOTE: If the rsmapper somehow has multiple results for the tmdbID, just return
        // the first one

        return JSONObject(res[0].getString("genres"))
    }

    fun getTitleFromTMDBMovieID(tmdbID: Int): String {
        val dbsql = DBSelector()
                .column("movielens_title")
                .from(Finals.ML_MOVIES_TABLE)
                .where("${Finals.TMDB_ID} = $tmdbID")

        val res = this.generalSelection(dbsql.sql())
        if(res.isEmpty())
            return "NO MOVIELENS TITLE"

        return res[0].getString("movielens_title")
    }
}
