package edu.gbcg.dbInteraction.dbcreator.reddit.comments;

import edu.gbcg.dbInteraction.DBCommon;
import edu.gbcg.dbInteraction.dbcreator.reddit.JsonPusher;
import edu.gbcg.utils.TSL;
import edu.gbcg.utils.TimeFormatter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class CommentsJsonPusher extends JsonPusher{
    public CommentsJsonPusher(String dbPath, List<String> jsonLines, List<String> columnNames, String tableName){
        super(dbPath, jsonLines, columnNames, tableName);
    }

    public CommentsJsonPusher(){ super(); }

    protected void parseAndPushDataToDB(){
        String sql = buildInsertionString();
        Connection conn = null;
        PreparedStatement ps = null;
        try{
            conn = DBCommon.connect(this.db);
            ps = conn.prepareStatement(sql);
            conn.setAutoCommit(false);

            for(int i = 0; i < num_objects; ++i){
                int key = 1;
                // column = author
                String author = this.json_objects.get(i).optString("author", "null");
                ps.setString(key, author); ++key;

                // column = author_flair_text
                String author_flair_text = this.json_objects.get(i).optString("author_flair_text", "null");
                ps.setString(key, author_flair_text); ++key;

                // column = body
                String body = this.json_objects.get(i).optString("body", "null");
                ps.setString(key, body); ++key;

                // column = can_gild
                int can_gild = this.json_objects.get(i).optBoolean("can_gild", false) ? 1 : 0;
                ps.setInt(key, can_gild); ++key;

                // column = controversial_score
                int cont = this.json_objects.get(i).optInt("controversiality", 0);
                ps.setInt(key, cont); ++key;

                // column = created_dt
                Long created_utc = this.json_objects.get(i).optLong("created_utc", 0);
                String created_dt = TimeFormatter.javaDateTimeToSQLDateTime(
                        TimeFormatter.utcToLDT(created_utc.toString())
                );
                ps.setString(key, created_dt); ++key;

                // column = distinguished
                String dist = this.json_objects.get(i).optString("distinguished", "null");
                ps.setString(key, dist); ++key;

                // column = been_edited
                int edited = this.json_objects.get(i).optBoolean("edited", false) ? 1 : 0;
                ps.setInt(key, edited); ++key;

                // column = gilded
                int gilded = this.json_objects.get(i).optInt("gilded", 0);
                ps.setInt(key, gilded); ++key;

                // column = pid
                String pid = this.json_objects.get(i).optString("id", "null");
                ps.setString(key, pid); ++key;

                // column = is_submitter
                int submitter = this.json_objects.get(i).optBoolean("is_submitter", false) ? 1 : 0;
                ps.setInt(key, submitter); ++key;

                // column = link_id
                String link_id = this.json_objects.get(i).optString("link_id", "null");
                ps.setString(key, link_id); ++key;

                // column = parent_id
                String parent_id = this.json_objects.get(i).optString("parent_id", "null");
                ps.setString(key, parent_id); ++key;

                // column = permalink
                String permalink = this.json_objects.get(i).optString("permalink", "null");
                ps.setString(key, permalink); ++key;

                // column = scraped_on
                Long retrieved_on = this.json_objects.get(i).optLong("retrieved_on", 0);
                String scraped_on = TimeFormatter.javaDateTimeToSQLDateTime(
                        TimeFormatter.utcToLDT(retrieved_on.toString())
                );
                ps.setString(key, scraped_on); ++key;

                // column = score
                int score = this.json_objects.get(i).optInt("score", 0);
                ps.setInt(key, score); ++key;

                // column = is_stickied
                int sticked = this.json_objects.get(i).optBoolean("stickied", false) ? 1 : 0;
                ps.setInt(key, sticked); ++key;

                // column = subreddit_name
                String sub_name = this.json_objects.get(i).optString("subreddit", "null");
                ps.setString(key, sub_name); ++key;

                // column = subreddit_id
                String sub_id = this.json_objects.get(i).optString("subreddit_id", "null");
                ps.setString(key, sub_id); ++key;

                // column = subreddit_type
                String sub_type = this.json_objects.get(i).optString("subreddit_type", "null");
                ps.setString(key, sub_type);

                ps.addBatch();
            }

            ps.executeBatch();
            conn.commit();;
        }
        catch(SQLException e){
            e.printStackTrace();
            try{
                conn.rollback();
            }
            catch(SQLException exp){
                TSL.get().err("CommentsJsonPusher.parseAndPushDataToDB SQLException on conn.rollback()");
            }
        }
        finally{
            try{
                if(!conn.getAutoCommit())
                    conn.setAutoCommit(true);
            }
            catch(SQLException exp){
                TSL.get().err("CommentsJsonPusher.parseAndPushDataToDB SQLException on conn.setAutoCommit(true)");
            }
            if(ps != null){
                try{
                    ps.close();
                }
                catch(SQLException ex){
                    TSL.get().err("CommentsJsonToDbWorker.parseAndPushDataToDb SQLException on ps.close()");
                }
            }
            if(conn != null){
                try{
                    conn.close();
                }
                catch(SQLException ex){
                    TSL.get().err("CommentsJsonPusher.parseAndPushDataToDB SQLException on conn.close()");
                }
            }
        }
    }
}
