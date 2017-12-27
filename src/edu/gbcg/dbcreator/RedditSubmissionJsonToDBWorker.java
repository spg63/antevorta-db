package edu.gbcg.dbcreator;

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
    private Map<String, Integer> kToIdx_ = null;

    public RedditSubmissionJsonToDBWorker(String dbPath, List<String> jsonLines,
                                          List<String> columnNames, List<String> jsonKeys,
                                          String tableName, Map<String, Integer> kToIdx){
        this.db_ = dbPath;
        this.json_strings_ = jsonLines;
        this.columns_ = columnNames;
        this.keys_ = jsonKeys;
        this.table_name_ = tableName;
        this.numObjects_ = this.json_strings_.size();
        this.kToIdx_ = kToIdx;
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
        c.writeln("line_count: " + line_count);
    }

    public void setDB(String db){ this.db_ = db; }
    public void setJSON(List<String> jsonLines){
        this.json_strings_ = jsonLines;
        this.numObjects_ = this.json_strings_.size();
    }
    public void setColumns(List<String> columns){ this.columns_ = columns; }
    public void setKeys(List<String> keys){ this.keys_ = keys; }
    public void setTableName(String tableName){ this.table_name_ = tableName; }
    public void setKeyToIdxMap(Map<String, Integer> m){ this.kToIdx_ = m; }

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

            // Loop through all of the jsonObjects
            for(int i = 0; i < numObjects_; ++i) {

                // Loop through all the columns, create the PreparedStatements, add them to the batch
                // NOTE: Starting the loop at 1 to skip the initial ID autoincrement field. Also
                // helps that PreparedStatements index starting at 1 instead of 0 like the rest of
                // the programming world
                for (int j = 1; j < this.columns_.size(); ++j) {
                    // Get archived
                    boolean archived_bool = this.jsonObjects_.get(i).getBoolean("archived");
                           // this.keys_.get(kToIdx_.get("archived"))
                    //);

                    String arch = this.jsonObjects_.get(i).get(this.keys_.get(0)).toString();
                    int archived = archived_bool ? 1 : 0;
                    c.writeln("arch: " + arch);
                    c.writeln("archived: " + archived);
                    System.exit(0);
                    ps.setInt(j, archived);


                    // Add the statement to the batch
                    ps.addBatch();
                }

                // Execute the batch update
                ps.executeBatch();

                // Commit
                conn.commit();
            }
        }
        catch(SQLException e){
            e.printStackTrace();
            try {
                // Roll things back if the batch failed
                conn.rollback();
            }
            catch(SQLException ex){
                ex.printStackTrace();
            }
        }
        finally{
            try{
                if(!conn.getAutoCommit())
                    conn.setAutoCommit(true);
            }
            catch(SQLException exp){
                exp.printStackTrace();
            }
            if(ps != null){
                try{
                    ps.close();
                }
                catch(SQLException ex){
                    ex.printStackTrace();
                }
            }
            if(conn != null){
                try{
                    conn.close();
                }
                catch(SQLException ex){
                    ex.printStackTrace();
                }
            }
        }


    }
}

