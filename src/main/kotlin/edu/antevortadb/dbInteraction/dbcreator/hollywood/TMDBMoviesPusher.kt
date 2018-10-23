package edu.antevortadb.dbInteraction.dbcreator.hollywood

import edu.antevortadb.configs.Finals
import edu.antevortadb.dbInteraction.DBCommon
import edu.antevortadb.dbInteraction.TimeUtils
import edu.antevortadb.dbInteraction.dbSelector.hollywood.MLIndividualRatingsSelector
import edu.antevortadb.dbInteraction.dbSelector.hollywood.MLLinksSelector
import edu.antevortadb.dbInteraction.dbSelector.hollywood.MLMoviesSelector
import edu.antevortadb.dbInteraction.dbSelector.hollywood.TMDBCreditsSelector
import edu.antevortadb.dbInteraction.dbcreator.CSVPusher
import org.apache.commons.csv.CSVRecord
import org.json.JSONArray
import org.json.JSONObject
import java.sql.PreparedStatement
import java.sql.SQLException
import java.text.DecimalFormat


@Suppress("unused")
class TMDBMoviesPusher: CSVPusher {
    private val linksSelector = MLLinksSelector()
    private val mlMoviesSelector = MLMoviesSelector()
    private val mlratingSelector = MLIndividualRatingsSelector()
    private val tmdbCreditsSelector = TMDBCreditsSelector()
    // Used to format the ML vote average to be consistent with single decimal for TMDB
    private val decimalFormatter = DecimalFormat("#.#")
    private val numberOfPerformanceClasses = 5

    constructor(): super()
    constructor(dbPath: String, columnNames: List<String>, tableName: String, records: List<CSVRecord>)
    :super(dbPath, columnNames, tableName, records)

    override fun parseAndPushDataToDB() {
        val sql = buildInsertionString()
        val conn = DBCommon.connect(this.DB)
        var ps: PreparedStatement? = null

        try{
            ps = conn.prepareStatement(sql)
            conn.autoCommit = false

            for(i in 0 until this.numRecords){
                var key = 1

                val budget = this.csvRecords[i][0].toIntOrNull() ?: continue
                val tmdbGenres = JSONObject().put("genres", JSONArray(this.csvRecords[i][1].trim()))
                val website = this.csvRecords[i][2].trim()
                val tmdbMovieID = this.csvRecords[i][3].toIntOrNull() ?: continue
                //val ml_genres = this.mlMoviesSelector.getGenresFromTMDBMovieID(tmdb_movieid)

                val genresAndMovielensTitle = this.mlMoviesSelector
                        .getGenresAndTitleFromTMDBMovieID(tmdbMovieID)
                val mlGenres = genresAndMovielensTitle.first
                val movielensTitle = genresAndMovielensTitle.second

                // Get the other 2 IDs using the links selector
                val imdbMlidIds = linksSelector.getIMDBandMLIDFromTMDBMovieID(tmdbMovieID)
                val imdbMovieid = imdbMlidIds.first
                val mlMovieid = imdbMlidIds.second

                val tmdbKeywords = JSONObject().put("keywords", JSONArray(this.csvRecords[i][4].trim()))
                val origLanguage = this.csvRecords[i][5].trim()
                val origTitle = this.csvRecords[i][6].trim()
                val overview = this.csvRecords[i][7].trim()
                val tmdbPopulatiry = this.csvRecords[i][8].trim().toDoubleOrNull() ?: continue

                val productionCompanies = JSONObject().put("production_companies",
                        JSONArray(this.csvRecords[i][9].trim()))

                val productionCountries = JSONObject().put("production_countries",
                        JSONArray(this.csvRecords[i][10].trim()))

                val releaseDate = convertDateToUnix(this.csvRecords[i][11].trim())
                val revenue = this.csvRecords[i][12].trim().toIntOrNull() ?: continue
                val runtime = this.csvRecords[i][13].trim().toIntOrNull() ?: continue

                val spokenLanguages = JSONObject().put("spoken_languages",
                        JSONArray(this.csvRecords[i][14].trim()))

                val inRelease = this.csvRecords[i][15].trim()
                var released = 0
                if(inRelease == "Released")
                    released = 1

                var tagline = this.csvRecords[i][16].trim()
                if(tagline.isBlank())
                    tagline = Finals.NOTAGLINE

                val tmdbTitle = this.csvRecords[i][17].trim()

                // Selected above (genres_and_movielens_title) to hit the DB once instead of twice
                //val movielens_title = this.mlMoviesSelector.getTitleFromTMDBMovieID(tmdb_movieid)

                val tmdbVoteAverage = this.csvRecords[i][18].trim().toDoubleOrNull() ?: continue
                val tmdbVoteCount = this.csvRecords[i][19].trim().toIntOrNull() ?: continue

                val mlVotePair = getMLVoteCountAndAverage(tmdbMovieID)
                val mlVoteCount = mlVotePair.first
                val mlVoteAverageManyPoints = mlVotePair.second
                val mlVoteAverage = decimalFormatter.format(mlVoteAverageManyPoints).toDouble()

                val castAndCrew = this.tmdbCreditsSelector.getCastAndCrewListFromTMDBID(tmdbMovieID)
                val cast = castAndCrew.first
                val crew = castAndCrew.second

                ps.setInt(key++, tmdbMovieID)               // TMDB Movie ID
                ps.setInt(key++, imdbMovieid)               // IMDB Movie ID
                ps.setInt(key++, mlMovieid)                 // Movielens Movie ID
                ps.setInt(key++, budget)                    // TMDB Movie budget
                ps.setObject(key++, tmdbGenres)             // TMDB Genres list
                ps.setObject(key++, mlGenres)               // Movielens genres list
                ps.setString(key++, website)                // Website homepage, as per TMDB
                ps.setObject(key++, tmdbKeywords)           // Keywords, according to TMDB
                ps.setString(key++, origLanguage)           // Original language
                ps.setString(key++, origTitle)              // Pre-production title
                ps.setString(key++, overview)               // Text overview of the movie
                ps.setDouble(key++, tmdbPopulatiry)         // The popularity of movie, TMDB,
                                                            // scale unknown
                ps.setObject(key++, productionCompanies)    // Production companies list
                ps.setObject(key++, productionCountries)    // Production countries list
                ps.setLong(key++, releaseDate)              // Date of release, stored in unix time
                ps.setInt(key++, revenue)                   // Generated revenue
                ps.setInt(key++, runtime)                   // Runtime in minutes
                ps.setObject(key++, spokenLanguages)        // Languages spoken in the movie
                ps.setInt(key++, released)                  // 1 if released, else 0
                ps.setString(key++, tagline)                // Tagline, if it exists
                ps.setString(key++, tmdbTitle)              // Title, as per TMDB
                ps.setString(key++, movielensTitle)         // Title, as per movielens
                ps.setDouble(key++, tmdbVoteAverage)        // Average TMDB movie score
                ps.setInt(key++, tmdbVoteCount)             // Total votes from TMDB
                ps.setDouble(key++, mlVoteAverage)          // Average score of the movielens votes
                ps.setInt(key++, mlVoteCount)               // The total number of ML votes for
                                                            // this movie
                ps.setObject(key++, cast)                   // The JSON cast from TMDB
                ps.setObject(key++, crew)                   // The JSON crew from TMDB

                // Added columns for the initial classification tests. See
                // initialClassification.txt for more details about this test.
                val performanceData = determinePerformanceClass(budget, revenue)
                val failure = if(performanceData == 0) 1 else 0
                val mildSuccess = if(performanceData == 1) 1 else 0
                val success = if(performanceData == 2) 1 else 0
                val greatSuccess = if(performanceData == 3) 1 else 0
                // Used for 4 classes, commented in determinePerformanceClass
                //val madeBackBudget = if(performanceData > 1) 1 else 0
                // Used when there are 3 classes, failure, mildSuccess, success
                val madeBackBudget = if(performanceData > 0) 1 else 0
                val missingData = if(performanceData == -1) 1 else 0

                ps.setInt(key++, failure)                   // 1 if true, 0 if false
                ps.setInt(key++, mildSuccess)               // 1 if true, 0 if false
                ps.setInt(key++, success)                   // 1 if true, 0 if false
                ps.setInt(key++, greatSuccess)              // 1 if true, 0 if false
                ps.setInt(key++, missingData)               // 1 if true, 0 if false
                ps.setInt(key++, madeBackBudget)            // 1 if true, 0 if false
                ps.setInt(key, performanceData)             // 0, 1, 2, 3, -1 depending on class

                ps.addBatch()
            }
            ps.executeBatch()
            conn.commit()
        }
        catch(e: SQLException){
            pusherCatchBlock(e, conn)
        }
        finally{
            pusherFinallyBlock(conn, ps)
        }
    }

    private fun determinePerformanceClass(budget: Int, revenue: Int): Int {
        // Not entirely sure what the deal is here, so we're going to exclude these movies for now
        if(budget == 0 && revenue == 0)
            return -1
        /*
        if(revenue <= budget)
            return 0
        else if(revenue <= (budget * 2))
            return 1
        else if(revenue > (budget * 2) && revenue <= (budget * 4))
            return 2
        else if(revenue > (budget * 4))
            return 3
        */
        if(revenue <= (budget * 1.5))
            return 0
        else if(revenue > (budget * 1.5) && revenue < (budget * 5))
            return 1
        else if(revenue > (budget * 5))
            return 2
        else
            logger.err("TMDBMoviePusher.determinePerformanceClass is returning -1\n" +
                        "budget: $budget | revenue: $revenue")
        return -1
    }

    private fun convertDateToUnix(date: String): Long {
        // Note: The 00:00:00 gets appended because there is no time value, just a day date
        // associated with this data. The time value keeps consistency with the rest of the DB data
        return TimeUtils.LDTtoUTCSeconds(TimeUtils.SQLDateTimeToJavaDateTime("$date 00:00:00"))
    }

    private fun getMLVoteCountAndAverage(tmdb_movieid: Int): Pair<Int, Double> {
        val mappers = this.mlratingSelector.getMovielensRatingsForTMDBIDAsRSMappers(tmdb_movieid)
        if(mappers.isEmpty())
            return Pair(-1, -1.0)

        val totalRatings = mappers.size

        var cumulativeRating = 0.0
        // Remember that the ratings need to be multiplied by 2. TMDB ratings are out of 10,
        // ML are out of 5
        for(mapper in mappers) {
            var theRating = mapper.getDouble("rating")
            theRating *= 2
            cumulativeRating += theRating
        }

        val finalRatingAverage = cumulativeRating / totalRatings
        return Pair(totalRatings, finalRatingAverage)
    }
}
