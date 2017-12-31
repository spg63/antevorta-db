package edu.gbcg.dbcreator.Reddit;

import edu.gbcg.dbcreator.DBCommon;
import edu.gbcg.utils.TSL;
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
