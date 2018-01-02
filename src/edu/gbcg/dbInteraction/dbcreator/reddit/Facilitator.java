package edu.gbcg.dbInteraction.dbcreator.reddit;

import edu.gbcg.configs.StateVars;
import edu.gbcg.dbInteraction.DBCommon;
import edu.gbcg.utils.FileUtils;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

public abstract class Facilitator {
    protected List<String> DBAbsolutePaths;     // Path to the DBs once the exist
    protected String tableName;                 // The name of the table in the DB
    protected List<String> DBDirectoryPaths;    // Path to the directory / directories that hold the DB shards
    protected List<String> columnNames;         // Names of the columns in the DB
    protected List<String> dataTypes;           // Type of data stored in the DB columns
    protected List<String> DBPaths;             // Paths to the DBs when they don't yet exist
    protected List<String> jsonAbsolutePaths;   // Paths to the json files

    // Used when the DBs don't exist, build the path to the DBs
    abstract List<String> buildDBPaths();           // c'tor call
    abstract List<String> getJsonAbsolutePaths();   // c'tor call
    abstract List<String> getDBAbsolutePaths();     // c'tor call
    abstract List<String> getDBDirectoryPaths();    // c'tor call
    abstract List<String> getColumnNames();         // c'tor call
    abstract List<String> getDateTypes();           // c'tor call

    public void createDBs(){
        // Check if the all the DBs exist. Note, this is 100% but it's good enough for my uses
        if(this.DBAbsolutePaths == null)
            this.DBAbsolutePaths = new ArrayList<>();
        boolean dbs_exist = this.DBAbsolutePaths.size() == StateVars.DB_SHARD_NUM;

        // Early exist if the DBs exist and we don't want to start fresh
        if(dbs_exist && !StateVars.START_FRESH)
            return;

        // The DBs exist but we want to start fresh, get rid of them
        if(dbs_exist && StateVars.START_FRESH){
            String sql = "drop table if exists " + this.tableName + ";";
            for(String dbPath : this.DBAbsolutePaths)
                DBCommon.delete(dbPath, sql);
        }

        // The DBs don't exist, create them
        if(!dbs_exist){
            // Create the directories that hold the DBs
            for(String path : DBDirectoryPaths)
                FileUtils.get().checkAndCreateDir(path);
            // Build the paths to the DBs so they can be created
            for(String path : DBPaths){
                Connection conn = DBCommon.connect(path);
                DBCommon.disconnect(conn);
            }
            // Now they exist, populate the path data structure
            DBAbsolutePaths = getDBAbsolutePaths();
        }

        // Create the table schema
        StringBuilder sb = new StringBuilder();
        sb.append("create table if not exists "+this.tableName+"(");
        for(int i = 0; i < this.columnNames.size(); ++i){
            sb.append(this.columnNames.get(i));
            sb.append(this.dataTypes.get(i));
        }
        sb.append(");");
        String sql = sb.toString();

        // Create the table in the DB shards
        for(String DB : DBAbsolutePaths)
            DBCommon.insert(DB, sql);
    }
}









































