package edu.gbcg.dbcreator;

import edu.gbcg.utils.c;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;

public class RedditSubmissionJsonToDBWorker implements Runnable{
    private String db_;
    private List<String> json_strings_;
    private List<String> columns_;
    private List<String> keys_;
    private String table_name_;
    private static int line_count = 0;

    public RedditSubmissionJsonToDBWorker(String dbPath, List<String> jsonLines,
                                          List<String> columnNames, List<String> jsonKeys,
                                          String tableName){
        this.db_ = dbPath;
        this.json_strings_ = jsonLines;
        this.columns_ = columnNames;
        this.keys_ = jsonKeys;
        this.table_name_ = tableName;
    }

    public RedditSubmissionJsonToDBWorker(){}

    public void run(){
        line_count += json_strings_.size();
        c.writeln("line_count: " + line_count);
    }

    public void setDB(String db){ this.db_ = db; }
    public void setJSON(List<String> jsonLines){ this.json_strings_ = jsonLines; }
    public void setColumns(List<String> columns){ this.columns_ = columns; }
    public void setKeys(List<String> keys){ this.keys_ = keys; }
    public void setTableName(String tableName){ this.table_name_ = tableName; }
}

