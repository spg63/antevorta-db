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

            for(i in 1 until this.numRecords){
                var key = 1
                val tagID = this.csvRecords[i][0].toIntOrNull()
                val tagName = this.csvRecords[i][1]

                ps.setInt(key++, tagID ?: -1)
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
