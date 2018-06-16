/*
 * Copyright (c) 2018 Sean Grimes. All Rights Reserved.
 * License: MIT
 */

package edu.gbcg.dbInteraction.dbcreator.reddit.submissions

import edu.gbcg.dbInteraction.DBCommon
import edu.gbcg.dbInteraction.dbcreator.DataPusher
import edu.gbcg.dbInteraction.dbcreator.JsonPusher
import edu.gbcg.utils.TSL
import java.sql.PreparedStatement
import java.sql.SQLException
import org.json.JSONObject

class SubmissionsJsonPusher : JsonPusher {
    // This class doesn't store anything, the below variables will be split between JsonPusher and DataPusher
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
                // Key indexes the PreparedStatement object
                var key = 1

                // column = archived
                val archived = if (this.jsonObjects[i].optBoolean("archived", false)) 1 else 0
                ps.setInt(key, archived); ++key

                // column = author
                val author = this.jsonObjects[i].optString("author", "null")
                ps.setString(key, author); ++key

                // column = brand_safe
                val brand_safe = if (this.jsonObjects[i].optBoolean("brand_safe", false)) 1 else 0
                ps.setInt(key, brand_safe); ++key

                // column = contest_mode
                val contest_mode = if (this.jsonObjects[i].optBoolean("contest_mode", false)) 1 else 0
                ps.setInt(key, contest_mode); ++key

                // column = created_dt
                val created_utc = this.jsonObjects[i].optLong("created_utc", 0)
                ps.setLong(key, created_utc); ++key

                // column = distinguished
                val dis = this.jsonObjects[i].optString("distinguished", "null")
                ps.setString(key, dis); ++key

                // column = host_domain
                val host_domain = this.jsonObjects[i].optString("domain", "null")
                ps.setString(key, host_domain); ++key

                // column = edited
                val edited = if (this.jsonObjects[i].optBoolean("edited", false)) 1 else 0
                ps.setInt(key, edited); ++key

                // column = gilded
                val gilded_count = this.jsonObjects[i].optInt("gilded", 0)
                ps.setInt(key, gilded_count); ++key

                // column = hidden
                val hidden = if (this.jsonObjects[i].optBoolean("hidden", false)) 1 else 0
                ps.setInt(key, hidden); ++key

                // column = pid
                val pid = this.jsonObjects[i].optString("id", "null")
                ps.setString(key, pid); ++key

                // column = is_self_post
                val is_self_post = if (this.jsonObjects[i].optBoolean("is_self", false)) 1 else 0
                ps.setInt(key, is_self_post); ++key

                // column = is_video_post
                val is_video_post = if (this.jsonObjects[i].optBoolean("is_video", false)) 1 else 0
                ps.setInt(key, is_video_post); ++key

                // column = link_flair_text
                val link_flair = this.jsonObjects[i].optString("link_flair_text", "null")
                ps.setString(key, link_flair); ++key

                // column = is_locked
                val is_locked = if (this.jsonObjects[i].optBoolean("locked", false)) 1 else 0
                ps.setInt(key, is_locked); ++key

                // column = num_comments
                val num_comments = this.jsonObjects[i].optInt("num_comments", 0)
                ps.setInt(key, num_comments); ++key

                // Gather the inner object for the media related items
                val media_object = jsonObjects[i].optJSONObject("media")
                var med_embed: JSONObject? = null
                if (media_object != null) {
                    med_embed = media_object.optJSONObject("oembed")
                    if (med_embed != null) {
                        // column = media_author_name
                        val med_author_name = med_embed.optString("author_name", "null")
                        ps.setString(key, med_author_name); ++key

                        // column = media_provider_name
                        val med_provider_name = med_embed.optString("provider_name", "null")
                        ps.setString(key, med_provider_name); ++key

                        // column = media_title
                        val med_title = med_embed.optString("title", "null")
                        ps.setString(key, med_title); ++key

                        // column = media_type
                        val med_type = med_embed.optString("type", "null")
                        ps.setString(key, med_type); ++key
                    }
                }

                if (media_object == null || med_embed == null) {
                    ps.setString(key, ""); ++key
                    ps.setString(key, ""); ++key
                    ps.setString(key, ""); ++key
                    ps.setString(key, ""); ++key
                }


                // column = num_crossposts
                val num_crossposts = this.jsonObjects[i].optInt("num_crossposts", 0)
                ps.setInt(key, num_crossposts); ++key

                // column = over_18
                val over_18 = if (this.jsonObjects[i].optBoolean("over_18", false)) 1 else 0
                ps.setInt(key, over_18); ++key

                // column = permalink
                val permalink = this.jsonObjects[i].optString("permalink", "null")
                ps.setString(key, permalink); ++key

                // column = is_pinned
                val is_pinned = if (this.jsonObjects[i].optBoolean("pinned", false)) 1 else 0
                ps.setInt(key, is_pinned); ++key

                // column = scraped_on
                val scraped_utc = this.jsonObjects[i].optLong("retrieved_on", 0)
                ps.setLong(key, scraped_utc); ++key

                // column = score
                val score = this.jsonObjects[i].optInt("score", 0)
                ps.setInt(key, score); ++key

                // column = selftext
                val selftext = this.jsonObjects[i].optString("selftext", "null")
                ps.setString(key, selftext); ++key

                // column = is_stickied
                val is_stickied = if (this.jsonObjects[i].optBoolean("stickied", false)) 1 else 0
                ps.setInt(key, is_stickied); ++key

                // column = subreddit_name
                val subreddit_name = this.jsonObjects[i].optString("subreddit", "null")
                ps.setString(key, subreddit_name); ++key

                // column = subreddit_id
                val subreddit_id = this.jsonObjects[i].optString("subreddit_id", "null")
                ps.setString(key, subreddit_id); ++key

                // column = subreddit_type
                val subreddit_type = this.jsonObjects[i].optString("subreddit_type", "null")
                ps.setString(key, subreddit_type); ++key

                // column = post_title
                val post_title = this.jsonObjects[i].optString("title", "null")
                ps.setString(key, post_title); ++key

                // column = link_url
                val link_url = this.jsonObjects[i].optString("url", "null")
                ps.setString(key, link_url); ++key

                ps.addBatch()
            }

            ps.executeBatch()
            conn.commit()
        }
        catch(e: SQLException){
            TSL.get().exception(e)
            try{
                conn.rollback()
            }
            catch(exp: SQLException){
                TSL.get().logAndKill(exp)
            }
        }
        finally{
            try{
                if(!conn.autoCommit)
                    conn.autoCommit = true
            }
            catch(exp: SQLException){
                TSL.get().logAndKill(exp)
            }
            if(ps != null){
                try{
                    ps.close()
                }
                catch(ex: SQLException){
                    TSL.get().exception(ex)
                }
            }
            try{
                conn.close()
            }
            catch(ex: SQLException){
                TSL.get().logAndKill(ex)
            }
        }
    }
}
