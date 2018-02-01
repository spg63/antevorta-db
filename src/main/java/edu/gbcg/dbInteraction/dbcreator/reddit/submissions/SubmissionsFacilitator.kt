/*
 * Copyright (c) 2018 Sean Grimes. All Rights Reserved.
 * License: MIT
 */

package edu.gbcg.dbInteraction.dbcreator.reddit.submissions

import edu.gbcg.configs.DBLocator
import edu.gbcg.configs.Finals
import edu.gbcg.configs.RawDataLocator
import edu.gbcg.configs.columnsAndKeys.RedditSubs
import edu.gbcg.dbInteraction.dbcreator.reddit.Facilitator
import edu.gbcg.dbInteraction.dbcreator.reddit.JsonPusher

class SubmissionsFacilitator: Facilitator {
    constructor(): super()

    override fun buildDBPaths(): List<String> {
        return DBLocator.buildSubDBPaths()
    }

    override fun getJsonAbsolutePaths(): List<String> {
        return RawDataLocator.redditJsonSubmissionAbsolutePaths()
    }

    override fun getDBAbsolutePaths(): List<String> {
        return DBLocator.redditSubsAbsolutePaths()
    }

    override fun getDBDirectoryPaths(): List<String> {
        return DBLocator.getSubDBPath()
    }

    override fun getJsonKeysOfInterest(): List<String> {
        return RedditSubs.JSONKeys()
    }

    override fun getColumnNames(): List<String> {
        return RedditSubs.columnNames()
    }

    override fun getDataTypes(): List<String> {
        return RedditSubs.dataTypes()
    }

    override fun populateJsonWorkers(): List<JsonPusher> {
        var workers = ArrayList<JsonPusher>()
        for(i in 0 until Finals.DB_SHARD_NUM)
            workers.add(SubmissionsJsonPusher())
        return workers
    }

    override fun getTableName(): String {
        return Finals.SUB_TABLE_NAME
    }

    override fun createIndices() {
        createDBIndex(Finals.AUTHOR, "attrs_author")
        createDBIndex(Finals.CREATED_DT, "attrs_created")
        createDBIndex("host_domain", "attrs_host")
        createDBIndex("gilded", "attrs_gilded")
        createDBIndex(Finals.POST_ID, "attrs_pid")
        createDBIndex("num_comments", "attrs_comments")
        createDBIndex("media_author_name", "attrs_med_author")
        createDBIndex("media_provider_name", "attrs_med_provider")
        createDBIndex("media_title", "attrs_media")
        createDBIndex(Finals.SCORE, "attrs_score")
        createDBIndex("subreddit_name", "attrs_sub_name")
        createDBIndex("subreddit_id", "attrs_sub_id")
    }
}
