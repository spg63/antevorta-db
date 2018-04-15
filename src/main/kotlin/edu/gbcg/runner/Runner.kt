/*
 * Copyright (c) 2018 Sean Grimes. All Rights Reserved.
 * License: MIT
 */

@file:Suppress("unused")

package edu.gbcg.runner

import com.google.common.base.Stopwatch
import edu.gbcg.configs.Finals
import edu.gbcg.configs.columnsAndKeys.RedditComs
import edu.gbcg.configs.columnsAndKeys.RedditSubs
import edu.gbcg.dbInteraction.RSMapperOutput
import edu.gbcg.dbInteraction.dbSelector.BaseMapper
import edu.gbcg.dbInteraction.dbSelector.DBSelector
import edu.gbcg.dbInteraction.dbSelector.RSMapper
import edu.gbcg.dbInteraction.dbSelector.reddit.comments.RedditComSelector
import edu.gbcg.dbInteraction.dbSelector.reddit.submissions.RedditSubSelector
import edu.gbcg.dbInteraction.dbcreator.Facilitator
import edu.gbcg.dbInteraction.dbcreator.reddit.comments.CommentsFacilitator
import edu.gbcg.dbInteraction.dbcreator.reddit.submissions.SubmissionsFacilitator
import edu.gbcg.utils.Out
import edu.gbcg.utils.TSL
import edu.gbcg.utils.client.AntevortaClient
import org.json.JSONObject

fun main(args : Array<String>){
    val logger = TSL.get()
    val out = Out.get()

    if(Finals.isWindows() && Finals.START_FRESH)
        logger.logAndKill("isWindows() was true while trying to start fresh")

    val sw = Stopwatch.createStarted()

    //doServerComs()
    //doSubs()
    //doComs()
    pushNewSubs()
    //pushNewComs()

    sw.stop()

    logger.info("Execution took " + out.timer_millis(sw))
    logger.info("Execution took " + out.timer_secs(sw))
    logger.info("Execution took " + out.timer_mins(sw))

    logger.shutDown()
}

fun doServerComs(){
    val client = AntevortaClient(Finals.CLIENT_CONFIG)
    val author = "a4k04"

    val dbsql = DBSelector()
            .from(Finals.COM_TABLE_NAME)
            .where("author = '$author'")

    // If results are null, return
    val jsonResults = client.queryServer(dbsql.sql()) ?: return

    // Get all objects from the JSONArray
    val objects = ArrayList<JSONObject>()
    for(i in 0 until jsonResults.length())
        objects.add(jsonResults.getJSONObject(i))

    val mappers = ArrayList<RSMapper>()
    for(jsonobj in objects)
        mappers.add(BaseMapper(jsonobj))

    RSMapperOutput.printAllColumnsFromRSMappers(mappers, RedditComs.columnsForPrinting(), RedditComs.dataTypesForPrinting())

}

fun doSubs(){
    if(Finals.START_FRESH){
        buildDBShards(SubmissionsFacilitator())
        return
    }
    val rss = RedditSubSelector()

    val res = rss.selectAllFromAuthor("a4k04")

    //val comGetter = RedditComOrganizer(res[1])

    //val coms = comGetter.getAllCommentsFromSubmission()
    //RSMapperOutput.printAllColumnsFromRSMappers(coms, RedditComs.columnsForPrinting(), RedditComs
    //        .dataTypesForPrinting())

    //return
    //val results = rss.selectAllAfterDate(2017, 11, 30, 23, 59, 58)
    //val startDate = LocalDateTime.of(2017, 11, 30, 23, 59, 58)
    //val endDate = LocalDateTime.of(2017, 12, 1, 0, 0, 0)
    //val results = rss.selectAllBetweenDates(startDate, endDate)
    //val results = rss.selectAllWhereColumnEquals("subreddit_name", "4chan")
    //val results = rss.selectAllWhereColumnEqualsAndColumnAboveValue("author", "a4k04", "score", "10")


    RSMapperOutput.printAllColumnsFromRSMappers(res, RedditSubs.columnsForPrinting(), RedditSubs.dataTypesForPrinting())
    //RSMapperOutput.RSMappersToCSV(results, RedditSubs.columnsForPrinting(), "out.csv")
}

fun doComs(){
    if(Finals.START_FRESH){
        buildDBShards(CommentsFacilitator())
        return
    }
    val rcs = RedditComSelector()

    val res = rcs.selectAllFromAuthor("a4k04")
    //val startDate = LocalDateTime.of(2017, 11, 30, 23, 59, 58)
    //val endDate = LocalDateTime.of(2017, 12, 1, 0, 0, 0)
    //val results = rcs.selectAllBetweenDates(startDate, endDate)
    //val results = rcs.selectAllWhereColumnEqualsAndColumnAboveValue("author", "a4k04", "score", "10")

    RSMapperOutput.printAllColumnsFromRSMappers(res, RedditComs.columnsForPrinting(), RedditComs.dataTypesForPrinting())
}

fun buildDBShards(fac: Facilitator){
    fac.createDBs()
    fac.pushJSONDataIntoDBs()
}

fun pushNewComs(){
    CommentsFacilitator().pushNewData()
}

fun pushNewSubs(){
    SubmissionsFacilitator().pushNewData()
}
