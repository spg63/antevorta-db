/*
 * Copyright (c) 2018 Sean Grimes. All Rights Reserved.
 * License: MIT
 */

@file:Suppress("unused")

package edu.gbcg.runner

import com.google.common.base.Stopwatch
import edu.gbcg.client.AntevortaClient
import edu.gbcg.configs.Finals
import edu.gbcg.configs.columnsAndKeys.RedditComs
import edu.gbcg.configs.columnsAndKeys.RedditSubs
import edu.gbcg.dbInteraction.dbSelector.RSMapperOutput
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
import org.json.JSONObject

val logger_ = TSL.get()

fun main(args : Array<String>){
    val out = Out.get()

    for(str in System.getProperties())
        println("property: $str")
    println(Runtime.getRuntime().availableProcessors())

    System.exit(0)

    if(Finals.isResearchMachine() && Finals.START_FRESH)
        logger_.logAndKill("isResearchMachine() was true while trying to start fresh")

    if(Finals.isResearchMachine() && Finals.ADD_NEW_DATA)
        logger_.logAndKill("isResearchMachine() was true while trying to add new data")

    val sw = Stopwatch.createStarted()

    doServerComs()
    //doSubs()
    //doServerSubs()
    //doComs()
    //pushNewSubs()
    //pushNewComs()

    sw.stop()

    logger_.info("Execution took " + out.timer_millis(sw))
    logger_.info("Execution took " + out.timer_secs(sw))
    logger_.info("Execution took " + out.timer_mins(sw))

    logger_.shutDown()
}

fun doServerComs(){
    val client = AntevortaClient(Finals.CLIENT_CONFIG)
    val author = "a4k04"

    val dbsql = DBSelector()
            .from(Finals.REDDIT_COM_TABLE)
            .where("author = '$author'")
            .orderBy(Finals.CREATED_DT, true)
            .orderBy("subreddit_name")
            .limit(10)

    logger_.info(dbsql.sql())

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

fun doServerSubs(){
    val client = AntevortaClient(Finals.CLIENT_CONFIG)
    val author = "SciTroll"

    val dbsql = DBSelector()
            .from(Finals.REDDIT_SUB_TABLE)
            .where("author = '$author'")
            .orderBy("created_dt")

    val jsonResults = client.queryServer(dbsql.sql()) ?: return

    val objects = ArrayList<JSONObject>()
    for(i in 0 until jsonResults.length())
        objects.add(jsonResults.getJSONObject(i))

    val mappers = ArrayList<RSMapper>()
    for(jsonobj in objects)
        mappers.add(BaseMapper(jsonobj))

    RSMapperOutput.printAllColumnsFromRSMappers(mappers, RedditSubs.columnsForPrinting(), RedditSubs.dataTypesForPrinting())
}

fun doSubs(){
    if(Finals.START_FRESH){
        buildDBShards(SubmissionsFacilitator())
        return
    }
    val rss = RedditSubSelector()

    //val res = rss.selectAllFromAuthor("a4k04")

    //val comGetter = RedditComOrganizer(res[1])

    //val coms = comGetter.getAllCommentsFromSubmission()
    //RSMapperOutput.printAllColumnsFromRSMappers(coms, RedditComs.columnsForPrinting(), RedditComs
    //        .dataTypesForPrinting())

    //return
    //val res = rss.selectAllAfterDate(2018, 2, 28, 23, 59, 58)
    val res = rss.selectAllFromAuthor("SciTroll")
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
    val author = "a4k04"
    val rcs = RedditComSelector()

    //val res = rcs.selectAllFromAuthor("a4k04")
    //val res = rcs.selectAllAfterDate(2018, 2, 28, 23, 59, 50)
    //val startDate = LocalDateTime.of(2017, 11, 30, 23, 59, 58)
    //val endDate = LocalDateTime.of(2017, 12, 1, 0, 0, 0)
    //val results = rcs.selectAllBetweenDates(startDate, endDate)
    //val results = rcs.selectAllWhereColumnEqualsAndColumnAboveValue("author", "a4k04", "score", "10")

    val dbsql = DBSelector()
            .from(Finals.REDDIT_COM_TABLE)
            .where("author = '$author'")
            .orderBy("subreddit_name")
            .orderBy(Finals.CREATED_DT, true)
            .limit(5)

    val res = rcs.generalSelection(dbsql.sql())

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
