package edu.gbcg.dbInteraction.dbcreator.reddit;

import edu.gbcg.configs.Finals;
import edu.gbcg.dbInteraction.DBCommon;
import edu.gbcg.dbInteraction.dbcreator.IndexWorker;
import edu.gbcg.utils.FileUtils;
import edu.gbcg.utils.TSL;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

public abstract class Facilitator {
    protected List<String> DBAbsolutePaths;     // Path to the DBs once the exist
    protected List<String> DBDirectoryPaths;    // Path to the directory / directories that hold the DB shards
    protected List<String> columnNames;         // Names of the columns in the DB
    protected List<String> dataTypes;           // Type of data stored in the DB columns
    protected List<String> DBPaths;             // Paths to the DBs when they don't yet exist
    protected List<String> jsonAbsolutePaths;   // Paths to the json files
    protected List<String> jsonKeysOfInterest;  // JSON keys we care about grabbing for the DB
    protected String tableName;                 // The name of the table in the DB

    public Facilitator(){
        this.DBAbsolutePaths        = getDBAbsolutePaths();
        this.DBDirectoryPaths       = getDBDirectoryPaths();
        this.columnNames            = getColumnNames();
        this.dataTypes              = getDataTypes();
        this.DBPaths                = buildDBPaths();
        this.jsonAbsolutePaths      = getJsonAbsolutePaths();
        this.jsonKeysOfInterest     = getJsonKeysOfInterest();
        this.tableName              = getTableName();
    }

    // Used when the DBs don't exist, build the path to the DBs
    protected abstract List<String> buildDBPaths();
    protected abstract List<String> getJsonAbsolutePaths();
    protected abstract List<String> getDBAbsolutePaths();
    protected abstract List<String> getDBDirectoryPaths();
    protected abstract List<String> getDataTypes();
    protected abstract List<JsonPusher> populateJsonWorkers();
    protected abstract List<String> getJsonKeysOfInterest();
    protected abstract String getTableName();
    protected abstract void createIndices();
    protected abstract List<String> getColumnNames();

    public void createDBs(){
        // Check if the all the DBs exist. Note, this is 100% but it's good enough for my uses
        if(this.DBAbsolutePaths == null)
            this.DBAbsolutePaths = new ArrayList<>();
        boolean dbs_exist = this.DBAbsolutePaths.size() == Finals.DB_SHARD_NUM;

        // Early exist if the DBs exist and we don't want to start fresh
        if(dbs_exist && !Finals.START_FRESH)
            return;

        // The DBs exist but we want to start fresh, get rid of them
        if(dbs_exist && Finals.START_FRESH){
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

    public void pushJSONDataIntoDBs(){
        // Early exit if we're not pushing data
        if(!Finals.START_FRESH) return;

        if(DBAbsolutePaths == null || DBAbsolutePaths.isEmpty())
            DBAbsolutePaths = getDBAbsolutePaths();
        if(jsonAbsolutePaths == null || jsonAbsolutePaths.isEmpty())
            jsonAbsolutePaths = getJsonAbsolutePaths();

        // For each json file, read it line by line, while reading start processing the data
        // Each iteration of the loop adds a line to a new worker thread to evenly shard the data across all DB shards
        for(String json : jsonAbsolutePaths){
            File f = new File(json);
            TSL.get().info("Reading "+f.getName());

            BufferedReader br = null;
            // How many lines to read before writing to a DB shard
            int dbDumpLimit = Finals.DB_SHARD_NUM * Finals.DB_BATCH_LIMIT;
            int lineReadCounter = 0;
            int whichWorker = 0;

            List<List<String>> linesList = new ArrayList<>();
            for(int j = 0; j < Finals.DB_SHARD_NUM; ++j)
                linesList.add(new ArrayList<>());

            try{
                br = new BufferedReader(new FileReader(json));
                String line = br.readLine();
                while(line != null){
                    ++lineReadCounter;
                    linesList.get(whichWorker).add(line);

                    // Increment the worker number so we're evenly distributing lines to the workers
                    ++whichWorker;
                    if(whichWorker >= Finals.DB_SHARD_NUM)
                        whichWorker = 0;

                    // Limit before dumping data to the DB, start the threads and perform the dump
                    if(lineReadCounter >= dbDumpLimit){
                        letWorkersFly(linesList);

                        // Reset the trackers and clear the lines list to recover the memory from 5000 lines of JSON
                        lineReadCounter = 0;
                        linesList.clear();

                        // Give the linesList new ArrayLists to store the lines
                        for(int j = 0; j < Finals.DB_SHARD_NUM; ++j)
                            linesList.add(new ArrayList<>());
                    }
                    // Read a line
                    line = br.readLine();
                }

                // There could be leftover json lines that don't get push due to not meeting the dbDumpLimit amount
                // of lines, start up the workers again and push the remaining data
                letWorkersFly(linesList);
            }
            catch(IOException e){
                TSL.get().err("Facilitator.pushJSONDataIntoDBs IOException");
            }
            finally{
                if(br != null){
                    try{
                        br.close();
                    }
                    catch(IOException e){
                        TSL.get().err("Facilitator.pushJSONDataIntoDBs br.close IOException");
                    }
                }
            }
        }
        createIndices();
    }

    private void letWorkersFly(List<List<String>> lines){
        List<JsonPusher> workers = populateJsonWorkers();
        // Give the workers the data they need
        for(int i = 0; i < Finals.DB_SHARD_NUM; ++i){
            workers.get(i).setDB(DBAbsolutePaths.get(i));
            workers.get(i).setJSON(lines.get(i));
            workers.get(i).setColumns(columnNames);
            workers.get(i).setTableName(tableName);
        }

        // Start the threads
        List<Thread> threads = new ArrayList<>();
        for(int i = 0; i < Finals.DB_SHARD_NUM; ++i){
            threads.add(new Thread(workers.get(i)));
            threads.get(i).start();
        }

        // Wait for them all to finish
        for(int i = 0; i < Finals.DB_SHARD_NUM; ++i){
            try{
                threads.get(i).join();
            }
            catch(InterruptedException e){
                TSL.get().err("Facilitator.letWorkersFly InterruptedException");
                e.printStackTrace();
            }
        }
    }

    public void createDBIndex(String columnName, String indexName){
        List<Thread> workers = new ArrayList<>();
        List<Connection> conns = new ArrayList<>();
        String sql = DBCommon.getDBIndexSQLStatement(this.tableName, columnName, indexName);
        if(this.DBAbsolutePaths == null)
            this.DBAbsolutePaths = getDBAbsolutePaths();
        for(String db : this.DBAbsolutePaths)
            conns.add(DBCommon.connect(db));

        for(int i = 0; i < conns.size(); ++i){
            workers.add(new Thread(new IndexWorker(conns.get(i), sql)));
            workers.get(i).start();
        }

        try{
            for(int i = 0; i < workers.size(); ++i)
                workers.get(i).join();
        }
        catch(InterruptedException e){
            e.printStackTrace();
        }

        for(Connection conn : conns)
            DBCommon.disconnect(conn);
    }
}
