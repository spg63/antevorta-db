/*
 * Copyright (c) 2018 Sean Grimes. All Rights Reserved.
 * License: MIT
 */

@file:Suppress("unused")

package edu.antevorta.runner

import com.google.common.base.Stopwatch
import edu.antevorta.client.AntevortaClient
import edu.antevorta.configs.Finals
import edu.antevorta.configs.RawDataLocator
import edu.antevorta.configs.columnsAndKeys.MovielensLink
import edu.antevorta.configs.columnsAndKeys.RedditComs
import edu.antevorta.configs.columnsAndKeys.RedditSubs
import edu.antevorta.dbInteraction.dbSelector.RSMapperOutput
import edu.antevorta.dbInteraction.dbSelector.BaseMapper
import edu.antevorta.dbInteraction.dbSelector.DBSelector
import edu.antevorta.dbInteraction.dbSelector.RSMapper
import edu.antevorta.dbInteraction.dbSelector.hollywood.movies.MLLinksSelector
import edu.antevorta.dbInteraction.dbSelector.reddit.comments.RedditComSelector
import edu.antevorta.dbInteraction.dbSelector.reddit.submissions.RedditSubSelector
import edu.antevorta.dbInteraction.dbcreator.Facilitator
import edu.antevorta.dbInteraction.dbcreator.hollywood.movies.MovielensGenomeTagsFacilitator
import edu.antevorta.dbInteraction.dbcreator.hollywood.movies.MovielensLinkFacilitator
import edu.antevorta.dbInteraction.dbcreator.hollywood.movies.MovielensMoviesFacilitator
import edu.antevorta.dbInteraction.dbcreator.reddit.comments.CommentsFacilitator
import edu.antevorta.dbInteraction.dbcreator.reddit.submissions.SubmissionsFacilitator
import edu.antevorta.utils.Out
import edu.antevorta.utils.TSL
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.json.JSONObject
import java.io.File
import java.io.FileReader

val logger_ = TSL.get()

fun main(args : Array<String>){
    val out = Out.get()

    if(Finals.isResearchMachine() && Finals.START_FRESH)
        logger_.logAndKill("isResearchMachine() was true while trying to start fresh")

    if(Finals.isResearchMachine() && Finals.ADD_NEW_DATA)
        logger_.logAndKill("isResearchMachine() was true while trying to add new data")

    val sw = Stopwatch.createStarted()

    //hollywoodSelect()
    //PullFromServer.doServerComs()
    //PullFromServer.doServerSubs()
    //doSubs()
    createHollywoodDB()
    //doComs()
    //pushNewSubs()
    //pushNewComs()

    sw.stop()

    logger_.info("Execution took " + out.timer_millis(sw))
    logger_.info("Execution took " + out.timer_secs(sw))
    logger_.info("Execution took " + out.timer_mins(sw))

    logger_.shutDown()
}

fun createHollywoodDB(){
    if(!Finals.START_FRESH)
        return

    // Create the DB, and the first table in the DB (links_table)
    buildDBShards(MovielensLinkFacilitator())

    /* ---------- Now start adding tables to the DB shards ---------------------------------------------------------- */

    // 2nd table should be the genome_tags table
    addTableToShards(MovielensGenomeTagsFacilitator())
    addTableToShards(MovielensMoviesFacilitator())

    //TODO("DROP THE TABLES THAT AREN'T NECESSARY!")
}

fun hollywoodSelect(){
    val mlsel = MLLinksSelector()
    val tmdbID = mlsel.getTMDBMovieIDFromMovielensMovieID(1)
    println("tmdbID: $tmdbID")
}

fun doSubs(){
    if(Finals.START_FRESH){
        buildDBShards(SubmissionsFacilitator())
        return
    }
    val rss = RedditSubSelector()
    val res = rss.selectAllFromAuthor("SciTroll")

    //val res = rss.selectAllFromAuthor("a4k04")

    //val comGetter = RedditComOrganizer(res[1])

    //val coms = comGetter.getAllCommentsFromSubmission()
    //RSMapperOutput.printAllColumnsFromRSMappers(coms, RedditComs.columnsForPrinting(), RedditComs
    //        .dataTypesForPrinting())

    //return
    //val res = rss.selectAllAfterDate(2018, 2, 28, 23, 59, 58)
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
    fac.pushDataIntoDBs()
}

fun addTableToShards(fac: Facilitator){
    fac.createNewTableInExistingDBs()
    fac.pushDataIntoDBs()
}

fun pushNewComs(){
    CommentsFacilitator().pushNewData()
}

fun pushNewSubs(){
    SubmissionsFacilitator().pushNewData()
}