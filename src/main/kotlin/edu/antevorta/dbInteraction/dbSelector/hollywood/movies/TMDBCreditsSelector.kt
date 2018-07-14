package edu.antevorta.dbInteraction.dbSelector.hollywood.movies

import edu.antevorta.configs.DBLocator
import edu.antevorta.configs.Finals
import edu.antevorta.dbInteraction.columnsAndKeys.TMDBCredits
import edu.antevorta.dbInteraction.dbSelector.DBSelector
import edu.antevorta.dbInteraction.dbSelector.RSMapper
import edu.antevorta.dbInteraction.dbSelector.SelectionWorker
import edu.antevorta.dbInteraction.dbSelector.Selector
import org.json.JSONObject

class TMDBCreditsSelector: Selector() {
    init{
        this.tableName = Finals.TMDB_CREDITS_TABLE
        this.listOfColumns = TMDBCredits.columnNames()
    }

    override fun generalSelection(SQLStatement: String): List<RSMapper> {
        val DBs = DBLocator.hollywoodAbsolutePaths()
        verifyDBsExist(DBs)

        val workers = ArrayList<SelectionWorker>()
        for(i in 0 until DBs.size)
            workers.add(SelectionWorker(DBs[i], SQLStatement, TMDBCreditsSetMapper()))
        return genericSelect(workers, SQLStatement)
    }

    fun getCastAndCrewListFromTMDBID(tmdbID: Int): Pair<JSONObject, JSONObject> {
        val dbsql = DBSelector()
                .column("cast")
                .column("crew")
                .from(Finals.TMDB_CREDITS_TABLE)
                .where("${Finals.TMDB_ID} = $tmdbID")

        val res = this.generalSelection(dbsql.sql())
        if(res.isEmpty())
            return Pair(JSONObject(), JSONObject())

        return Pair(JSONObject(res[0].getString("cast")), JSONObject(res[0].getString("crew")))
    }
}