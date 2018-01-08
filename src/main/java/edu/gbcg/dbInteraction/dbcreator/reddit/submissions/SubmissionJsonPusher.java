/*
 * Copyright (c) 2018 Sean Grimes. All rights reserved.
 * License: MIT License
 */

package edu.gbcg.dbInteraction.dbcreator.reddit.submissions;

import edu.gbcg.dbInteraction.DBCommon;
import edu.gbcg.dbInteraction.dbcreator.reddit.JsonPusher;
import edu.gbcg.utils.TSL;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class SubmissionJsonPusher extends JsonPusher {
    public SubmissionJsonPusher(String dbPath, List<String> jsonLines, List<String> columnName, String tableName){
        super(dbPath, jsonLines, columnName, tableName);
    }

    public SubmissionJsonPusher(){ super(); }

    protected void parseAndPushDataToDB(){
        String sql = buildInsertionString();
        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = DBCommon.connect(this.db);
            ps = conn.prepareStatement(sql);
            conn.setAutoCommit(false);

            // Loop through all of the jsonObjects. For each key in the json key list, prepare an
            // insertion and add it to the batch. At the end of the loop, after all json objects
            // have been setup for insertion, execute the batch insertion
            // *** NOTE THE USE OF opt json lookups. Many current fields didn't exist in the
            // early data !!!!!
            for(int i = 0; i < this.num_objects; ++i) {
                // Key indexes the PreparedStatement object
                int key = 1;

                // column = archived
                int archived = this.json_objects.get(i).optBoolean("archived", false) ? 1 : 0;
                ps.setInt(key, archived); ++key;

                // column = author
                String author = this.json_objects.get(i).optString("author", "null");
                ps.setString(key, author); ++key;

                // column = brand_safe
                int brand_safe = this.json_objects.get(i).optBoolean("brand_safe", false) ? 1 : 0;
                ps.setInt(key, brand_safe); ++key;

                // column = contest_mode
                int contest_mode = this.json_objects.get(i).optBoolean(
                        "contest_mode", false) ? 1 : 0;
                ps.setInt(key, contest_mode); ++key;

                // column = created_dt
                Long created_utc = this.json_objects.get(i).optLong("created_utc", 0);
                /*
                String created_dt = TimeUtils.javaDateTimeToSQLDateTime(
                        TimeUtils.utcToLDT(created_utc.toString())
                );
                */
                ps.setLong(key, created_utc); ++key;

                // column = distinguished
                String dis = this.json_objects.get(i).optString("distinguished", "null");
                ps.setString(key, dis); ++key;

                // column = host_domain
                String host_domain = this.json_objects.get(i).optString("domain", "null");
                ps.setString(key, host_domain); ++key;

                // column = edited
                int edited = this.json_objects.get(i).optBoolean("edited", false) ? 1 : 0;
                ps.setInt(key, edited); ++key;

                // column = gilded
                int gilded_count = this.json_objects.get(i).optInt("gilded", 0);
                ps.setInt(key, gilded_count); ++key;

                // column = hidden
                int hidden = this.json_objects.get(i).optBoolean("hidden", false) ? 1 : 0;
                ps.setInt(key, hidden); ++key;

                // column = pid
                String pid = this.json_objects.get(i).optString("id", "null");
                ps.setString(key, pid); ++key;

                // column = is_self_post
                int is_self_post = this.json_objects.get(i).optBoolean("is_self", false) ? 1 : 0;
                ps.setInt(key, is_self_post); ++key;

                // column = is_video_post
                int is_video_post = this.json_objects.get(i).optBoolean("is_video", false) ? 1 : 0;
                ps.setInt(key, is_video_post); ++key;

                // column = link_flair_text
                String link_flair = this.json_objects.get(i).optString("link_flair_text", "null");
                ps.setString(key, link_flair); ++key;

                // column = is_locked
                int is_locked = this.json_objects.get(i).optBoolean("locked", false) ? 1 : 0;
                ps.setInt(key, is_locked); ++key;

                // column = num_comments
                int num_comments = this.json_objects.get(i).optInt("num_comments", 0);
                ps.setInt(key, num_comments); ++key;

                // Gather the inner object for the media related items
                JSONObject media_object = json_objects.get(i).optJSONObject("media");
                JSONObject med_embed = null;
                if(media_object != null){
                    med_embed = media_object.optJSONObject("oembed");
                    if(med_embed != null){
                        // column = media_author_name
                        String med_author_name = med_embed.optString("author_name", "null");
                        ps.setString(key, med_author_name); ++key;

                        // column = media_provider_name
                        String med_provider_name = med_embed.optString("provider_name", "null");
                        ps.setString(key, med_provider_name); ++key;

                        // column = media_title
                        String med_title = med_embed.optString("title", "null");
                        ps.setString(key, med_title); ++key;

                        // column = media_type
                        String med_type = med_embed.optString("type", "null");
                        ps.setString(key, med_type); ++key;
                    }
                }

                if(media_object == null || med_embed == null){
                    ps.setString(key, ""); ++key;
                    ps.setString(key, ""); ++key;
                    ps.setString(key, ""); ++key;
                    ps.setString(key, ""); ++key;
                }


                // column = num_crossposts
                int num_crossposts = this.json_objects.get(i).optInt("num_crossposts", 0);
                ps.setInt(key, num_crossposts); ++key;

                // column = over_18
                int over_18 = this.json_objects.get(i).optBoolean("over_18", false) ? 1 : 0;
                ps.setInt(key, over_18); ++key;

                // column = permalink
                String permalink = this.json_objects.get(i).optString("permalink", "null");
                ps.setString(key, permalink); ++key;

                // column = is_pinned
                int is_pinned = this.json_objects.get(i).optBoolean("pinned", false) ? 1 : 0;
                ps.setInt(key, is_pinned); ++key;

                // column = scraped_on
                Long scraped_utc = this.json_objects.get(i).optLong("retrieved_on", 0);
                /*
                String scraped_on = TimeUtils.javaDateTimeToSQLDateTime(
                        TimeUtils.utcSecondsToLDT(scraped_utc.toString())
                );
                */
                ps.setLong(key, scraped_utc); ++key;

                // column = score
                int score = this.json_objects.get(i).optInt("score", 0);
                ps.setInt(key, score); ++key;

                // column = selftext
                String selftext = this.json_objects.get(i).optString("selftext", "null");
                ps.setString(key, selftext); ++key;

                // column = is_stickied
                int is_stickied = this.json_objects.get(i).optBoolean("stickied", false) ? 1 : 0;
                ps.setInt(key, is_stickied); ++key;

                // column = subreddit_name
                String subreddit_name = this.json_objects.get(i).optString("subreddit", "null");
                ps.setString(key, subreddit_name); ++key;

                // column = subreddit_id
                String subreddit_id = this.json_objects.get(i).optString("subreddit_id", "null");
                ps.setString(key, subreddit_id); ++key;

                // column = subreddit_type
                String subreddit_type = this.json_objects.get(i).optString("subreddit_type",
                        "null");
                ps.setString(key, subreddit_type); ++key;

                // column = post_title
                String post_title = this.json_objects.get(i).optString("title", "null");
                ps.setString(key, post_title); ++key;

                // column = link_url
                String link_url = this.json_objects.get(i).optString("url", "null");
                ps.setString(key, link_url); ++key;

                ps.addBatch();
            }
            // Execute the batch update
            ps.executeBatch();

            // Commit
            conn.commit();
        }
        catch(SQLException e){
            e.printStackTrace();
            try {
                // Roll things back if the batch failed
                conn.rollback();
            }
            catch(SQLException ex){
                TSL.get().err("SubmissionJsonPusher.parseAndPushDataToDB SQLException " +
                        "on conn.rollback()");
            }
        }
        finally{
            try{
                if(!conn.getAutoCommit())
                    conn.setAutoCommit(true);
            }
            catch(SQLException exp){
                TSL.get().err("SubmissionJsonPusher.parseAndPushDataToDB SQLException " +
                        "on conn.setAutoCommit(true)");
            }
            if(ps != null){
                try{
                    ps.close();
                }
                catch(SQLException ex){
                    TSL.get().err("SubmissionJsonPusher.parseAndPushDataToDB " +
                            "SQLException on PreparedStatement.close()");
                }
            }
            if(conn != null){
                try{
                    conn.close();
                }
                catch(SQLException ex){
                    TSL.get().err("SubmissionJsonPusher.parseAndPushDataToDB " +
                            "SQLException on Connection.close()");
                }
            }
        }
    }
}
