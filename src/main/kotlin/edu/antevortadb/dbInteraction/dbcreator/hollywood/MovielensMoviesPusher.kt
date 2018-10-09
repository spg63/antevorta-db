/*
 * Copyright (c) 2018 Sean Grimes. All Rights Reserved.
 * License: MIT
 */

package edu.antevortadb.dbInteraction.dbcreator.hollywood

import edu.antevortadb.dbInteraction.DBCommon
import edu.antevortadb.dbInteraction.dbSelector.hollywood.movies.MLLinksSelector
import edu.antevortadb.dbInteraction.dbcreator.CSVPusher
import org.apache.commons.csv.CSVRecord
import org.json.JSONArray
import org.json.JSONObject
import java.sql.PreparedStatement
import java.sql.SQLException

@Suppress("unused", "PrivatePropertyName")
class MovielensMoviesPusher: CSVPusher {
    private val linksSelector = MLLinksSelector()
    private val GENREKEY = "genre"

    constructor(): super()
    constructor(dbPath: String, columnNames: List<String>, tableName: String, records: List<CSVRecord>)
    : super(dbPath, columnNames, tableName, records)

    override fun parseAndPushDataToDB() {
        val sql = buildInsertionString()
        val conn = DBCommon.connect(this.DB)
        var ps: PreparedStatement? = null

        try{
            ps = conn.prepareStatement(sql)
            conn.autoCommit = false

            for(i in 0 until this.numRecords){
                var key = 1
                // If the movieid returns something unparsable to int then we have the header,
                // just continue
                val movieid = this.csvRecords[i][0].toIntOrNull() ?: continue
                val tmdbImdbIds = linksSelector.getIMDBandTMDBFromMovielensMovieID(movieid)
                val tmdbMovieID = tmdbImdbIds.first
                val imdbMovieID = tmdbImdbIds.second
                val titleWithQuote = this.csvRecords[i][1]
                val title = titleWithQuote.replace("\"", "") // Movie title has quotes, remove them

                val genresString = this.csvRecords[i][2]
                val genresJson = splitGenresIntoJsonObject(genresString)

                ps.setInt(key++, tmdbMovieID)
                ps.setInt(key++, imdbMovieID)
                ps.setInt(key++, movieid)
                ps.setString(key++, title)
                ps.setObject(key, genresJson)       // This is a json object

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

    private fun splitGenresIntoJsonObject(genres: String): JSONObject {
        val allGenres = genres.split("|")
        val mainJsonObject = JSONObject()
        val jsonArray = JSONArray()
        for(genre in allGenres) {
            val jsonObject = JSONObject()
            // 7.14.18: Attempting to remove the leading / trailing quotes in the ml_genres data
            jsonObject.put(GENREKEY, genre.replace("\"", ""))
            jsonArray.put(jsonObject)
        }
        mainJsonObject.put("genres", jsonArray)
        return mainJsonObject
    }
}
