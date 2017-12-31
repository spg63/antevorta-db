package edu.gbcg.dbcreator;

import edu.gbcg.configs.DBLocator;
import edu.gbcg.configs.RawDataLocator;
import edu.gbcg.configs.StateVars;
import edu.gbcg.utils.FileUtils;
import edu.gbcg.utils.TSL;
import edu.gbcg.utils.c;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.util.*;

/**
 * Class to interact with the reddit submission data. This class will allow for all required
 * functionality from DB creation, inserting the data, updating the data and selecting the data.
 * This class will automatically determine the number of DB shards and their storage location for
 * the user. The sharding will allow for parallel reads and writes to multiple HDDs / compute nodes.
 */
public class RedditSubmissions {
    private static List<String> DBs = DBLocator.redditSubsAbsolutePaths();

    /**
     * Creates the reddit submission databases. The number of databases and their storage
     * location are based on values found in StateVars and DataPaths. The databases are setup
     * for round-robin based sharding to allow for storage on parallel HDDs for increased
     * performance.
     */
    public static void createDBs(){
        // Check if all the DBs exist. Note this could be done to be certain they exist but lets
        // skip the extra work for now
        if(DBs == null)
            DBs = new ArrayList<>();
        boolean dbs_exist = DBs.size() == StateVars.DB_SHARD_NUM;

        // Early exit if the DBs exist and we're not starting fresh
        if(dbs_exist && !StateVars.START_FRESH)
            return;

        // The DBs exist but we want to start fresh, blow them up
        if(dbs_exist && StateVars.START_FRESH){
            String sql = "drop table if exists " + StateVars.SUB_TABLE_NAME + ";";
            for(int i = 0; i < DBs.size(); ++i) {
                DBCommon.delete(DBs.get(i), sql);
            }
        }

        // The DBs don't exist, we need to create them
        if(!dbs_exist){
            List<String> dir_paths = DBLocator.getSubDBPath();
            for(String dir_path : dir_paths)
                FileUtils.get().checkAndCreateDir(dir_path);
            List<String> paths = DBLocator.buildSubDBPaths();
            for(String DB : paths) {
                Connection conn = DBCommon.connect(DB);
                DBCommon.disconnect(conn);
            }
            // Now that they exist, go and find them
            DBs = DBLocator.redditSubsAbsolutePaths();
        }

        // Get a list of all column names
        List<String> col_names = getColumnsForDB();
        // Get a list of all column data types
        List<String> data_types = getColumnDataTypesForDB();

        // Create the table schema
        StringBuilder create = new StringBuilder();
        create.append("create table if not exists "+ StateVars.SUB_TABLE_NAME + "(");
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

    /*
        ** NO JAVADOC **
        * The keys we care about from the json data. There isn't a 1-to-1 matchup on column names
        * but they're close for the most part. You'll notice the media object in the center;
        * this links to 2 inner objects which contains the data we actually care about
     */
    private static List<String> keysOfInterest = new ArrayList<>(Arrays.asList(
            "archived",         "author",           "brand_safe",
            "contest_mode",     "created_utc",      "distinguished",
            "domain",           "edited",           "gilded",
            "hidden",           "id",               "is_self",
            "is_video",         "link_flair_text",  "locked",
            "num_comments",

            // Note: Here we have an inner object
            "media",
            // The inner object
            "oembed",
            // The stuff we care about is inside the inner object
            "author_name",
            "provider_name",
            "title",
            "type",
            // END INNER

            "num_crossposts",   "over_18",          "permalink",
            "pinned",           "retrieved_on",     "score",
            "selftext",         "stickied",         "subreddit",
            "subreddit_id",     "subreddit_type",   "title",        "url"
    ));

    private static Map<String, Integer> keyToIdx(){
        Map<String, Integer> key_to_idx = new HashMap<>();
        for(int i = 0; i < keysOfInterest.size(); ++i){
            key_to_idx.put(keysOfInterest.get(i), i);
        }
        return key_to_idx;
    }

    /*
        ** NO JAVADOC **
        * The column names we care about. Based on the above key names. There are extra integer,
        * real, and text based columns built-in for future expansion without re-reading the data
        * in to create a new table.
     */
    public static List<String> getColumnsForDB(){
        ArrayList<String> columns = new ArrayList<>(Arrays.asList(
                "ID",                   "archived",             "author",
                "brand_safe",           "contest_mode",         "created_dt",
                "distinguished",        "host_domain",          "edited",
                "gilded",               "hidden",               "pid",
                "is_self_post",         "is_video_post",        "link_flair_text",
                "is_locked",            "num_comments",         "media_author_name",
                "media_provider_name",  "media_title",          "media_type",
                "num_crossposts",       "over_18",              "permalink",
                "is_pinned",            "scraped_on",           "score",
                "selftext",             "is_stickied",          "subreddit_name",
                "subreddit_id",         "subreddit_type",       "post_title",
                "link_url",             "intg_exp_1",           "intg_exp_2",
                "intg_exp_3",           "real_exp_1",           "real_exp_2",
                "text_exp_1",           "text_exp_2",           "text_exp_3",
                "text_exp_4",           "text_exp_5",           "text_exp_6",
                "text_exp_7",           "text_exp_8",           "text_exp_9"
        ));
        return columns;
    }

    /*
        ** NO JAVADOC **
        * The datatypes associated with the above columns. Straightforward. However, do note that
        * sqlite doesn't support a boolean datatype and is being represented with an integer. The
        * value defaults to 0. Datetype will be represented by an sql time string. Datetime
        * objects can be converted to and from java LocalDateTime objects in the TimeFormatter
        * class
     */
    private static List<String> getColumnDataTypesForDB(){
        List<String> data_types = new ArrayList<>(Arrays.asList(
                " INTEGER PRIMARY KEY AUTOINCREMENT,",  " INTEGER DEFAULT 0,",  " TEXT,",
                " INTEGER DEFAULT 0,",                  " INTEGER DEFAULT 0,",  " DATETIME,",
                " TEXT,",                               " TEXT,",               " INTEGER DEFAULT 0,",
                " INTEGER,",                            " INTEGER DEFAULT 0,",  " TEXT,",
                " INTEGER DEFAULT 0,",                  " INTEGER DEFAULT 0,",  " TEXT,",
                " INTEGER DEFAULT 0,",                  " INTEGER,",            " TEXT,",
                " TEXT,",                               " TEXT,",               " TEXT,",
                " INTEGER,",                            " INTEGER DEFAULT 0,",  " TEXT,",
                " INTEGER DEFAULT 0,",                  " DATETIME,",           " INTEGER,",
                " TEXT,",                               " INTEGER DEFAULT 0,",  " TEXT,",
                " TEXT,",                               " TEXT,",               " TEXT,",
                " TEXT,",                               " INTEGER,",            " INTEGER,",
                " INTEGER,",                            " REAL,",               " REAL,",
                " TEXT,",                               " TEXT,",               " TEXT,",
                " TEXT,",                               " TEXT,",               " TEXT,",
                " TEXT,",                               " TEXT,",               " TEXT"
        ));
        return data_types;
    }

    /**
     * Reads all JSON reddit submission data and pushes it into the various DBs
     */
    public static void pushJSONDataIntoDBs(){
        // If we're not starting fresh then get the hell out of here
        if(!StateVars.START_FRESH) return;

        // Get the absolute paths to the JSON submission data
        List<String> json_paths = RawDataLocator.redditJsonSubmissionAbsolutePaths();

        // The list of DBs should already be populated by the createDBs() function, but check again
        if(DBs == null || DBs.isEmpty())
            DBs = DBLocator.redditSubsAbsolutePaths();

        // For each file, read it line by line, and while reading it line by line start
        // processing the data. The files are often too large to read entirely into memory in one
        // shot
        for(int i = 0; i < json_paths.size(); ++i){
            String file = json_paths.get(i);
            File f = new File(file);
            c.writeln("Reading " + f.getName());

            BufferedReader br = null;
            // Once we've read this many lines we start the DB writers and push everything into
            // the DB
            int dump_to_db_limit = StateVars.DB_SHARD_NUM * StateVars.DB_BATCH_LIMIT;
            int line_read_counter = 0;
            int arr_ele_counter = 0;
            int dump_counter = 1;
            List<List<String>> lines_list = new ArrayList<>();
            List<RedditSubmissionJsonToDBWorker> sub_workers = new ArrayList<>();
            List<Thread> workers = new ArrayList<>();
            for(int j = 0; j < StateVars.DB_SHARD_NUM; ++j) {
                lines_list.add(new ArrayList<>());
                sub_workers.add(new RedditSubmissionJsonToDBWorker());
            }

            try{
                br = new BufferedReader(new FileReader(file));
                String line = br.readLine();
                while(line != null){
                    ++line_read_counter;

                    lines_list.get(arr_ele_counter).add(line);

                    // Increment the array we're putting strings in. When we hit the limit reset
                    // to the first array
                    ++arr_ele_counter;
                    if(arr_ele_counter >= StateVars.DB_SHARD_NUM)
                        arr_ele_counter = 0;

                    // We've reached the limit before dumping the data into the DB. Now start the
                    // threads, push the data into the DB, wait on all threads to finish, reseat
                    // the thread list objects and reset the counter
                    if(line_read_counter >= dump_to_db_limit){
                        c.writeln("Writing to DBs, dump #" + dump_counter);
                        ++dump_counter;

                        // Setup the worker threads with the proper data
                        for(int j = 0; j < StateVars.DB_SHARD_NUM; ++j){
                            sub_workers.get(j).setDB(DBs.get(j));
                            sub_workers.get(j).setJSON(lines_list.get(j));
                            sub_workers.get(j).setColumns(getColumnsForDB());
                            sub_workers.get(j).setKeys(keysOfInterest);
                            sub_workers.get(j).setTableName(StateVars.SUB_TABLE_NAME);
                        }

                        ArrayList<Thread> worker_threads = new ArrayList<>();

                        // Workers have been set, launch all the workers
                        for(int j = 0; j < StateVars.DB_SHARD_NUM; ++j){
                            worker_threads.add(new Thread(sub_workers.get(j)));
                            worker_threads.get(j).start();
                        }

                        // Wait for all the threads to finish
                        for(int j = 0; j < StateVars.DB_SHARD_NUM; ++j) {
                            try {
                                worker_threads.get(j).join();
                            }
                            catch(InterruptedException exp){
                                exp.printStackTrace();
                            }
                        }

                        // Reset the trackers: line_read_counter back to zero, clear the workers
                        // now that they've completed their work, clear the lines array list now
                        // that the data has been put in the DB. Finally add new workers to the
                        // workers array list for the next iterations and add new array lists to
                        // the lines list to store new data from the file
                        line_read_counter = 0;
                        sub_workers.clear();
                        lines_list.clear();
                        for(int j = 0; j < StateVars.DB_SHARD_NUM; ++j){
                            sub_workers.add(new RedditSubmissionJsonToDBWorker());
                            lines_list.add(new ArrayList<>());
                        }
                    }

                    line = br.readLine();
                }

                // Push the data to get any and all of the leftover data from the previous list
                // that hasn't already been pushed into the DB
                c.writeln("Writing to DBs, dump #" + dump_counter);
                ArrayList<Thread> worker_ts = new ArrayList<>();
                for(int j = 0; j < StateVars.DB_SHARD_NUM; ++j){
                    sub_workers.get(j).setDB(DBs.get(j));
                    sub_workers.get(j).setJSON((lines_list.get(j)));
                    sub_workers.get(j).setColumns(getColumnsForDB());
                    sub_workers.get(j).setKeys(keysOfInterest);
                    sub_workers.get(j).setTableName(StateVars.SUB_TABLE_NAME);
                }

                for(int j = 0; j < StateVars.DB_SHARD_NUM; ++j){
                    worker_ts.add(new Thread(sub_workers.get(j)));
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
                TSL.get().err("RedditSubmission.pushJsonDataIntoDBs IOException on BufferedReader");
            }
            finally{
                if(br != null){
                    try{
                        br.close();
                    }
                    catch(IOException e){
                        TSL.get().err("RedditSubmission.pushJsonDataIntoDBs IOException on " +
                                        "BufferedReader.close()");
                        e.printStackTrace();
                    }
                }
            }
        }
        // The DBs have been created, now create the usual indicies for quicker queries
        createDBIndex(StateVars.SUB_TABLE_NAME, "author", "attrs_author");
    }

    public static void createDBIndex(String tableName, String columnName, String indexName){
        List<Thread> idx_workers = new ArrayList<>();
        List<Connection> conns = new ArrayList<>();
        String index_string = DBCommon.getDBIndexSQLStatement(tableName, columnName, indexName);
        if(DBs == null)
            DBs = DBLocator.redditSubsAbsolutePaths();
        for(String db : DBs)
            conns.add(DBCommon.connect(db));

        for(int i = 0; i < conns.size(); ++i) {
            idx_workers.add(new Thread(new IndexWorker(conns.get(i), index_string)));
            idx_workers.get(i).start();
        }

        try {
            for (int i = 0; i < idx_workers.size(); ++i)
                idx_workers.get(i).join();
        }
        catch(InterruptedException e){
            e.printStackTrace();
        }

        // Close the DB connections
        for(Connection conn : conns)
            DBCommon.disconnect(conn);
    }
}
