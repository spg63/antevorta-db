/*
 * Copyright (c) 2018 Sean Grimes. All Rights Reserved.
 * License: MIT
 */

package edu.gbcg.dbInteraction.dbcreator.reddit.comments

import edu.gbcg.configs.DBLocator
import edu.gbcg.configs.Finals
import edu.gbcg.configs.RawDataLocator
import edu.gbcg.configs.columnsAndKeys.RedditComs
import edu.gbcg.dbInteraction.dbcreator.reddit.Facilitator
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
}
