package edu.antevorta.dbInteraction.dbSelector.hollywood.movies

import edu.antevorta.configs.DBLocator
import edu.antevorta.configs.Finals
import edu.antevorta.dbInteraction.columnsAndKeys.TMDBMovies
import edu.antevorta.dbInteraction.dbSelector.RSMapper
import edu.antevorta.dbInteraction.dbSelector.SelectionWorker
import edu.antevorta.dbInteraction.dbSelector.Selector

class MovieSelector: Selector() {
    init {
        this.tableName = Finals.TMDB_MOVIES_TABLE
        this.listOfColumns = TMDBMovies.columnNames()
    }

    override fun generalSelection(SQLStatement: String): List<RSMapper> {
        val DBs = DBLocator.hollywoodAbsolutePaths()
        verifyDBsExist(DBs)

        val workers = ArrayList<SelectionWorker>()
        for(i in 0 until DBs.size)
            workers.add(SelectionWorker(DBs[i], SQLStatement, MovieSetMapper()))
        return genericSelect(workers, SQLStatement)
    }
}