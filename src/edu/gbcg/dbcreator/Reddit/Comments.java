package edu.gbcg.dbcreator.Reddit;

import edu.gbcg.configs.DBLocator;
import edu.gbcg.configs.RawDataLocator;
import edu.gbcg.configs.StateVars;
import edu.gbcg.dbcreator.DBCommon;
import edu.gbcg.dbcreator.IndexWorker;
import edu.gbcg.utils.FileUtils;
import edu.gbcg.utils.TSL;
import edu.gbcg.utils.c;

import javax.swing.plaf.nimbus.State;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Comments {
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

    private static List<String> getColumnDataTypesForDB(){
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
        if(!StateVars.START_FRESH) return;

        // Get the absolute paths to the JSON comment data
        List<String> json_paths = RawDataLocator.redditJsonCommentAbsolutePaths();

        // The list of DBs should already be populated, but check again
        if(DBs == null || DBs.isEmpty())
            DBs = DBLocator.redditComsAbsolutePaths();

        for(int i = 0; i < json_paths.size(); ++i){
            String file = json_paths.get(i);
            File f = new File(file);
            c.writeln("Reading " + f.getName());

            BufferedReader br = null;
            int dump_to_db_limit = StateVars.DB_SHARD_NUM * StateVars.DB_BATCH_LIMIT;
            int line_read_counter = 0;
            int arr_ele_counter = 0;
            int dump_counter = 0;
            List<List<String>> lines_list = new ArrayList<>();
            List<CommentsJsonToDBWorker> com_workers = new ArrayList<>();
            for(int j = 0; j < StateVars.DB_SHARD_NUM; ++j){
                lines_list.add(new ArrayList<>());
                com_workers.add(new CommentsJsonToDBWorker());
            }

            try{
                br = new BufferedReader(new FileReader(file));
                String line = br.readLine();
                while(line != null){
                    ++line_read_counter;
                    lines_list.get(arr_ele_counter).add(line);

                    ++arr_ele_counter;
                    if(arr_ele_counter >= StateVars.DB_SHARD_NUM)
                        arr_ele_counter = 0;

                    if(line_read_counter >= dump_to_db_limit) {
                        c.writeln("Writing to DBs, dump #" + dump_counter);
                        ++dump_counter;

                        for(int j = 0; j < StateVars.DB_SHARD_NUM; ++j){
                            com_workers.get(j).setDB(DBs.get(j));
                            com_workers.get(j).setJSON(lines_list.get(j));
                            com_workers.get(j).setColumns(getColumnsForDB());
                            com_workers.get(j).setTableName(StateVars.COM_TABLE_NAME);
                        }

                        ArrayList<Thread> worker_threads = new ArrayList<>();
                        for(int j = 0; j < StateVars.DB_SHARD_NUM; ++j){
                            worker_threads.add(new Thread(com_workers.get(j)));
                            worker_threads.get(j).start();
                        }

                        for(int j = 0; j < StateVars.DB_SHARD_NUM; ++j){
                            try{
                                worker_threads.get(j).join();
                            }
                            catch(InterruptedException exp){
                                exp.printStackTrace();
                            }
                        }

                        line_read_counter = 0;
                        com_workers.clear();
                        lines_list.clear();
                        for(int j = 0; j < StateVars.DB_SHARD_NUM; ++j){
                            com_workers.add(new CommentsJsonToDBWorker());
                            lines_list.add(new ArrayList<>());
                        }
                    }
                    line = br.readLine();
                }

                c.writeln("Writing to DBs, dump #" + dump_counter);
                ArrayList<Thread> worker_ts = new ArrayList<>();
                for(int j = 0; j < StateVars.DB_SHARD_NUM; ++j){
                    com_workers.get(j).setDB(DBs.get(j));
                    com_workers.get(j).setJSON(lines_list.get(j));
                    com_workers.get(j).setColumns(getColumnsForDB());
                    com_workers.get(j).setTableName(StateVars.COM_TABLE_NAME);
                }

                for(int j = 0; j < StateVars.DB_SHARD_NUM; ++j){
                    worker_ts.add(new Thread(com_workers.get(j)));
                    worker_ts.get(j).start();
                }

                for(int j = 0; j < StateVars.DB_SHARD_NUM; ++j){
                    try{
                        worker_ts.get(j).join();
                    }
                    catch(InterruptedException exp){
                        exp.printStackTrace();
                    }
                }

            }
            catch(IOException e){
                TSL.get().err("Comments.pushJsonDataIntoDBs IOException on BufferedReader");
            }
            finally{
                if(br != null){
                    try{
                        br.close();
                    }
                    catch(IOException e){
                        TSL.get().err("Comments.pushJsonDataIntoDBs IOException on BufferedReader.close()");
                        e.printStackTrace();
                    }
                }
            }
        }
        // Create the indices
        createDBIndex("author", "attrs_author");
        createDBIndex("can_gild", "attrs_gild");
        createDBIndex("controversial_score", "attrs_cont_score");
        createDBIndex("created_dt", "attrs_created");
        createDBIndex("gilded", "attrs_gilded");
        createDBIndex("score", "attrs_score");
        createDBIndex("subreddit_name", "attrs_sub_name");
        createDBIndex("subreddit_id", "attrs_sub_id");
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
