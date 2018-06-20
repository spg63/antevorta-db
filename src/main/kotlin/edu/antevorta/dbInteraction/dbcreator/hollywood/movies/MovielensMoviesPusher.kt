/*
 * Copyright (c) 2018 Sean Grimes. All Rights Reserved.
 * License: MIT
 */

package edu.antevorta.dbInteraction.dbcreator.hollywood.movies

import edu.antevorta.dbInteraction.DBCommon
import edu.antevorta.dbInteraction.dbSelector.hollywood.movies.MLLinksSelector
import edu.antevorta.dbInteraction.dbcreator.CSVPusher
import org.apache.commons.csv.CSVRecord
import org.json.JSONArray
import org.json.JSONObject
import java.sql.PreparedStatement
import java.sql.SQLException

class MovielensMoviesPusher: CSVPusher {
    private val linksSelector = MLLinksSelector()
    private val GENRE_KEY = "genre"

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
                // If the movieid returns something unparsable to int then we have the header, just continue
                val movieid = this.csvRecords[i][0].toIntOrNull() ?: continue
                val tmdb_imdb_ids = linksSelector.getIMDBandTMDBFromMovielensMovieID(movieid)
                val tmdb_movieid = tmdb_imdb_ids.first
                val imdb_movieid = tmdb_imdb_ids.second
                val title = this.csvRecords[i][1]

                val genresString = this.csvRecords[i][2]
                val genresJson = splitGenresIntoJsonObject(genresString)

                ps.setInt(key++, tmdb_movieid)
                ps.setInt(key++, imdb_movieid)
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
        var counter = 0
        for(genre in allGenres) {
            val jsonObject = JSONObject()
            jsonObject.put(GENRE_KEY, genre)
            jsonArray.put(jsonObject)
        }
        mainJsonObject.put("Genres", jsonArray)
        return mainJsonObject
    }
}