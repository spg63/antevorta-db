/*
 * Copyright (c) 2018 Sean Grimes. All Rights Reserved.
 * License: MIT
 */

package edu.antevorta.dbInteraction.dbcreator.hollywood.movies

import edu.antevorta.dbInteraction.DBCommon
import edu.antevorta.dbInteraction.dbcreator.CSVPusher
import org.apache.commons.csv.CSVRecord
import java.sql.PreparedStatement
import java.sql.SQLException
import java.util.*

class MovielensLinkPusher: CSVPusher {
    private val rand = Random()
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
                // If any return null it's a broken record or header, just skip it
                val movieID = this.csvRecords[i][0].trim().toIntOrNull() ?: continue
                val imdbID = this.csvRecords[i][1].trim().toIntOrNull() ?: continue
                val tmdbID = this.csvRecords[i][2].trim().toIntOrNull() ?: rand.nextInt() * -1

                // The "?:" operator says, "if this value is null, insert -1 instead"
                ps.setInt(key++, tmdbID)
                ps.setInt(key++, imdbID)
                ps.setInt(key, movieID)

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
