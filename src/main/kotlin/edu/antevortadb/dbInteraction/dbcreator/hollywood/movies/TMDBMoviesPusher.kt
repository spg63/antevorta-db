package edu.antevortadb.dbInteraction.dbcreator.hollywood.movies

import edu.antevortadb.configs.Finals
import edu.antevortadb.dbInteraction.DBCommon
import edu.antevortadb.dbInteraction.TimeUtils
import edu.antevortadb.dbInteraction.dbSelector.hollywood.movies.MLIndividualRatingsSelector
import edu.antevortadb.dbInteraction.dbSelector.hollywood.movies.MLLinksSelector
import edu.antevortadb.dbInteraction.dbSelector.hollywood.movies.MLMoviesSelector
import edu.antevortadb.dbInteraction.dbSelector.hollywood.movies.TMDBCreditsSelector
import edu.antevortadb.dbInteraction.dbcreator.CSVPusher
import org.apache.commons.csv.CSVRecord
import org.json.JSONArray
import org.json.JSONObject
import java.sql.PreparedStatement
import java.sql.SQLException


@Suppress("unused")
class TMDBMoviesPusher: CSVPusher {
    private val linksSelector = MLLinksSelector()
    private val mlMoviesSelector = MLMoviesSelector()
    private val mlratingSelector = MLIndividualRatingsSelector()
    private val tmdbCreditsSelector = TMDBCreditsSelector()

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
                val mlVoteAverage = mlVotePair.second

                val castAndCrew = this.tmdbCreditsSelector.getCastAndCrewListFromTMDBID(tmdbMovieID)
                val cast = castAndCrew.first
                val crew = castAndCrew.second

                ps.setInt(key++, tmdbMovieID)          // TMDB Movie ID
                ps.setInt(key++, imdbMovieid)          // IMDB Movie ID
                ps.setInt(key++, mlMovieid)            // Movielens Movie ID
                ps.setInt(key++, budget)                // TMDB Movie budget
                ps.setObject(key++, tmdbGenres)        // TMDB Genres list
                ps.setObject(key++, mlGenres)          // Movielens genres list
                ps.setString(key++, website)            // Website homepage, as per TMDB
                ps.setObject(key++, tmdbKeywords)      // Keywords, according to TMDB
                ps.setString(key++, origLanguage)           // Original language
                ps.setString(key++, origTitle)         // Pre-production title
                ps.setString(key++, overview)           // Text overview of the movie
                ps.setDouble(key++, tmdbPopulatiry)           // The popularity of movie, TMDB, scale unknown
                ps.setObject(key++, productionCompanies)     // Production companies list
                ps.setObject(key++, productionCountries)     // Production countries list
                ps.setLong(key++, releaseDate)         // Date of release, stored in unix time
                ps.setInt(key++, revenue)               // Generated revenue
                ps.setInt(key++, runtime)               // Runtime in minutes
                ps.setObject(key++, spokenLanguages)       // Languages spoken in the movie
                ps.setInt(key++, released)              // 1 if released, else 0
                ps.setString(key++, tagline)            // Tagline, if it exists
                ps.setString(key++, tmdbTitle)         // Title, as per TMDB
                ps.setString(key++, movielensTitle)    // Title, as per movielens
                ps.setDouble(key++, tmdbVoteAverage)  // Average TMDB movie score
                ps.setInt(key++, tmdbVoteCount)       // Total votes from TMDB
                ps.setDouble(key++, mlVoteAverage)      // Average score of the movielens votes
                ps.setInt(key++, mlVoteCount)           // The total number of ML votes for this movie
                ps.setObject(key++, cast)               // The JSON cast from TMDB
                ps.setObject(key, crew)                 // The JSON crew from TMDB

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

    private fun convertDateToUnix(date: String): Long {
        // Note: The 00:00:00 gets appended because there is no time value, just a day date associated with
        // this data. The time value keeps consistency with the rest of the DB data
        return TimeUtils.LDTtoUTCSeconds(TimeUtils.SQLDateTimeToJavaDateTime("$date 00:00:00"))
    }

    private fun getMLVoteCountAndAverage(tmdb_movieid: Int): Pair<Int, Double> {
        val mappers = this.mlratingSelector.getMovielensRatingsForTMDBIDAsRSMappers(tmdb_movieid)
        if(mappers.isEmpty())
            return Pair(-1, -1.0)

        val totalRatings = mappers.size

        var cumulativeRating = 0.0
        // Remember that the ratings need to be multiplied by 2. TMDB ratings are out of 10, ML are out of 5
        for(mapper in mappers) {
            var theRating = mapper.getDouble("rating")
            theRating *= 2
            cumulativeRating += theRating
        }

        val finalRatingAverage = cumulativeRating / totalRatings
        return Pair(totalRatings, finalRatingAverage)
    }
}