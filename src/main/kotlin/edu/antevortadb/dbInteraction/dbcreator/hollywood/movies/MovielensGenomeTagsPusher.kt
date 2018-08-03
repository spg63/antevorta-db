/*
 * Copyright (c) 2018 Sean Grimes. All Rights Reserved.
 * License: MIT
 */

package edu.antevortadb.dbInteraction.dbcreator.hollywood.movies

import edu.antevortadb.dbInteraction.DBCommon
import edu.antevortadb.dbInteraction.dbcreator.CSVPusher
import org.apache.commons.csv.CSVRecord
import java.sql.PreparedStatement
import java.sql.SQLException

@Suppress("unused")
class MovielensGenomeTagsPusher: CSVPusher {
    constructor(): super()
    constructor(dbPath: String, columnNames: List<String>, tableName: String, records: List<CSVRecord>)
    : super(dbPath, columnNames, tableName, records)

    override fun parseAndPushDataToDB(){
        val sql = buildInsertionString()
        val conn = DBCommon.connect(this.DB)
        var ps: PreparedStatement? = null

        try{
            ps = conn.prepareStatement(sql)
            conn.autoCommit = false

            for(i in 0 until this.numRecords){
                var key = 1

                // If tagID returns null it's a broken record or header, just skip it
                val tagID = this.csvRecords[i][0].toIntOrNull() ?: continue
                val tagName = this.csvRecords[i][1].trim().replace("'", ".").replace("\"", ".")

                ps.setInt(key++, tagID)
                ps.setString(key, tagName)

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
