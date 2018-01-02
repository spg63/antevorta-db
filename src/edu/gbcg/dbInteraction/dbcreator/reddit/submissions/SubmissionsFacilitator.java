package edu.gbcg.dbInteraction.dbcreator.reddit.submissions;

import edu.gbcg.configs.DBLocator;
import edu.gbcg.configs.RawDataLocator;
import edu.gbcg.configs.StateVars;
import edu.gbcg.dbInteraction.dbcreator.reddit.Facilitator;
import edu.gbcg.dbInteraction.dbcreator.reddit.JsonPusher;

import java.util.*;

/**
 * Class to interact with the reddit submission data. This class will allow for all required
 * functionality from DB creation, inserting the data, updating the data and selecting the data.
 * This class will automatically determine the number of DB shards and their storage location for
 * the user. The sharding will allow for parallel reads and writes to multiple HDDs / compute nodes.
 */
public class SubmissionsFacilitator extends Facilitator {
    public SubmissionsFacilitator(){ super(); }

    protected List<String> buildDBPaths(){
        return DBLocator.buildSubDBPaths();
    }

    protected List<String> getJsonAbsolutePaths(){
        return RawDataLocator.redditJsonSubmissionAbsolutePaths();
    }

    protected List<String> getDBAbsolutePaths(){
        return DBLocator.redditSubsAbsolutePaths();
    }

    protected List<String> getDBDirectoryPaths(){
        return DBLocator.getSubDBPath();
    }

    protected List<String> getJsonKeysOfInterest(){
        ArrayList<String> keys = new ArrayList<>(Arrays.asList(
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
        return keys;
    }

    public List<String> getColumnNames(){
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

    protected List<String> getDataTypes(){
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

    protected List<JsonPusher> populateJsonWorkers(){
        List<JsonPusher> workers = new ArrayList<>();
        for(int i = 0; i < StateVars.DB_SHARD_NUM; ++i)
            workers.add(new SubmissionJsonPusher());
        return workers;
    }

    protected String getTableName(){
        return StateVars.SUB_TABLE_NAME;
    }

    protected void createIndices(){
        createDBIndex("author", "attrs_author");
        createDBIndex("created_dt", "attrs_created");
        createDBIndex("host_domain", "attrs_host");
        createDBIndex("gilded", "attrs_gilded");
        createDBIndex("num_comments", "attrs_comments");
        createDBIndex("media_provider_name", "attrs_med_provider");
        createDBIndex("media_title", "attrs_media");
        createDBIndex("over_18", "attrs_18");
        createDBIndex("score", "attrs_score");
        createDBIndex("selftext", "attrs_self");
        createDBIndex("subreddit_name", "attrs_sub_name");
        createDBIndex("subreddit_id", "attrs_sub_id");
        createDBIndex("post_title", "attrs_post_title");
    }
}
