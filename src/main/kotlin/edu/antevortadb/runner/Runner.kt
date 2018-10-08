/*
 * Copyright (c) 2018 Sean Grimes. All Rights Reserved.
 * License: MIT
 */

@file:Suppress("unused", "ConstantConditionIf", "HasPlatformType")

package edu.antevortadb.runner

import com.google.common.base.Stopwatch
import edu.antevortadb.configs.Finals
import edu.antevortadb.dbInteraction.columnsAndKeys.RedditComs
import edu.antevortadb.dbInteraction.columnsAndKeys.RedditSubs
import edu.antevortadb.dbInteraction.dbSelector.RSMapperOutput
import edu.antevortadb.dbInteraction.dbSelector.DBSelector
import edu.antevortadb.dbInteraction.dbSelector.hollywood.movies.MLGenomeTagsSelector
import edu.antevortadb.dbInteraction.dbSelector.reddit.comments.RedditComSelector
import edu.antevortadb.dbInteraction.dbSelector.reddit.submissions.RedditSubSelector
import edu.antevortadb.dbInteraction.dbcreator.Facilitator
import edu.antevortadb.dbInteraction.dbcreator.hollywood.movies.*
import edu.antevortadb.dbInteraction.dbcreator.reddit.comments.CommentsFacilitator
import edu.antevortadb.dbInteraction.dbcreator.reddit.submissions.SubmissionsFacilitator
import edu.antevortadb.utils.Out
import edu.antevortadb.utils.TSL
import java.io.File
import java.text.DecimalFormat

val logger = TSL.get()

fun main(args : Array<String>){

    val out = Out.get()
    TSL.LOG_TO_CONSOLE = true

    if(Finals.isResearchMachine() && Finals.START_FRESH)
        logger.logAndKill("isResearchMachine() was true while trying to start fresh")

    if(Finals.isResearchMachine() && Finals.ADD_NEW_DATA)
        logger.logAndKill("isResearchMachine() was true while trying to add new data")

    val sw = Stopwatch.createStarted()

    //PullFromServer.doServerComs()
    //PullFromServer.doServerSubs()
    //doSubs()
    createHollywoodDB()
    //doComs()
    //pushNewSubs()
    //pushNewComs()
    //hollywoodSelect()

    sw.stop()

    logger.info("Execution took " + out.timer_millis(sw))
    logger.info("Execution took " + out.timer_secs(sw))
    logger.info("Execution took " + out.timer_mins(sw))

    logger.shutDown()
}

fun hollywoodSelect(){
    val text = "boat"
    val res = MLGenomeTagsSelector().getTagIDFromTagText(text)
    logger.info("$text: $res")

}

fun createHollywoodDB(){
    if(!Finals.START_FRESH)
        return

    // Skip the info logs when creating the hollywood DBs, there are a lot of select statements

    // Create the DB, and the first table in the DB (links_table)
    buildDBShards(MovielensLinkFacilitator())                   // Okay

    /* ---------- Now start adding tables to the DB shards ------------------------------------------------ */

    // 2nd table should be the genome_tags table
    addTableToShards(MovielensGenomeTagsFacilitator())          // Okay
    addTableToShards(MovielensMoviesFacilitator())              // Okay
    addTableToShards(TMDBCreditsFacilitator())                  // Okay
    addTableToShards(MovielensGenomeScoresFacilitator())        // Okay
    addTableToShards(MovielensIndividualTagsFacilitator())      // Okay
    addTableToShards(MovielensIndividualRatingsFacilitator())   // Not checked
    addTableToShards(TMDBMoviesFacilitator())                   // Not checked
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


    RSMapperOutput.printAllColumnsFromRSMappers(res, RedditSubs.columnsForPrinting(),
            RedditSubs.dataTypesForPrinting())
    //RSMapperOutput.rsMappersToCSV(results, RedditSubs.columnsForPrinting(), "out.csv")
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

    RSMapperOutput.printAllColumnsFromRSMappers(res, RedditComs.columnsForPrinting(),
            RedditComs.dataTypesForPrinting())
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
