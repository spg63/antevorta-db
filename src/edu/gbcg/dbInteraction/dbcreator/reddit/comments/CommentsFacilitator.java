package edu.gbcg.dbInteraction.dbcreator.reddit.comments;

import edu.gbcg.configs.DBLocator;
import edu.gbcg.configs.RawDataLocator;
import edu.gbcg.configs.StateVars;
import edu.gbcg.dbInteraction.dbcreator.reddit.Facilitator;
import edu.gbcg.dbInteraction.dbcreator.reddit.JsonPusher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommentsFacilitator extends Facilitator{
    public CommentsFacilitator(){ super(); }

    protected List<String> buildDBPaths(){
        return DBLocator.buildComDBPaths();
    }

    protected List<String> getJsonAbsolutePaths(){
        return RawDataLocator.redditJsonCommentAbsolutePaths();
    }

    protected List<String> getDBAbsolutePaths(){
        return DBLocator.redditComsAbsolutePaths();
    }

    protected List<String> getDBDirectoryPaths(){
        return DBLocator.getComDBPath();
    }

    protected List<String> getJsonKeysOfInterest(){
        ArrayList<String> keys = new ArrayList<>(Arrays.asList(
                "author",           "author_flair_text",    "body",
                "can_gild",         "controversiality",     "created_utc",
                "distinguished",    "edited",               "gilded",
                "id",               "is_submitter",         "link_id",
                "parent_id",        "permalink",            "retrieved_on",
                "score",            "stickied",             "subreddit",
                "subreddit_id",     "subreddit_type"
        ));
        return keys;
    }

    public List<String> getColumnNames(){
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

    protected List<String> getDataTypes(){
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

    protected List<JsonPusher> populateJsonWorkers(){
        List<JsonPusher> workers = new ArrayList<>();
        for(int i = 0; i < StateVars.DB_SHARD_NUM; ++i)
            workers.add(new CommentsJsonPusher());
        return workers;
    }

    protected String getTableName(){
        return StateVars.COM_TABLE_NAME;
    }

    protected void createIndices(){
        createDBIndex("author", "attrs_author");
        createDBIndex("can_gild", "attrs_gild");
        createDBIndex("controversial_score", "attrs_cont_score");
        createDBIndex("created_dt", "attrs_created");
        createDBIndex("gilded", "attrs_gilded");
        createDBIndex("score", "attrs_score");
        createDBIndex("subreddit_name", "attrs_sub_name");
        createDBIndex("subreddit_id", "attrs_sub_id");
    }
}
