package edu.antevortadb.dbInteraction.dbSelector.hollywood

import edu.antevortadb.configs.DBLocator
import edu.antevortadb.configs.Finals
import edu.antevortadb.dbInteraction.columnsAndKeys.MovielensIndividualRatings
import edu.antevortadb.dbInteraction.dbSelector.DBSelector
import edu.antevortadb.dbInteraction.dbSelector.RSMapper
import edu.antevortadb.dbInteraction.dbSelector.SelectionWorker
import edu.antevortadb.dbInteraction.dbSelector.Selector

class MLIndividualRatingsSelector: Selector() {
    init{
        this.tableName = Finals.ML_INDIVIDUAL_RATING_TABLE
        this.listOfColumns = MovielensIndividualRatings.columnNames()
    }

    override fun generalSelection(SQLStatement: String): List<RSMapper> {
        val dbs = DBLocator.hollywoodAbsolutePaths()
        verifyDBsExist(dbs)

        val workers = ArrayList<SelectionWorker>()
        for(i in 0 until dbs.size)
            workers.add(SelectionWorker(
                    dbs[i], SQLStatement, MLIndividualRatingsSetMapper())
            )
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
