package edu.antevorta.dbInteraction.dbSelector.hollywood.movies

import edu.antevorta.configs.DBLocator
import edu.antevorta.configs.Finals
import edu.antevorta.dbInteraction.columnsAndKeys.MovielensIndividualRatings
import edu.antevorta.dbInteraction.dbSelector.DBSelector
import edu.antevorta.dbInteraction.dbSelector.RSMapper
import edu.antevorta.dbInteraction.dbSelector.SelectionWorker
import edu.antevorta.dbInteraction.dbSelector.Selector

class MLIndividualRatingsSelector: Selector() {
    init{
        this.tableName = Finals.ML_INDIVIDUAL_RATING_TABLE
        this.listOfColumns = MovielensIndividualRatings.columnNames()
    }

    override fun generalSelection(SQLStatement: String): List<RSMapper> {
        val DBs = DBLocator.hollywoodAbsolutePaths()
        verifyDBsExist(DBs)

        val workers = ArrayList<SelectionWorker>()
        for(i in 0 until DBs.size)
            workers.add(SelectionWorker(DBs[i], SQLStatement, MLIndividualRatingsSetMapper()))
        return genericSelect(workers, SQLStatement)
    }

    fun getMovielensRatingsForTMDBIDAsRSMappers(tmdbID: Int): List<RSMapper> {
        val dbsql = DBSelector()
                .column("rating")
                .from(Finals.ML_INDIVIDUAL_RATING_TABLE)
                .where("${Finals.TMDB_ID} = $tmdbID")

        return this.generalSelection(dbsql.sql())
    }
}