/*
 * Copyright (c) 2018 Sean Grimes. All Rights Reserved.
 * License: MIT
 */

package edu.gbcg.dbInteraction.dbcreator.reddit.comments

import edu.gbcg.configs.DBLocator
import edu.gbcg.configs.Finals
import edu.gbcg.configs.RawDataLocator
import edu.gbcg.configs.columnsAndKeys.RedditComs
import edu.gbcg.dbInteraction.dbcreator.Facilitator
import edu.gbcg.dbInteraction.dbcreator.reddit.JsonPusher

class CommentsFacilitator: Facilitator {
    constructor(): super()

    override fun buildDBPaths()             = DBLocator.buildComDBPaths()
    override fun getJsonAbsolutePaths()     = RawDataLocator.redditJsonCommentAbsolutePaths()
    override fun getDBAbsolutePaths()       = DBLocator.redditComsAbsolutePaths()
    override fun getDBDirectoryPaths()      = DBLocator.getComDBPath()
    override fun getJsonKeysOfInterest()    = RedditComs.JSONKeys()
    override fun getColumnNames()           = RedditComs.columnNames()
    override fun getDataTypes()             = RedditComs.dataTypes()
    override fun getTableName()             = Finals.COM_TABLE_NAME

    override fun populateJsonWorkers(): List<JsonPusher> {
        val workers = ArrayList<JsonPusher>()
        for(i in 0 until Finals.DB_SHARD_NUM)
            workers.add(CommentsJsonPusher())
        return workers
    }

    override fun createIndices() {
        createDBIndex(Finals.AUTHOR, "attrs_author")
        createDBIndex("controversial_score", "attrs_cont_score")
        createDBIndex(Finals.CREATED_DT, "attrs_created")
        createDBIndex("gilded", "attrs_gilded")
        createDBIndex(Finals.POST_ID, "attrs_pid")
        createDBIndex("link_id", "attrs_linkid")
        createDBIndex("parent_id", "attrs_parentid")
        createDBIndex(Finals.SCORE, "attrs_score")
        createDBIndex("subreddit_name", "attrs_sub_name")
        createDBIndex("subreddit_id", "attrs_sub_id")
    }

    // The default values above for raw data location need to be reset to only account for the new data that's
    // getting added to the system.
    // NOTE: This means replacing "this.jsonAbsolutePaths_" list with a list of new json files (or just a single file)
    override fun addNewData(){
        // Clear the existing list. Note: clear can't be called on a "List" so jst replace it with a new one
        // TODO: Is this step even necessary?
        this.jsonAbsolutePaths_ = ArrayList()

        // Get the path(s) to the new json file(s)
        this.jsonAbsolutePaths_ = RawDataLocator.redditJsonCommentAbsolutePathsNewData()

        // Now that the paths have been reset the new data can be pushed into the DB shards
        this.pushJSONDataIntoDBs()
    }
}


















