/*
 * Copyright (c) 2018 Sean Grimes. All Rights Reserved.
 * License: MIT
 */

package edu.antevorta.dbInteraction.dbcreator.reddit.comments

import edu.antevorta.dbInteraction.DBCommon
import edu.antevorta.dbInteraction.dbcreator.DataPusher
import edu.antevorta.dbInteraction.dbcreator.JsonPusher
import edu.antevorta.utils.TSL
import java.sql.PreparedStatement
import java.sql.SQLException

class CommentsJsonPusher : JsonPusher {
    constructor(): super()
    constructor(dbPath: String, jsonLines: List<String>, columnNames: List<String>, tableName: String)
    : super(dbPath, jsonLines, columnNames, tableName)

    override fun parseAndPushDataToDB() {
        val sql = buildInsertionString()
        val conn = DBCommon.connect(this.DB)
        var ps: PreparedStatement? = null

        try{
            ps = conn.prepareStatement(sql)
            conn.autoCommit = false

            for(i in 0 until this.numObjects){
                var key = 1
                // column = author
                val author = this.jsonObjects[i].optString("author", "null")
                ps.setString(key, author); ++key

                // column = author_flair_text
                val author_flair_text = this.jsonObjects[i].optString("author_flair_text", "null")
                ps.setString(key, author_flair_text); ++key

                // column = body
                val body = this.jsonObjects[i].optString("body", "null")
                ps.setString(key, body); ++key

                // column = can_gild
                val can_gild = if (this.jsonObjects[i].optBoolean("can_gild", false)) 1 else 0
                ps.setInt(key, can_gild); ++key

                // column = controversial_score
                val cont = this.jsonObjects[i].optInt("controversiality", 0)
                ps.setInt(key, cont); ++key

                // column = created_dt
                val created_utc = this.jsonObjects[i].optLong("created_utc", 0)
                ps.setLong(key, created_utc); ++key

                // column = distinguished
                val dist = this.jsonObjects[i].optString("distinguished", "null")
                ps.setString(key, dist); ++key

                // column = been_edited
                val edited = if (this.jsonObjects[i].optBoolean("edited", false)) 1 else 0
                ps.setInt(key, edited); ++key

                // column = gilded
                val gilded = this.jsonObjects[i].optInt("gilded", 0)
                ps.setInt(key, gilded); ++key

                // column = pid
                val pid = this.jsonObjects[i].optString("id", "null")
                ps.setString(key, pid); ++key

                // column = is_submitter
                val submitter = if (this.jsonObjects[i].optBoolean("is_submitter", false)) 1 else 0
                ps.setInt(key, submitter); ++key

                // column = link_id
                val link_id = this.jsonObjects[i].optString("link_id", "null")
                ps.setString(key, link_id); ++key

                // column = parent_id
                val parent_id = this.jsonObjects[i].optString("parent_id", "null")
                ps.setString(key, parent_id); ++key

                // column = permalink
                val permalink = this.jsonObjects[i].optString("permalink", "null")
                ps.setString(key, permalink); ++key

                // column = scraped_on
                val retrieved_on = this.jsonObjects[i].optLong("retrieved_on", 0)
                ps.setLong(key, retrieved_on); ++key

                // column = score
                val score = this.jsonObjects[i].optInt("score", 0)
                ps.setInt(key, score); ++key

                // column = is_stickied
                val sticked = if (this.jsonObjects[i].optBoolean("stickied", false)) 1 else 0
                ps.setInt(key, sticked); ++key

                // column = subreddit_name
                val sub_name = this.jsonObjects[i].optString("subreddit", "null")
                ps.setString(key, sub_name); ++key

                // column = subreddit_id
                val sub_id = this.jsonObjects[i].optString("subreddit_id", "null")
                ps.setString(key, sub_id); ++key

                // column = subreddit_type
                val sub_type = this.jsonObjects[i].optString("subreddit_type", "null")
                ps.setString(key, sub_type)

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
