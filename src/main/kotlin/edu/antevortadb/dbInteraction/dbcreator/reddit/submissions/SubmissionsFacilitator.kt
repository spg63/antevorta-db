/*
 * Copyright (c) 2018 Sean Grimes. All Rights Reserved.
 * License: MIT
 */

package edu.antevortadb.dbInteraction.dbcreator.reddit.submissions

import edu.antevortadb.configs.DBLocator
import edu.antevortadb.configs.Finals
import edu.antevortadb.configs.RawDataLocator
import edu.antevortadb.dbInteraction.columnsAndKeys.RedditSubs
import edu.antevortadb.dbInteraction.dbcreator.JsonFacilitator
import edu.antevortadb.dbInteraction.dbcreator.JsonPusher

@Suppress("ConvertSecondaryConstructorToPrimary")
class SubmissionsFacilitator: JsonFacilitator {
    constructor(): super()

    override fun buildDBPaths()                    = DBLocator.buildSubDBPaths()
    override fun getDataFileAbsolutePaths()        = RawDataLocator.redditJsonSubmissionAbsolutePaths()
    override fun getDBAbsolutePaths()              = DBLocator.redditSubsAbsolutePaths()
    override fun getDBDirectoryPaths()             = DBLocator.getSubDBDirectoryPath()
    override fun getDataKeysOfInterest()           = RedditSubs.jsonKeys()
    override fun getColumnNames()                  = RedditSubs.columnNames()
    override fun getDataTypes()                    = RedditSubs.dataTypes()
    override fun getTableName()                    = Finals.REDDIT_SUB_TABLE
    override fun getDataAbsolutePathsForNewData()  = RawDataLocator.redditJsonSubmissionAbsolutePathsNewData()

    override fun populateJsonWorkers(): List<JsonPusher> {
        val workers = ArrayList<JsonPusher>()
        for(i in 0 until Finals.DB_SHARD_NUM)
            workers.add(SubmissionsJsonPusher())
        return workers
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

    override fun dropIndices() {
        dropDBIndex("attrs_author")
        dropDBIndex("attrs_created")
        dropDBIndex("attrs_host")
        dropDBIndex("attrs_gilded")
        dropDBIndex("attrs_pid")
        dropDBIndex("attrs_comments")
        dropDBIndex("attrs_med_author")
        dropDBIndex("attrs_med_provider")
        dropDBIndex("attrs_media")
        dropDBIndex("attrs_score")
        dropDBIndex("attrs_sub_name")
        dropDBIndex("attrs_sub_id")
    }
}
