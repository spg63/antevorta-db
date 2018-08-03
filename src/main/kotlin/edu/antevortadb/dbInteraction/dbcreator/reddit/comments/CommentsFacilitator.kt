/*
 * Copyright (c) 2018 Sean Grimes. All Rights Reserved.
 * License: MIT
 */

package edu.antevortadb.dbInteraction.dbcreator.reddit.comments

import edu.antevortadb.configs.DBLocator
import edu.antevortadb.configs.Finals
import edu.antevortadb.configs.RawDataLocator
import edu.antevortadb.dbInteraction.columnsAndKeys.RedditComs
import edu.antevortadb.dbInteraction.dbcreator.JsonFacilitator
import edu.antevortadb.dbInteraction.dbcreator.JsonPusher

@Suppress("ConvertSecondaryConstructorToPrimary")
class CommentsFacilitator: JsonFacilitator {
    constructor(): super()

    override fun buildDBPaths()                     = DBLocator.buildComDBPaths()
    override fun getDataFileAbsolutePaths()         = RawDataLocator.redditJsonCommentAbsolutePaths()
    override fun getDBAbsolutePaths()               = DBLocator.redditComsAbsolutePaths()
    override fun getDBDirectoryPaths()              = DBLocator.getComDBDirectoryPath()
    override fun getDataKeysOfInterest()            = RedditComs.jsonKeys()
    override fun getColumnNames()                   = RedditComs.columnNames()
    override fun getDataTypes()                     = RedditComs.dataTypes()
    override fun getTableName()                     = Finals.REDDIT_COM_TABLE
    override fun getDataAbsolutePathsForNewData()   = RawDataLocator.redditJsonCommentAbsolutePathsNewData()

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

    override fun dropIndices() {
        dropDBIndex("attrs_author")
        dropDBIndex("attrs_cont_score")
        dropDBIndex("attrs_created")
        dropDBIndex("attrs_gilded")
        dropDBIndex("attrs_pid")
        dropDBIndex("attrs_linkid")
        dropDBIndex("attrs_parentid")
        dropDBIndex("attrs_score")
        dropDBIndex("attrs_sub_name")
        dropDBIndex("attrs_sub_id")
    }
}
