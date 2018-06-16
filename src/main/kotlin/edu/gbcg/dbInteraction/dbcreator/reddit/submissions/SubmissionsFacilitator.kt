/*
 * Copyright (c) 2018 Sean Grimes. All Rights Reserved.
 * License: MIT
 */

package edu.gbcg.dbInteraction.dbcreator.reddit.submissions

import edu.gbcg.configs.DBLocator
import edu.gbcg.configs.Finals
import edu.gbcg.configs.RawDataLocator
import edu.gbcg.configs.columnsAndKeys.RedditSubs
import edu.gbcg.dbInteraction.dbcreator.JsonFacilitator
import edu.gbcg.dbInteraction.dbcreator.JsonPusher

@Suppress("ConvertSecondaryConstructorToPrimary")
class SubmissionsFacilitator: JsonFacilitator {
    constructor(): super()

    override fun buildDBPaths()                     = DBLocator.buildSubDBPaths()
    override fun getDataFileAbsolutePaths()         = RawDataLocator.redditJsonSubmissionAbsolutePaths()
    override fun getDBAbsolutePaths()               = DBLocator.redditSubsAbsolutePaths()
    override fun getDBDirectoryPaths()              = DBLocator.getSubDBDirectoryPath()
    override fun getJsonKeysOfInterest()            = RedditSubs.JSONKeys()
    override fun getColumnNames()                   = RedditSubs.columnNames()
    override fun getDataTypes()                     = RedditSubs.dataTypes()
    override fun getTableName()                     = Finals.REDDIT_SUB_TABLE
    override fun getDataAbsolutePathsForNewData()   = RawDataLocator.redditJsonSubmissionAbsolutePathsNewData()

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
        dropDBIndices("attrs_author")
        dropDBIndices("attrs_created")
        dropDBIndices("attrs_host")
        dropDBIndices("attrs_gilded")
        dropDBIndices("attrs_pid")
        dropDBIndices("attrs_comments")
        dropDBIndices("attrs_med_author")
        dropDBIndices("attrs_med_provider")
        dropDBIndices("attrs_media")
        dropDBIndices("attrs_score")
        dropDBIndices("attrs_sub_name")
        dropDBIndices("attrs_sub_id")
    }
}
