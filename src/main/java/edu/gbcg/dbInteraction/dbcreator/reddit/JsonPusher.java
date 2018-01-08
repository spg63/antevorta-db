/*
 * Copyright (c) 2018 Sean Grimes. All rights reserved.
 * License: MIT License
 */

package edu.gbcg.dbInteraction.dbcreator.reddit;

import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public abstract class JsonPusher implements Runnable{
    protected String db;
    protected List<String> json_strings;
    protected List<String> columns;
    protected String table_name;
    protected List<JSONObject> json_objects;
    protected int num_objects;

    public JsonPusher(){
        this.json_objects = new ArrayList<>();
    }
    public JsonPusher(String dbPath, List<String> jsonLines, List<String> columnNames, String tableName){
        this.db = dbPath;
        this.json_strings = jsonLines;
        this.columns = columnNames;
        this.table_name = tableName;
        this.num_objects = this.json_strings.size();
        this.json_objects = new ArrayList<>();
    }

    public void run(){
        for(int i = 0; i < this.num_objects; ++i)
            this.json_objects.add(new JSONObject(this.json_strings.get(i)));

        parseAndPushDataToDB();
    }

    public void setDB(String db){ this.db = db; }
    public void setColumns(List<String> columns){ this.columns = columns; }
    public void setTableName(String tableName){ this.table_name = tableName; }
    public void setJSON(List<String> jsonLines){
        this.json_strings = jsonLines;
        this.num_objects = this.json_strings.size();
    }

    /**
     * This is called from parseAndPushDataToDB. It will build the insertion string used by the PreparedStatement in
     * parseAndPushDataToDB
     * @return
     */
    protected String buildInsertionString(){
        StringBuilder sb = new StringBuilder();
        sb.append("INSERT INTO ");
        sb.append(this.table_name);
        sb.append(" (");

        // Loop through all column names and add them to the insert string
        // NOTE: Starting the loop at 1 to skip the initial ID autoincrement field
        // Skip the last one to avoid an extra comma
        for(int i = 1; i < this.columns.size() - 1; ++i){
            sb.append(this.columns.get(i));
            sb.append(",");
        }
        sb.append(this.columns.get(this.columns.size() - 1));

        // Start the values string
        sb.append(") VALUES (");
        for(int i = 1; i < this.columns.size() - 1; ++i)
            sb.append("?,");
        sb.append("?);");

        return sb.toString();
    }

    // What needs to be implemented for each DB type
    protected abstract void parseAndPushDataToDB();
}
