/*
 * Copyright (c) 2018 Sean Grimes. All Rights Reserved.
 * License: MIT
 */

package edu.antevorta.dbInteraction.dbcreator.hollywood.movies

import edu.antevorta.dbInteraction.DBCommon
import edu.antevorta.dbInteraction.TimeUtils
import edu.antevorta.dbInteraction.dbSelector.hollywood.movies.MLGenomeTagsSelector
import edu.antevorta.dbInteraction.dbSelector.hollywood.movies.MLLinksSelector
import edu.antevorta.dbInteraction.dbcreator.CSVPusher
import org.apache.commons.csv.CSVRecord
import java.sql.PreparedStatement
import java.sql.SQLException


@Suppress("unused")
class MovielensIndividualTagsPusher: CSVPusher {
    private val linksSelector = MLLinksSelector()
    private val tagIDSelector = MLGenomeTagsSelector()

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

                val userid = this.csvRecords[i][0].toIntOrNull() ?: continue
                val mlmid = this.csvRecords[i][1].toIntOrNull() ?: continue
                val imdbTmdbIds = linksSelector.getIMDBandTMDBFromMovielensMovieID(mlmid)
                val imdbid = imdbTmdbIds.first
                val tmdbid = imdbTmdbIds.second
                val tagtext = this.csvRecords[i][2].trim().replace("'", "").replace("\"", "")

                // Get the tagid from the genome_tags table
                val tagid = tagIDSelector.getTagIDFromTagText(tagtext)

                val sqltime = this.csvRecords[i][3]
                // Convert the sql time-string to LDT object, then convert LDT to UTC seconds. Could be more
                // efficient but the code already exists to do it this way so this is how it's done
                val time = TimeUtils.LDTtoUTCSeconds(TimeUtils.SQLDateTimeToJavaDateTime(sqltime))

                ps.setInt(key++, tmdbid)
                ps.setInt(key++, imdbid)
                ps.setInt(key++, mlmid)
                ps.setInt(key++, userid)
                ps.setInt(key++, tagid)
                ps.setString(key++, tagtext)
                ps.setLong(key, time)

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
