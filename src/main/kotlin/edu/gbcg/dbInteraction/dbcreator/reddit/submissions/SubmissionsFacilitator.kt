/*
 * Copyright (c) 2018 Sean Grimes. All Rights Reserved.
 * License: MIT
 */

package edu.gbcg.dbInteraction.dbcreator.reddit.submissions

import edu.gbcg.configs.DBLocator
import edu.gbcg.configs.Finals
import edu.gbcg.configs.RawDataLocator
import edu.gbcg.configs.columnsAndKeys.RedditSubs
import edu.gbcg.dbInteraction.dbcreator.Facilitator
import edu.gbcg.dbInteraction.dbcreator.reddit.JsonPusher

class SubmissionsFacilitator: Facilitator {
    constructor(): super()

    override fun buildDBPaths()             = DBLocator.buildSubDBPaths()
    override fun getJsonAbsolutePaths()     = RawDataLocator.redditJsonSubmissionAbsolutePaths()
    override fun getDBAbsolutePaths()       = DBLocator.redditSubsAbsolutePaths()
    override fun getDBDirectoryPaths()      = DBLocator.getSubDBPath()
    override fun getJsonKeysOfInterest()    = RedditSubs.JSONKeys()
    override fun getColumnNames()           = RedditSubs.columnNames()
    override fun getDataTypes()             = RedditSubs.dataTypes()
    override fun getTableName()             = Finals.SUB_TABLE_NAME

    override fun populateJsonWorkers(): List<JsonPusher> {
        var workers = ArrayList<JsonPusher>()
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

    // The default values above for raw data location need to be reset to only account for the new data that's
    // getting added to the system.
    override fun addNewData() {
        // Clear the existing list. NOTE: clear can't be called on a "List" so just replace it with a new one
        // TODO: Is this step even necessary?
        this.jsonAbsolutePaths_ = ArrayList()

        // Get the path(s) to the new json file(s)
        this.jsonAbsolutePaths_ = RawDataLocator.redditJsonSubmissionAbsolutePathsNewData()

        // Now that the paths have been reset the new data can be pushed into the DB shards
        this.pushJSONDataIntoDBs()
    }
}
