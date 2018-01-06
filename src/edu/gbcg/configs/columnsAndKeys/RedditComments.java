package edu.gbcg.configs.columnsAndKeys;

import edu.gbcg.configs.Finals;

import java.util.Arrays;
import java.util.List;

public class RedditComments {
    public static List<String> JSONkeys(){
        return Arrays.asList(
                "author",           "author_flair_text",    "body",
                "can_gild",         "controversiality",     "created_utc",
                "distinguished",    "edited",               "gilded",
                "id",               "is_submitter",         "link_id",
                "parent_id",        "permalink",            "retrieved_on",
                "score",            "stickied",             "subreddit",
                "subreddit_id",     "subreddit_type"
        );
    }

    public static List<String> columnNames(){
        return Arrays.asList(
                "ID",               Finals.AUTHOR,      "author_flair_text",
                Finals.BODY,        "can_gild",         "controversial_score",
                Finals.CREATED_DT,  "distinguished",    "been_edited",
                "gilded",           Finals.POST_ID,     "is_submitter",
                "link_id",          "parent_id",        Finals.PERMALINK,
                Finals.SCRAPED_DT,  Finals.SCORE,       "is_stickied",
                "subreddit_name",   "subreddit_id",     "subreddit_type",
                "intg_exp_1",       "real_exp_1",       "text_exp_1",
                "text_exp_2",       "text_exp_3",       "text_exp_4"
        );
    }

    public static List<String> columnsForPrinting(){
        return Arrays.asList(
                "ID",               Finals.AUTHOR,      "author_flair_text",
                Finals.BODY,        "can_gild",         "controversial_score",
                Finals.CREATED_DT,  "distinguished",    "been_edited",
                "gilded",           Finals.POST_ID,     "is_submitter",
                "link_id",          "parent_id",        Finals.PERMALINK,
                Finals.SCRAPED_DT,  Finals.SCORE,       "is_stickied",
                "subreddit_name",   "subreddit_id",     "subreddit_type"
        );
    }

    public static List<String> dataTypes(){
        return Arrays.asList(
                " INTEGER PRIMARY KEY AUTOINCREMENT,",  " TEXT,",               " TEXT,",
                " TEXT,",                               " INTEGER DEFAULT 0,",  " INTEGER DEFAULT 0,",
                " DATETIME,",                           " TEXT,",               " INTEGER DEFAULT 0,",
                " INTEGER DEFAULT 0,",                  " TEXT,",               " INTEGER DEFAULT 0,",
                " TEXT,",                               " TEXT,",               " TEXT,",
                " DATETIME,",                           " INTEGER DEFAULT 0,",  " INTEGER DEFAULT 0,",
                " TEXT,",                               " TEXT,",               " TEXT,",
                " INTEGER,",                            " REAL,",               " TEXT,",
                " TEXT,",                               " TEXT,",               " TEXT"
        );
    }
}
