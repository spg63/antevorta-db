package edu.antevortadb.dbInteraction.dbSelector.hollywood

import edu.antevortadb.configs.DBLocator
import edu.antevortadb.configs.Finals
import edu.antevortadb.dbInteraction.columnsAndKeys.TMDBMovies
import edu.antevortadb.dbInteraction.dbSelector.RSMapper
import edu.antevortadb.dbInteraction.dbSelector.SelectionWorker
import edu.antevortadb.dbInteraction.dbSelector.Selector

@Suppress("unused")
class MovieSelector: Selector() {
    init {
        this.tableName = Finals.TMDB_MOVIES_TABLE
        this.listOfColumns = TMDBMovies.columnNames()
    }

    override fun generalSelection(SQLStatement: String): List<RSMapper> {
        val dbs = DBLocator.hollywoodAbsolutePaths()
        verifyDBsExist(dbs)

        val workers = ArrayList<SelectionWorker>()
        for(i in 0 until dbs.size)
            workers.add(SelectionWorker(dbs[i], SQLStatement, MovieSetMapper()))
        return genericSelect(workers, SQLStatement)
    }
}
