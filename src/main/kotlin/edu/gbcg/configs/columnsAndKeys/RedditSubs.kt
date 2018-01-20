/*
 * Copyright (c) 2018 Sean Grimes. All Rights Reserved.
 * License: MIT
 */

package edu.gbcg.configs.columnsAndKeys

import edu.gbcg.configs.Finals

object RedditSubs {
    @JvmStatic fun JSONKeys(): List<String> {
        return listOf(
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
        )
    }

    @JvmStatic fun columnNames(): List<String> {
        return listOf(
                "ID",                   "archived",             Finals.AUTHOR,
                "brand_safe",           "contest_mode",         Finals.CREATED_DT,
                "distinguished",        "host_domain",          "edited",
                "gilded",               "hidden",               Finals.POST_ID,
                "is_self_post",         "is_video_post",        "link_flair_text",
                "is_locked",            "num_comments",         "media_author_name",
                "media_provider_name",  "media_title",          "media_type",
                "num_crossposts",       "over_18",              Finals.PERMALINK,
                "is_pinned",            Finals.SCRAPED_DT,      Finals.SCORE,
                "selftext",             "is_stickied",          "subreddit_name",
                "subreddit_id",         "subreddit_type",       Finals.TITLE,
                "link_url",             "intg_exp_1",           "intg_exp_2",
                "intg_exp_3",           "real_exp_1",           "real_exp_2",
                "text_exp_1",           "text_exp_2",           "text_exp_3",
                "text_exp_4",           "text_exp_5",           "text_exp_6",
                "text_exp_7",           "text_exp_8",           "text_exp_9"
        )
    }

    @JvmStatic fun columnsForPrinting(): List<String> {
        return listOf(
                "ID",                   "archived",             Finals.AUTHOR,
                "brand_safe",           "contest_mode",         Finals.CREATED_DT,
                "distinguished",        "host_domain",          "edited",
                "gilded",               "hidden",               Finals.POST_ID,
                "is_self_post",         "is_video_post",        "link_flair_text",
                "is_locked",            "num_comments",         "media_author_name",
                "media_provider_name",  "media_title",          "media_type",
                "num_crossposts",       "over_18",              "permalink",
                "is_pinned",            Finals.SCRAPED_DT,      Finals.SCORE,
                "selftext",             "is_stickied",          "subreddit_name",
                "subreddit_id",         "subreddit_type",       Finals.TITLE,
                "link_url"
        )
    }

    @JvmStatic fun dataTypesForPrinting(): List<String> {
        return listOf(
                "INT",  "BOOL", "TEXT",
                "BOOL", "BOOL", "INT",
                "TEXT", "TEXT", "BOOL",
                "INT",  "BOOL", "TEXT",
                "BOOL", "BOOL", "TEXT",
                "BOOL", "INT",  "TEXT",
                "TEXT", "TEXT", "TEXT",
                "INT",  "BOOL", "TEXT",
                "BOOL", "INT",  "INT",
                "TEXT", "BOOL", "TEXT",
                "TEXT", "TEXT", "TEXT",
                "TEXT"
        )
    }

    @JvmStatic fun dataTypes(): List<String> {
        return listOf(
                " INTEGER PRIMARY KEY AUTOINCREMENT,",  " INTEGER DEFAULT 0,",  " TEXT,",
                " INTEGER DEFAULT 0,",                  " INTEGER DEFAULT 0,",  " INTEGER,",
                " TEXT,",                               " TEXT,",               " INTEGER DEFAULT 0,",
                " INTEGER,",                            " INTEGER DEFAULT 0,",  " TEXT,",
                " INTEGER DEFAULT 0,",                  " INTEGER DEFAULT 0,",  " TEXT,",
                " INTEGER DEFAULT 0,",                  " INTEGER,",            " TEXT,",
                " TEXT,",                               " TEXT,",               " TEXT,",
                " INTEGER,",                            " INTEGER DEFAULT 0,",  " TEXT,",
                " INTEGER DEFAULT 0,",                  " INTEGER,",            " INTEGER,",
                " TEXT,",                               " INTEGER DEFAULT 0,",  " TEXT,",
                " TEXT,",                               " TEXT,",               " TEXT,",
                " TEXT,",                               " INTEGER,",            " INTEGER,",
                " INTEGER,",                            " REAL,",               " REAL,",
                " TEXT,",                               " TEXT,",               " TEXT,",
                " TEXT,",                               " TEXT,",               " TEXT,",
                " TEXT,",                               " TEXT,",               " TEXT"
        )
    }
}
