package edu.antevorta.dbInteraction.dbcreator.hollywood.movies

import edu.antevorta.configs.Finals
import edu.antevorta.configs.RawDataLocator
import edu.antevorta.dbInteraction.columnsAndKeys.TMDBMovies
import edu.antevorta.dbInteraction.dbcreator.CSVPusher
import org.apache.commons.csv.CSVFormat

@Suppress("ConvertSecondaryConstructorToPrimary")
class TMDBMoviesFacilitator: AbstractMoviesFacilitator {
    private val budgetIDX       = "tmdb_movies_budget_idx"
    private val origLangIDX     = "tmdb_movies_orig_lang_idx"
    private val tmdbPopIDX      = "tmdb_movies_tmdb_populatiry_idx"
    private val revenueIDX      = "tmdb_movies_revenue_idx"
    private val runtimeIDX      = "tmdb_movies_runtime_idx"
    private val tmdbVoteAvgIDX  = "tmdb_movies_vote_average_idx"
    private val mlVoteAvgIDX    = "tmdb_movies_ml_vote_average_idx"
    private val tmdbVoteCntIDX  = "tmdb_movies_tmdb_vote_count_idx"
    private val mlVoteCntIDX    = "tmdb_movies_ml_vote_count_idx"

    constructor(): super() { this.parseFormat = CSVFormat.DEFAULT }

    override fun getDataFileAbsolutePaths()         = listOf(RawDataLocator.tmdbMoviesCSVAbsolutePath())
    override fun getDataKeysOfInterest()            = TMDBMovies.csvKeys()
    override fun getColumnNames()                   = TMDBMovies.columnNames()
    override fun getDataTypes()                     = TMDBMovies.dataTypes()
    override fun getTableName()                     = Finals.TMDB_MOVIES_TABLE

    override fun populateCSVWorkers(): List<CSVPusher> {
        val workers = ArrayList<CSVPusher>()
        for(i in 0 until Finals.DB_SHARD_NUM)
            workers.add(TMDBMoviesPusher())
        return workers
    }

    override fun createIndices() {
        createDBIndex("budget", budgetIDX)
        createDBIndex("original_language", origLangIDX)
        createDBIndex("tmdb_popularity", tmdbPopIDX)
        createDBIndex("revenue", revenueIDX)
        createDBIndex("runtime", runtimeIDX)
        createDBIndex("tmdb_vote_average", tmdbVoteAvgIDX)
        createDBIndex("movielens_vote_average", mlVoteAvgIDX)
        createDBIndex("tmdb_vote_count", tmdbVoteCntIDX)
        createDBIndex("movielens_vote_count", mlVoteCntIDX)
    }

    override fun dropIndices() {
        dropDBIndex(budgetIDX)
        dropDBIndex(origLangIDX)
        dropDBIndex(tmdbPopIDX)
        dropDBIndex(revenueIDX)
        dropDBIndex(runtimeIDX)
        dropDBIndex(tmdbVoteAvgIDX)
        dropDBIndex(mlVoteAvgIDX)
        dropDBIndex(tmdbVoteCntIDX)
        dropDBIndex(mlVoteCntIDX)
    }
}
