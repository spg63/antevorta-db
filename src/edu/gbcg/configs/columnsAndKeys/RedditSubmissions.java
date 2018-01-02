package edu.gbcg.configs.columnsAndKeys;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RedditSubmissions {
    public static List<String> JSONKeys(){
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

    public static List<String> columnNames(){
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

    public static List<String> dataTypes(){
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
}
