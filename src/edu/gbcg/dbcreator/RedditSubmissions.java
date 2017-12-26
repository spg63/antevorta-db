package edu.gbcg.dbcreator;

import edu.gbcg.configs.DBLocator;
import edu.gbcg.configs.RawDataLocator;
import edu.gbcg.configs.StateVars;
import edu.gbcg.utils.FileUtils;
import edu.gbcg.utils.c;
import org.json.JSONObject;

import java.io.File;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Class to interact with the reddit submission data. This class will allow for all required
 * functionality from DB creation, inserting the data, updating the data and selecting the data.
 * This class will automatically determine the number of DB shards and their storage location for
 * the user. The sharding will allow for parallel reads and writes to multiple HDDs / compute nodes.
 */
public class RedditSubmissions {
    private static List<String> DBs = DBLocator.redditSubsAbsolutePaths();
    private static final String SUB_TABLE_NAME = "submission_attrs";

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
            String sql = "drop table if exists " + SUB_TABLE_NAME + ";";
            for(String DB : DBs)
                DBCommon.delete(DB, sql);
        }

        // The DBs don't exist, we need to create them
        if(!dbs_exist){
            File f = new File(DBLocator.getSubDBPath());
            f.mkdirs();
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
        create.append("create table if not exists "+ SUB_TABLE_NAME + "(");
        for(int i = 0; i < col_names.size(); ++i){
            create.append(col_names.get(i));
            create.append(data_types.get(i));
        }
        create.append(");");
        String sql = create.toString();

        // Create the table in each DB
        for(String db : DBs)
            DBCommon.insert(db, sql);
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

    /*
        ** NO JAVADOC **
        * The column names we care about. Based on the above key names. There are extra integer,
        * real, and text based columns built-in for future expansion without re-reading the data
        * in to create a new table.
     */
    private static List<String> getColumnsForDB(){
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
                " TEXT",                                " INTEGER DEFAULT 0,",  " TEXT,",
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
     * Will write the json data the the database shards
     */
    public static void writeJsonToDBs(){
        List<JSONObject> objects = getJsonObjects();
        String key = "selftext";
        for(JSONObject jo : objects)
            c.writeln(key + ": " + jo.get(key).toString());

    }

    /*
        ** NO JAVADOC **
        * Reads in the Json objects
     */
    private static List<JSONObject> getJsonObjects(){
        List<String> data_paths = RawDataLocator.redditJsonSubmissionAbsolutePaths();
        String tenline = data_paths.get(2);
        List<String> jsonStrings = FileUtils.getInstance().readLineByLine(tenline);

        List<JSONObject> objects = new ArrayList<>();
        for(String line : jsonStrings)
            objects.add(new JSONObject(line));

        return objects;
    }
}
