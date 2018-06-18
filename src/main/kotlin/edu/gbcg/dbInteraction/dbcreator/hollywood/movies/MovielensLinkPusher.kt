/*
 * Copyright (c) 2018 Sean Grimes. All Rights Reserved.
 * License: MIT
 */

package edu.gbcg.dbInteraction.dbcreator.hollywood.movies

import edu.gbcg.dbInteraction.DBCommon
import edu.gbcg.dbInteraction.dbcreator.CSVPusher
import org.apache.commons.csv.CSVRecord
import java.sql.PreparedStatement
import java.sql.SQLException

class MovielensLinkPusher: CSVPusher {
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

            for(i in 1 until this.numRecords){
                var key = 1
                val movieID = this.csvRecords[i][0].trim().toIntOrNull()
                val imdbID = this.csvRecords[i][1].trim().toIntOrNull()
                val tmdbID = this.csvRecords[i][2].trim().toIntOrNull()

                // The "?:" operator says, "if this value is null, insert -1 instead"
                ps.setInt(key++, tmdbID ?: -1)
                ps.setInt(key++, imdbID ?: -1)
                ps.setInt(key, movieID ?: -1)

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
