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

class TMDBCreditsPusher: CSVPusher {
    private val linksSelector = MLLinksSelector()

    constructor(): super()
    constructor(dbPath: String, columnNames: List<String>, tableName: String, records: List<CSVRecord>)
    :super(dbPath, columnNames, tableName, records)

    override fun parseAndPushDataToDB(){
        val sql = buildInsertionString()
        val conn = DBCommon.connect(this.DB)
        var ps: PreparedStatement? = null

        try{
            ps = conn.prepareStatement(sql)
            conn.autoCommit = false

            for(i in 0 until this.numRecords){
                var key = 1

                // If the movieid returns something unparsable to int then we have the header, just continue
                val tmdb_movieid = this.csvRecords[i][0].toIntOrNull() ?: continue
                val imdb_mlid_ids = linksSelector.getIMDBandMLIDFromTMDBMovieID(tmdb_movieid)
                val imdb_movieid = imdb_mlid_ids.first
                val movieid = imdb_mlid_ids.second
                val title = this.csvRecords[i][1]

                val castJson = JSONObject().put("Cast", JSONArray(this.csvRecords[i][2].trim()))
                val crewJson = JSONObject().put("Crew", JSONArray(this.csvRecords[i][3].trim()))

                ps.setInt(key++, tmdb_movieid)
                ps.setInt(key++, imdb_movieid)
                ps.setInt(key++, movieid)
                ps.setString(key++, title)
                ps.setObject(key++, castJson)
                ps.setObject(key, crewJson)

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