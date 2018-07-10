package edu.antevorta.dbInteraction.dbcreator.hollywood.movies

import edu.antevorta.dbInteraction.DBCommon
import edu.antevorta.dbInteraction.dbSelector.hollywood.movies.MLLinksSelector
import edu.antevorta.dbInteraction.dbcreator.CSVPusher
import org.apache.commons.csv.CSVRecord
import org.json.JSONArray
import org.json.JSONObject
import java.sql.PreparedStatement
import java.sql.SQLException


class TMDBMoviesPusher: CSVPusher {
    private val linksSelector = MLLinksSelector()

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
                val homepage = this.csvRecords[i][2]
                val tmdb_movieid = this.csvRecords[i][3].toIntOrNull() ?: continue

                // Get the other 2 IDs using the links selector
                val imdb_mlid_ids = linksSelector.getIMDBandMLIDFromTMDBMovieID(tmdb_movieid)
                val imdb_movieid = imdb_mlid_ids.first
                val ml_movieid = imdb_mlid_ids.second





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
}