package edu.antevorta.dbInteraction.dbSelector.hollywood.movies

import edu.antevorta.configs.DBLocator
import edu.antevorta.configs.Finals
import edu.antevorta.dbInteraction.columnsAndKeys.MovielensMovies
import edu.antevorta.dbInteraction.dbSelector.DBSelector
import edu.antevorta.dbInteraction.dbSelector.RSMapper
import edu.antevorta.dbInteraction.dbSelector.SelectionWorker
import edu.antevorta.dbInteraction.dbSelector.Selector
import org.json.JSONObject


class MLMoviesSelector: Selector() {
    init{
        this.tableName = Finals.ML_MOVIES_TABLE
        this.listOfColumns = MovielensMovies.columnNames()
    }

    override fun generalSelection(SQLStatement: String): List<RSMapper> {
        val DBs = DBLocator.hollywoodAbsolutePaths()
        verifyDBsExist(DBs)

        val workers = ArrayList<SelectionWorker>()
        for(i in 0 until DBs.size)
            workers.add(SelectionWorker(DBs[i], SQLStatement, MLMoviesSetMapper()))
        return genericSelect(workers, SQLStatement)
    }

    fun getGenresFromTMDBMovieID(tmdbID: Int): JSONObject {
        val dbsql = DBSelector()
                .column("genres")
                .from(Finals.ML_MOVIES_TABLE)
                .where("tmdb_movieid = $tmdbID")

        val res = this.generalSelection(dbsql.sql())
        if(res.isEmpty())
            return JSONObject()

        // NOTE: If the rsmapper somehow has multiple results for the tmdbID, just return the first one

        return JSONObject(res[0].getString("genres"))
    }
}