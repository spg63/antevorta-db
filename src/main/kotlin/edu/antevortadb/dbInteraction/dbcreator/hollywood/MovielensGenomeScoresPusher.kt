/*
 * Copyright (c) 2018 Sean Grimes. All Rights Reserved.
 * License: MIT
 */

package edu.antevortadb.dbInteraction.dbcreator.hollywood

import edu.antevortadb.dbInteraction.DBCommon
import edu.antevortadb.dbInteraction.dbSelector.hollywood.MLLinksSelector
import edu.antevortadb.dbInteraction.dbcreator.CSVPusher
import org.apache.commons.csv.CSVRecord
import java.sql.PreparedStatement
import java.sql.SQLException

@Suppress("unused")
class MovielensGenomeScoresPusher: CSVPusher {
    private val linksSelector = MLLinksSelector()

    constructor(): super()
    constructor(dbPath: String, columnNames: List<String>, tableName: String,
                records: List<CSVRecord>)
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

                val mlmid = this.csvRecords[i][0].toIntOrNull() ?: continue
                val imdbTmdbMids = linksSelector.getIMDBandTMDBFromMovielensMovieID(mlmid)
                val imdbid = imdbTmdbMids.first
                val tmdbid = imdbTmdbMids.second
                val tagid = this.csvRecords[i][1].toIntOrNull() ?: continue
                val relevance = this.csvRecords[i][2].toDoubleOrNull() ?: continue

                ps.setInt(key++, tmdbid)
                ps.setInt(key++, imdbid)
                ps.setInt(key++, mlmid)
                ps.setInt(key++, tagid)
                ps.setDouble(key, relevance)

                ps.addBatch()
            }
            ps.executeBatch()
            conn.commit()
        }
        catch(e: SQLException){
            pusherCatchBlock(e, conn)
        }
        finally {
            pusherFinallyBlock(conn, ps)
        }
    }
}
