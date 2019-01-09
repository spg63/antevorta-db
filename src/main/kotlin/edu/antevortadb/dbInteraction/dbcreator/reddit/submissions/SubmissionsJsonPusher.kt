/*
 * Copyright (c) 2018 Sean Grimes. All Rights Reserved.
 * License: MIT
 */

package edu.antevortadb.dbInteraction.dbcreator.reddit.submissions

import edu.antevortadb.dbInteraction.DBCommon
import edu.antevortadb.dbInteraction.dbcreator.JsonPusher
import java.sql.PreparedStatement
import java.sql.SQLException
import org.json.JSONObject

@Suppress("unused")
class SubmissionsJsonPusher : JsonPusher {
    // This class doesn't store anything, the below variables will be split between JsonPusher
    // and DataPusher
    constructor(): super()
    constructor(dbPath: String, jsonLines: List<String>, columnNames: List<String>,
                tableName: String)
            : super(dbPath, jsonLines, columnNames, tableName)

    override fun parseAndPushDataToDB() {
        val sql = buildInsertionString()
        val conn = DBCommon.connect(this.DB)
        var ps: PreparedStatement? = null

        try{
            ps = conn.prepareStatement(sql)
            conn.autoCommit = false

            for(i in 0 until this.numObjects){
                // Key indexes the PreparedStatement object
                var key = 1

                // column = archived
                val archived = if (this.jsonObjects[i]
                                .optBoolean("archived", false)) 1 else 0
                ps.setInt(key, archived); ++key

                // column = author
                val author = this.jsonObjects[i].optString("author", "null")
                ps.setString(key, author); ++key

                // column = brand_safe
                val brandSafe = if (this.jsonObjects[i]
                                .optBoolean("brand_safe", false)) 1 else 0
                ps.setInt(key, brandSafe); ++key

                // column = contest_mode
                val contestMode = if (this.jsonObjects[i]
                                .optBoolean("contest_mode", false)) 1 else 0
                ps.setInt(key, contestMode); ++key

                // column = created_dt
                val createdUTC = this.jsonObjects[i].optLong("created_utc", 0)
                ps.setLong(key, createdUTC); ++key

                // column = distinguished
                val dis = this.jsonObjects[i].optString("distinguished", "null")
                ps.setString(key, dis); ++key

                // column = host_domain
                val hostDomain = this.jsonObjects[i].optString("domain", "null")
                ps.setString(key, hostDomain); ++key

                // column = edited
                val edited = if (this.jsonObjects[i].optBoolean("edited", false)) 1 else 0
                ps.setInt(key, edited); ++key

                // column = gilded
                val gildedCount = this.jsonObjects[i].optInt("gilded", 0)
                ps.setInt(key, gildedCount); ++key

                // column = hidden
                val hidden = if (this.jsonObjects[i].optBoolean("hidden", false)) 1 else 0
                ps.setInt(key, hidden); ++key

                // column = pid
                val pid = this.jsonObjects[i].optString("id", "null")
                ps.setString(key, pid); ++key

                // column = is_self_post
                val isSelfPost = if (this.jsonObjects[i]
                                .optBoolean("is_self", false)) 1 else 0
                ps.setInt(key, isSelfPost); ++key

                // column = is_video_post
                val isVideoPost = if (this.jsonObjects[i]
                                .optBoolean("is_video", false)) 1 else 0
                ps.setInt(key, isVideoPost); ++key

                // column = link_flair_text
                val linkFlair = this.jsonObjects[i].optString("link_flair_text", "null")
                ps.setString(key, linkFlair); ++key

                // column = is_locked
                val isLocked = if (this.jsonObjects[i]
                                .optBoolean("locked", false)) 1 else 0
                ps.setInt(key, isLocked); ++key

                // column = num_comments
                val numComments = this.jsonObjects[i].optInt("num_comments", 0)
                ps.setInt(key, numComments); ++key

                // Gather the inner object for the media related items
                val mediaObject = jsonObjects[i].optJSONObject("media")
                var medEmbed: JSONObject? = null
                if (mediaObject != null) {
                    medEmbed = mediaObject.optJSONObject("oembed")
                    if (medEmbed != null) {
                        // column = media_author_name
                        val medAuthorName = medEmbed.optString("author_name", "null")
                        ps.setString(key, medAuthorName); ++key

                        // column = media_provider_name
                        val medProviderName = medEmbed.optString("provider_name", "null")
                        ps.setString(key, medProviderName); ++key

                        // column = media_title
                        val medTitle = medEmbed.optString("title", "null")
                        ps.setString(key, medTitle); ++key

                        // column = media_type
                        val medType = medEmbed.optString("type", "null")
                        ps.setString(key, medType); ++key
                    }
                }

                if (mediaObject == null || medEmbed == null) {
                    ps.setString(key, ""); ++key
                    ps.setString(key, ""); ++key
                    ps.setString(key, ""); ++key
                    ps.setString(key, ""); ++key
                }


                // column = num_crossposts
                val numCrossposts = this.jsonObjects[i].optInt("num_crossposts", 0)
                ps.setInt(key, numCrossposts); ++key

                // column = over_18
                val over18 = if (this.jsonObjects[i]
                                .optBoolean("over_18", false)) 1 else 0
                ps.setInt(key, over18); ++key

                // column = permalink
                val permalink = this.jsonObjects[i].optString("permalink", "null")
                ps.setString(key, permalink); ++key

                // column = is_pinned
                val isPinned = if (this.jsonObjects[i]
                                .optBoolean("pinned", false)) 1 else 0
                ps.setInt(key, isPinned); ++key

                // column = scraped_on
                val scrapedUTC = this.jsonObjects[i].optLong("retrieved_on", 0)
                ps.setLong(key, scrapedUTC); ++key

                // column = score
                val score = this.jsonObjects[i].optInt("score", 0)
                ps.setInt(key, score); ++key

                // column = selftext
                val selftext = this.jsonObjects[i].optString("selftext", "null")
                ps.setString(key, selftext); ++key

                // column = is_stickied
                val isStickied = if (this.jsonObjects[i]
                                .optBoolean("stickied", false)) 1 else 0
                ps.setInt(key, isStickied); ++key

                // column = subreddit_name
                val subredditName = this.jsonObjects[i].optString("subreddit", "null")
                ps.setString(key, subredditName); ++key

                // column = subreddit_id
                val subredditID = this.jsonObjects[i].optString("subreddit_id", "null")
                ps.setString(key, subredditID); ++key

                // column = subreddit_type
                val subredditType = this.jsonObjects[i]
                        .optString("subreddit_type", "null")
                ps.setString(key, subredditType); ++key

                // column = post_title
                val postTitle = this.jsonObjects[i].optString("title", "null")
                ps.setString(key, postTitle); ++key

                // column = link_url
                val linkURL = this.jsonObjects[i].optString("url", "null")
                ps.setString(key, linkURL); ++key

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
