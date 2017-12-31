package edu.gbcg.dbcreator.Reddit;

import edu.gbcg.configs.DBLocator;
import edu.gbcg.configs.StateVars;
import edu.gbcg.dbcreator.DBCommon;
import edu.gbcg.dbcreator.IndexWorker;
import edu.gbcg.utils.FileUtils;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RedditComments {
    private static List<String> DBs = DBLocator.redditComsAbsolutePaths();

    public static void createDBs(){
        // Check if all the DBs exist. Note this should be done better to be certain they exist but lets skip the
        // extra work for now
        if(DBs == null)
            DBs = new ArrayList<>();
        boolean dbs_exist = DBs.size() == StateVars.DB_SHARD_NUM;

        // Early exit if the DBs exist and we're not starting fresh
        if(dbs_exist && !StateVars.START_FRESH)
            return;

        // The DBs exist but we want to start fresh, blow them up
        if(dbs_exist && StateVars.START_FRESH){
            String sql = "drop table if exists " + StateVars.COM_TABLE_NAME + ";";
            for(int i = 0; i < DBs.size(); ++i)
                DBCommon.delete(DBs.get(i), sql);
        }

        // The DBs don't exist, we need to create them
        if(!dbs_exist){
            List<String> dir_paths = DBLocator.getComDBPath();
            for(String dir_path : dir_paths)
                FileUtils.get().checkAndCreateDir(dir_path);
            List<String> paths = DBLocator.buildComDBPaths();
            for(String DB : paths){
                Connection conn = DBCommon.connect(DB);
                DBCommon.disconnect(conn);
            }
            // Now they exist, go and fine them
            DBs = DBLocator.redditComsAbsolutePaths();
        }

        // Get a list of all column names
        List<String> col_names = getColumnsForDB();
        // Get a list of all column data types
        List<String> data_types = getColumnDataTypesForDB();

        // Create the table schema
        StringBuilder create = new StringBuilder();
        create.append("create table if not exists "+StateVars.COM_TABLE_NAME+"(");
        for(int i = 0; i < col_names.size(); ++i){
            create.append(col_names.get(i));
            create.append(data_types.get(i));
        }
        create.append(");");
        String sql = create.toString();

        // Create the table in each DB
        for(int i = 0; i < DBs.size(); ++i)
            DBCommon.insert(DBs.get(i), sql);
    }

    private static List<String> keysOfInterest = new ArrayList<>(Arrays.asList(
            "author",           "author_flair_text",    "body",
            "can_gild",         "controversiality",     "created_utc",
            "distinguished",    "edited",               "gilded",
            "id",               "is_submitter",         "link_id",
            "parent_id",        "permalink",            "retrieved_on",
            "score",            "stickied",             "subreddit",
            "subreddit_id",     "subreddit_type"
    ));

    public static List<String> getColumnsForDB(){
        ArrayList<String> columns = new ArrayList<>(Arrays.asList(
                "ID",               "author",           "author_flair_text",
                "body",             "can_gild",         "controversial_score",
                "created_dt",       "distinguished",    "been_edited",
                "gilded",           "pid",              "is_submitter",
                "link_id",          "parent_id",        "permalink",
                "scraped_on",       "score",            "is_stickied",
                "subreddit_name",   "subreddit_id",     "subreddit_type",
                "intg_exp_1",       "real_exp_1",       "text_exp_1",
                "text_exp_2",       "text_exp_3",       "text_exp_4"
        ));
        return columns;
    }

    public static List<String> getColumnDataTypesForDB(){
        ArrayList<String> data_types = new ArrayList<>(Arrays.asList(
                " INTEGER PRIMARY KEY AUTOINCREMENT,",  " TEXT,",               " TEXT,",
                " TEXT,",                               " INTEGER DEFAULT 0,",  " INTEGER DEFAULT 0,",
                " DATETIME,",                           " TEXT,",               " INTEGER DEFAULT 0,",
                " INTEGER DEFAULT 0,",                  " TEXT,",               " INTEGER DEFAULT 0,",
                " TEXT,",                               " TEXT,",               " TEXT,",
                " DATETIME,",                           " INTEGER DEFAULT 0,",  " INTEGER DEFAULT 0,",
                " TEXT,",                               " TEXT,",               " TEXT,",
                " INTEGER,",                            " REAL,",               " TEXT,",
                " TEXT,",                               " TEXT,",               " TEXT"
        ));
        return data_types;
    }

    public static void pushJSSONDataIntoDBs(){

    }

    public static void createDBIndex(String columnName, String indexName){
        List<Thread> idx_workers = new ArrayList<>();
        List<Connection> conns = new ArrayList<>();
        String index_string = DBCommon.getDBIndexSQLStatement(StateVars.COM_TABLE_NAME, columnName, indexName);
        if(DBs == null)
            DBs = DBLocator.redditComsAbsolutePaths();
        for(String db : DBs)
            conns.add(DBCommon.connect(db));

        for(int i = 0; i < conns.size(); ++i){
            idx_workers.add(new Thread(new IndexWorker(conns.get(i), index_string)));
            idx_workers.get(i).start();
        }

        try{
            for(int i = 0; i < idx_workers.size(); ++i)
                idx_workers.get(i).join();
        }
        catch(InterruptedException e){
            e.printStackTrace();
        }

        for(Connection conn : conns)
            DBCommon.disconnect(conn);
    }
}























