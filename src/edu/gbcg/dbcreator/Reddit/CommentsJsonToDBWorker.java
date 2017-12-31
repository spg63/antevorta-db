package edu.gbcg.dbcreator.Reddit;

import edu.gbcg.dbcreator.DBCommon;
import edu.gbcg.utils.TSL;
import edu.gbcg.utils.TimeFormatter;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CommentsJsonToDBWorker implements Runnable{
    private String db_;
    private List<String> json_strings_;
    private List<String> columns_;
    String table_name_;
    private static int line_count = 0;
    private List<JSONObject> jsonObjects_ = new ArrayList<>();
    private int numObjects_;

    public CommentsJsonToDBWorker(String dbPath, List<String> jsonLines,
                                  List<String> columnNames, String tableName){
        this.db_ = dbPath;
        this.json_strings_ = jsonLines;
        this.columns_ = columnNames;
        this.table_name_ = tableName;
        this.numObjects_ = this.json_strings_.size();
    }

    public CommentsJsonToDBWorker(){}

    public void run(){
        // Create the JSONObjects from the strings
        for(int i = 0; i < this.numObjects_; ++i)
            this.jsonObjects_.add(new JSONObject(this.json_strings_.get(i)));

        // Parse the json data into PreparedStatements and push to the DB
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
    public void setTableName(String tableName){ this.table_name_ = tableName; }

    private void parseAndPushDataToDB(){
        StringBuilder sb = new StringBuilder();
        sb.append("INSERT INTO ");
        sb.append(this.table_name_);
        sb.append(" (");

        for(int i = 1; i < this.columns_.size() - 1; ++i){
            sb.append(this.columns_.get(i));
            sb.append(",");
        }
        sb.append(this.columns_.get(this.columns_.size() - 1));
        sb.append(") VALUES (");
        for(int i = 1; i < this.columns_.size() - 1; ++i)
            sb.append("?,");
        sb.append("?);");

        String sql = sb.toString();
        Connection conn = null;
        PreparedStatement ps = null;
        try{
            conn = DBCommon.connect(this.db_);
            ps = conn.prepareStatement(sql);
            conn.setAutoCommit(false);

            for(int i = 0; i < numObjects_; ++i){
                int key = 1;
                // column = author
                String author = this.jsonObjects_.get(i).optString("author", "null");
                ps.setString(key, author); ++key;

                // column = author_flair_text
                String author_flair_text = this.jsonObjects_.get(i).optString("author_flair_text", "null");
                ps.setString(key, author_flair_text); ++key;

                // column = body
                String body = this.jsonObjects_.get(i).optString("body", "null");
                ps.setString(key, body); ++key;

                // column = can_gild
                int can_gild = this.jsonObjects_.get(i).optBoolean("can_gild", false) ? 1 : 0;
                ps.setInt(key, can_gild); ++key;

                // column = controversial_score
                int cont = this.jsonObjects_.get(i).optInt("controversiality", 0);
                ps.setInt(key, cont); ++key;

                // column = created_dt
                Long created_utc = this.jsonObjects_.get(i).optLong("created_utc", 0);
                String created_dt = TimeFormatter.javaDateTimeToSQLDateTime(
                        TimeFormatter.utcToLDT(created_utc.toString())
                );
                ps.setString(key, created_dt); ++key;

                // column = distinguished
                String dist = this.jsonObjects_.get(i).optString("distinguished", "null");
                ps.setString(key, dist); ++key;

                // column = been_edited
                int edited = this.jsonObjects_.get(i).optBoolean("edited", false) ? 1 : 0;
                ps.setInt(key, edited); ++key;

                // column = gilded
                int gilded = this.jsonObjects_.get(i).optInt("gilded", 0);
                ps.setInt(key, gilded); ++key;

                // column = pid
                String pid = this.jsonObjects_.get(i).optString("id", "null");
                ps.setString(key, pid); ++key;

                // column = is_submitter
                int submitter = this.jsonObjects_.get(i).optBoolean("is_submitter", false) ? 1 : 0;
                ps.setInt(key, submitter); ++key;

                // column = link_id
                String link_id = this.jsonObjects_.get(i).optString("link_id", "null");
                ps.setString(key, link_id); ++key;

                // column = parent_id
                String parent_id = this.jsonObjects_.get(i).optString("parent_id", "null");
                ps.setString(key, parent_id); ++key;

                // column = permalink
                String permalink = this.jsonObjects_.get(i).optString("permalink", "null");
                ps.setString(key, permalink); ++key;

                // column = scraped_on
                Long retrieved_on = this.jsonObjects_.get(i).optLong("retrieved_on", 0);
                String scraped_on = TimeFormatter.javaDateTimeToSQLDateTime(
                        TimeFormatter.utcToLDT(retrieved_on.toString())
                );
                ps.setString(key, scraped_on); ++key;

                // column = score
                int score = this.jsonObjects_.get(i).optInt("score", 0);
                ps.setInt(key, score); ++key;

                // column = is_stickied
                int sticked = this.jsonObjects_.get(i).optBoolean("stickied", false) ? 1 : 0;
                ps.setInt(key, sticked); ++key;

                // column = subreddit_name
                String sub_name = this.jsonObjects_.get(i).optString("subreddit", "null");
                ps.setString(key, sub_name); ++key;

                // column = subreddit_id
                String sub_id = this.jsonObjects_.get(i).optString("subreddit_id", "null");
                ps.setString(key, sub_id); ++key;

                // column = subreddit_type
                String sub_type = this.jsonObjects_.get(i).optString("subreddit_type", "null");
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
                TSL.get().err("CommentsJsonToDBWorker.parseAndPushDataToDB SQLException on conn.rollback()");
            }
        }
        finally{
            try{
                if(!conn.getAutoCommit())
                    conn.setAutoCommit(true);
            }
            catch(SQLException exp){
                TSL.get().err("CommentsJsonToDBWorker.parseAndPushDataToDB SQLException on conn.setAutoCommit(true)");
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
                    TSL.get().err("CommentsJsonToDBWorker.parseAndPushDataToDB SQLException on conn.close()");
                }
            }
        }
    }
}
