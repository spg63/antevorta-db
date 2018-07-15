package edu.antevorta.dbInteraction.dbcreator.hollywood.movies

import edu.antevorta.configs.Finals
import edu.antevorta.dbInteraction.DBCommon
import edu.antevorta.dbInteraction.TimeUtils
import edu.antevorta.dbInteraction.dbSelector.hollywood.movies.MLIndividualRatingsSelector
import edu.antevorta.dbInteraction.dbSelector.hollywood.movies.MLLinksSelector
import edu.antevorta.dbInteraction.dbSelector.hollywood.movies.MLMoviesSelector
import edu.antevorta.dbInteraction.dbSelector.hollywood.movies.TMDBCreditsSelector
import edu.antevorta.dbInteraction.dbcreator.CSVPusher
import org.apache.commons.csv.CSVRecord
import org.json.JSONArray
import org.json.JSONObject
import java.sql.PreparedStatement
import java.sql.SQLException


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
                val tmdb_genres = JSONObject().put("genres", JSONArray(this.csvRecords[i][1].trim()))
                val website = this.csvRecords[i][2].trim()
                val tmdb_movieid = this.csvRecords[i][3].toIntOrNull() ?: continue
                //val ml_genres = this.mlMoviesSelector.getGenresFromTMDBMovieID(tmdb_movieid)

                val genres_and_movielens_title = this.mlMoviesSelector.getGenresAndTitleFromTMDBMovieID(tmdb_movieid)
                val ml_genres = genres_and_movielens_title.first
                val movielens_title = genres_and_movielens_title.second

                // Get the other 2 IDs using the links selector
                val imdb_mlid_ids = linksSelector.getIMDBandMLIDFromTMDBMovieID(tmdb_movieid)
                val imdb_movieid = imdb_mlid_ids.first
                val ml_movieid = imdb_mlid_ids.second

                val tmdb_keywords = JSONObject().put("keywords", JSONArray(this.csvRecords[i][4].trim()))
                val orig_lan = this.csvRecords[i][5].trim()
                val orig_title = this.csvRecords[i][6].trim()
                val overview = this.csvRecords[i][7].trim()
                val tmdb_pop = this.csvRecords[i][8].trim().toDoubleOrNull() ?: continue
                val prod_companies = JSONObject().put("production_companies", JSONArray(this.csvRecords[i][9].trim()))
                val prod_countries = JSONObject().put("production_countries", JSONArray(this.csvRecords[i][10].trim()))
                val release_date = convertDateToUnix(this.csvRecords[i][11].trim())
                val revenue = this.csvRecords[i][12].trim().toIntOrNull() ?: continue
                val runtime = this.csvRecords[i][13].trim().toIntOrNull() ?: continue
                val spoken_langs = JSONObject().put("spoken_languages", JSONArray(this.csvRecords[i][14].trim()))

                val inRelease = this.csvRecords[i][15].trim()
                var released = 0
                if(inRelease == "Released")
                    released = 1

                var tagline = this.csvRecords[i][16].trim()
                if(tagline.isBlank())
                    tagline = Finals.NOTAGLINE

                val tmdb_title = this.csvRecords[i][17].trim()

                // Selected above (genres_and_movielens_title) to hit the DB once instead of twice
                //val movielens_title = this.mlMoviesSelector.getTitleFromTMDBMovieID(tmdb_movieid)

                val tmdb_vote_average = this.csvRecords[i][18].trim().toDoubleOrNull() ?: continue
                val tmdb_vote_count = this.csvRecords[i][19].trim().toIntOrNull() ?: continue

                val ml_vote_pair = getMLVoteCountAndAverage(tmdb_movieid)
                val mlVoteCount = ml_vote_pair.first
                val mlVoteAverage = ml_vote_pair.second

                val cast_and_crew = this.tmdbCreditsSelector.getCastAndCrewListFromTMDBID(tmdb_movieid)
                val cast = cast_and_crew.first
                val crew = cast_and_crew.second

                ps.setInt(key++, tmdb_movieid)          // TMDB Movie ID
                ps.setInt(key++, imdb_movieid)          // IMDB Movie ID
                ps.setInt(key++, ml_movieid)            // Movielens Movie ID
                ps.setInt(key++, budget)                // TMDB Movie budget
                ps.setObject(key++, tmdb_genres)        // TMDB Genres list
                ps.setObject(key++, ml_genres)          // Movielens genres list
                ps.setString(key++, website)            // Website homepage, as per TMDB
                ps.setObject(key++, tmdb_keywords)      // Keywords, according to TMDB
                ps.setString(key++, orig_lan)           // Original language
                ps.setString(key++, orig_title)         // Pre-production title
                ps.setString(key++, overview)           // Text overview of the movie
                ps.setDouble(key++, tmdb_pop)           // The popularity of movie, TMDB, scale unknown
                ps.setObject(key++, prod_companies)     // Production companies list
                ps.setObject(key++, prod_countries)     // Production countries list
                ps.setLong(key++, release_date)         // Date of release, stored in unix time
                ps.setInt(key++, revenue)               // Generated revenue
                ps.setInt(key++, runtime)               // Runtime in minutes
                ps.setObject(key++, spoken_langs)       // Languages spoken in the movie
                ps.setInt(key++, released)              // 1 if released, else 0
                ps.setString(key++, tagline)            // Tagline, if it exists
                ps.setString(key++, tmdb_title)         // Title, as per TMDB
                ps.setString(key++, movielens_title)    // Title, as per movielens
                ps.setDouble(key++, tmdb_vote_average)  // Average TMDB movie score
                ps.setInt(key++, tmdb_vote_count)       // Total votes from TMDB
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
        val appendString = " 00:00:00"
        val dateString = date + appendString
        return TimeUtils.LDTtoUTCSeconds(TimeUtils.SQLDateTimeToJavaDateTime(dateString))
    }

    private fun getMLVoteCountAndAverage(tmdb_movieid: Int): Pair<Int, Double> {
        val mappers = this.mlratingSelector.getMovielensRatingsForTMDBIDAsRSMappers(tmdb_movieid)
        if(mappers == null || mappers.isEmpty())
            return Pair(-1, -1.0)

        val totalRatings = mappers.size

        var cumulativeRating: Double = 0.0
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
