package edu.gbcg.dbcreator.Reddit;

import edu.gbcg.dbcreator.DBCommon;
import edu.gbcg.utils.TSL;
import edu.gbcg.utils.TimeFormatter;
import edu.gbcg.utils.c;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RedditSubmissionJsonToDBWorker implements Runnable{
    private String db_;
    private List<String> json_strings_;
    private List<String> columns_;
    private List<String> keys_;
    private String table_name_;
    private static int line_count = 0;
    private ArrayList<ArrayList<String>> colValuesList = new ArrayList<>();
    private List<JSONObject> jsonObjects_ = new ArrayList<>();
    private int numObjects_;

    public RedditSubmissionJsonToDBWorker(String dbPath, List<String> jsonLines,
                                          List<String> columnNames, List<String> jsonKeys,
                                          String tableName){
        this.db_ = dbPath;
        this.json_strings_ = jsonLines;
        this.columns_ = columnNames;
        this.keys_ = jsonKeys;
        this.table_name_ = tableName;
        this.numObjects_ = this.json_strings_.size();
    }

    public RedditSubmissionJsonToDBWorker(){}

    public void run(){
    /*
        For each line in the file create a JSONObject. After creating the object loop through
        the possible keys that I care about, if the data exists push it the colValues
        ArrayList in column order for later insertion into the DB.
    */

        // Create the JSONObjects from the strings
        for(int i = 0; i < this.numObjects_; ++i)
            this.jsonObjects_.add(new JSONObject(this.json_strings_.get(i)));

        // Parse the JSON data into PreparedStatements and push to the DB
        parseAndPushDataToDB();


        line_count += json_strings_.size();
        TSL.get().log("# json lines processed: " + line_count);
    }

    public void setDB(String db){ this.db_ = db; }
    public void setJSON(List<String> jsonLines){
        this.json_strings_ = jsonLines;
        this.numObjects_ = this.json_strings_.size();
    }
    public void setColumns(List<String> columns){ this.columns_ = columns; }
    public void setKeys(List<String> keys){ this.keys_ = keys; }
    public void setTableName(String tableName){ this.table_name_ = tableName; }

    private void parseAndPushDataToDB(){
        StringBuilder sb = new StringBuilder();
        sb.append("INSERT INTO ");
        sb.append(this.table_name_);
        sb.append(" (");

        // Loop through all column names and add them to the insert string
        // NOTE: Starting the loop at 1 to skip the initial ID autoincrement field
        for(int i = 1; i < this.columns_.size() - 1; ++i) {
            sb.append(this.columns_.get(i));
            sb.append(",");
        }
        sb.append(this.columns_.get(this.columns_.size() - 1));

        sb.append(") VALUES (");
        // NOTE: Starting the loop at 1 to skip the initial ID autoincrement field
        for(int i = 1; i < this.columns_.size() - 1; ++i)
            sb.append("?,");

        // Add the final ? manually to avoid the extra comma and close up the statement
        sb.append("?);");

        String sql = sb.toString();
        // The full SQL String
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBCommon.connect(this.db_);
            ps = conn.prepareStatement(sql);
            conn.setAutoCommit(false);

            // Loop through all of the jsonObjects. For each key in the json key list, prepare an
            // insertion and add it to the batch. At the end of the loop, after all json objects
            // have been setup for insertion, execute the batch insertion
            // *** NOTE THE USE OF opt json lookups. Many current fields didn't exist in the
            // early data !!!!!
            for(int i = 0; i < numObjects_; ++i) {
                // Key indexes the PreparedStatement object
                int key = 1;

                // column = archived
                int archived = this.jsonObjects_.get(i).optBoolean("archived", false) ? 1 : 0;
                ps.setInt(key, archived); ++key;

                // column = author
                String author = this.jsonObjects_.get(i).optString("author", "null");
                ps.setString(key, author); ++key;

                // column = brand_safe
                int brand_safe = this.jsonObjects_.get(i).optBoolean("brand_safe", false) ? 1 : 0;
                ps.setInt(key, brand_safe); ++key;

                // column = contest_mode
                int contest_mode = this.jsonObjects_.get(i).optBoolean(
                        "contest_mode", false) ? 1 : 0;
                ps.setInt(key, contest_mode); ++key;

                // column = created_dt
                Long created_utc = this.jsonObjects_.get(i).optLong("created_utc", 0);
                String created_dt = TimeFormatter.javaDateTimeToSQLDateTime(
                        TimeFormatter.utcToLDT(created_utc.toString())
                );
                ps.setString(key, created_dt); ++key;

                // column = distinguished
                String dis = this.jsonObjects_.get(i).optString("distinguished", "null");
                ps.setString(key, dis); ++key;

                // column = host_domain
                String host_domain = this.jsonObjects_.get(i).optString("domain", "null");
                ps.setString(key, host_domain); ++key;

                // column = edited
                int edited = this.jsonObjects_.get(i).optBoolean("edited", false) ? 1 : 0;
                ps.setInt(key, edited); ++key;

                // column = gilded
                int gilded_count = this.jsonObjects_.get(i).optInt("gilded", 0);
                ps.setInt(key, gilded_count); ++key;

                // column = hidden
                int hidden = this.jsonObjects_.get(i).optBoolean("hidden", false) ? 1 : 0;
                ps.setInt(key, hidden); ++key;

                // column = pid
                String pid = this.jsonObjects_.get(i).optString("id", "null");
                ps.setString(key, pid); ++key;

                // column = is_self_post
                int is_self_post = this.jsonObjects_.get(i).optBoolean("is_self", false) ? 1 : 0;
                ps.setInt(key, is_self_post); ++key;

                // column = is_video_post
                int is_video_post = this.jsonObjects_.get(i).optBoolean("is_video", false) ? 1 : 0;
                ps.setInt(key, is_video_post); ++key;

                // column = link_flair_text
                String link_flair = this.jsonObjects_.get(i).optString("link_flair_text", "null");
                ps.setString(key, link_flair); ++key;

                // column = is_locked
                int is_locked = this.jsonObjects_.get(i).optBoolean("locked", false) ? 1 : 0;
                ps.setInt(key, is_locked); ++key;

                // column = num_comments
                int num_comments = this.jsonObjects_.get(i).optInt("num_comments", 0);
                ps.setInt(key, num_comments); ++key;

                // Gather the inner object for the media related items
                JSONObject media_object = jsonObjects_.get(i).optJSONObject("media");
                if(media_object != null){
                    JSONObject med_embed = media_object.optJSONObject("oembed");
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

                if(media_object == null || media_object == null){
                    ps.setString(key, ""); ++key;
                    ps.setString(key, ""); ++key;
                    ps.setString(key, ""); ++key;
                    ps.setString(key, ""); ++key;
                }


                // column = num_crossposts
                int num_crossposts = this.jsonObjects_.get(i).optInt("num_crossposts", 0);
                ps.setInt(key, num_crossposts); ++key;

                // column = over_18
                int over_18 = this.jsonObjects_.get(i).optBoolean("over_18", false) ? 1 : 0;
                ps.setInt(key, over_18); ++key;

                // column = permalink
                String permalink = this.jsonObjects_.get(i).optString("permalink", "null");
                ps.setString(key, permalink); ++key;

                // column = is_pinned
                int is_pinned = this.jsonObjects_.get(i).optBoolean("pinned", false) ? 1 : 0;
                ps.setInt(key, is_pinned); ++key;

                // column = scraped_on
                Long scraped_utc = this.jsonObjects_.get(i).optLong("retrieved_on", 0);
                String scraped_on = TimeFormatter.javaDateTimeToSQLDateTime(
                        TimeFormatter.utcToLDT(scraped_utc.toString())
                );
                ps.setString(key, scraped_on); ++key;

                // column = score
                int score = this.jsonObjects_.get(i).optInt("score", 0);
                ps.setInt(key, score); ++key;

                // column = selftext
                String selftext = this.jsonObjects_.get(i).optString("selftext", "null");
                ps.setString(key, selftext); ++key;

                // column = is_stickied
                int is_stickied = this.jsonObjects_.get(i).optBoolean("stickied", false) ? 1 : 0;
                ps.setInt(key, is_stickied); ++key;

                // column = subreddit_name
                String subreddit_name = this.jsonObjects_.get(i).optString("subreddit", "null");
                ps.setString(key, subreddit_name); ++key;

                // column = subreddit_id
                String subreddit_id = this.jsonObjects_.get(i).optString("subreddit_id", "null");
                ps.setString(key, subreddit_id); ++key;

                // column = subreddit_type
                String subreddit_type = this.jsonObjects_.get(i).optString("subreddit_type",
                        "null");
                ps.setString(key, subreddit_type); ++key;

                // column = post_title
                String post_title = this.jsonObjects_.get(i).optString("title", "null");
                ps.setString(key, post_title); ++key;

                // column = link_url
                String link_url = this.jsonObjects_.get(i).optString("url", "null");
                ps.setString(key, link_url); ++key;

                ps.addBatch();
            }
            //System.exit(0);
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
                TSL.get().err("RedditSubmissionJsonToDBWorker.parseAndPushDataToDB SQLException " +
                        "on conn.rollback()");
            }
        }
        finally{
            try{
                if(!conn.getAutoCommit())
                    conn.setAutoCommit(true);
            }
            catch(SQLException exp){
                TSL.get().err("RedditSubmissionJsonToDBWorker.parseAndPushDataToDB SQLException " +
                        "on conn.setAutoCommit(true)");
            }
            if(ps != null){
                try{
                    ps.close();
                }
                catch(SQLException ex){
                    TSL.get().err("RedditSubmissionJsonToDBWorker.parseAndPushDataToDB " +
                            "SQLException on PreparedStatement.close()");
                }
            }
            if(conn != null){
                try{
                    conn.close();
                }
                catch(SQLException ex){
                    TSL.get().err("RedditSubmissionJsonToDBWorker.parseAndPushDataToDB " +
                            "SQLException on Connection.close()");
                }
            }
        }
    }
}
