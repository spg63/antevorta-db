/*
 * Copyright (c) 2018 Sean Grimes. All Rights Reserved.
 * License: MIT
 */

package edu.antevortadb.dbInteraction.dbcreator.reddit.comments

import edu.antevortadb.dbInteraction.DBCommon
import edu.antevortadb.dbInteraction.dbcreator.JsonPusher
import java.sql.PreparedStatement
import java.sql.SQLException

@Suppress("unused")
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
                val authorFlairText = this.jsonObjects[i].optString("author_flair_text", "null")
                ps.setString(key, authorFlairText); ++key

                // column = body
                val body = this.jsonObjects[i].optString("body", "null")
                ps.setString(key, body); ++key

                // column = can_gild
                val canGild = if (this.jsonObjects[i].optBoolean("can_gild", false)) 1 else 0
                ps.setInt(key, canGild); ++key

                // column = controversial_score
                val cont = this.jsonObjects[i].optInt("controversiality", 0)
                ps.setInt(key, cont); ++key

                // column = created_dt
                val createdUTC = this.jsonObjects[i].optLong("created_utc", 0)
                ps.setLong(key, createdUTC); ++key

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
                val linkID = this.jsonObjects[i].optString("link_id", "null")
                ps.setString(key, linkID); ++key

                // column = parent_id
                val parentID = this.jsonObjects[i].optString("parent_id", "null")
                ps.setString(key, parentID); ++key

                // column = permalink
                val permalink = this.jsonObjects[i].optString("permalink", "null")
                ps.setString(key, permalink); ++key

                // column = scraped_on
                val retrievedON = this.jsonObjects[i].optLong("retrieved_on", 0)
                ps.setLong(key, retrievedON); ++key

                // column = score
                val score = this.jsonObjects[i].optInt("score", 0)
                ps.setInt(key, score); ++key

                // column = is_stickied
                val sticked = if (this.jsonObjects[i].optBoolean("stickied", false)) 1 else 0
                ps.setInt(key, sticked); ++key

                // column = subreddit_name
                val subredditName = this.jsonObjects[i].optString("subreddit", "null")
                ps.setString(key, subredditName); ++key

                // column = subreddit_id
                val subredditID = this.jsonObjects[i].optString("subreddit_id", "null")
                ps.setString(key, subredditID); ++key

                // column = subreddit_type
                val subredditType = this.jsonObjects[i].optString("subreddit_type", "null")
                ps.setString(key, subredditType)

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
