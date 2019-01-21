/*
 * Copyright (c) 2018 Sean Grimes. All Rights Reserved.
 * License: MIT
 */

@file:Suppress("unused", "ConstantConditionIf", "HasPlatformType")

package edu.antevortadb.runner

import com.google.common.base.Stopwatch
import edu.antevortadb.configs.Finals
import edu.antevortadb.configs.RawDataLocator
import edu.antevortadb.dbInteraction.columnsAndKeys.RedditComs
import edu.antevortadb.dbInteraction.columnsAndKeys.RedditSubs
import edu.antevortadb.dbInteraction.columnsAndKeys.TMDBMovies
import edu.antevortadb.dbInteraction.dbSelector.RSMapperOutput
import edu.antevortadb.dbInteraction.dbSelector.DBSelector
import edu.antevortadb.dbInteraction.dbSelector.hollywood.MLGenomeTagsSelector
import edu.antevortadb.dbInteraction.dbSelector.hollywood.MovieSelector
import edu.antevortadb.dbInteraction.dbSelector.reddit.comments.RedditComSelector
import edu.antevortadb.dbInteraction.dbSelector.reddit.submissions.RedditSubSelector
import edu.antevortadb.dbInteraction.dbcreator.Facilitator
import edu.antevortadb.dbInteraction.dbcreator.hollywood.TMDBMoviesFacilitator
import edu.antevortadb.dbInteraction.dbcreator.hollywood.*
import edu.antevortadb.dbInteraction.dbcreator.reddit.comments.CommentsFacilitator
import edu.antevortadb.dbInteraction.dbcreator.reddit.submissions.SubmissionsFacilitator
//import edu.antevortadb.utils.Out
//import edu.antevortadb.utils.TSL
import java.io.File
import java.text.DecimalFormat
import javalibs.DBUtils
import javalibs.Out
import javalibs.TSL

val logger = TSL.get()

fun main(args : Array<String>){

    val out = Out.get()
    TSL.LOG_TO_CONSOLE = true
    println("hello")
    System.exit(0)

    if(Finals.isResearchMachine() && Finals.START_FRESH)
        logger.logAndKill("isResearchMachine() was true while trying to start fresh")

    if(Finals.isResearchMachine() && Finals.ADD_NEW_DATA)
        logger.logAndKill("isResearchMachine() was true while trying to add new data")

    val sw = Stopwatch.createStarted()

    PullFromServer.doServerComs()
    //PullFromServer.doServerSubs()
    //doSubs()
    //createHollywoodDB()
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
    val randomShuffleSeed = 55L
    val dataDir = "${RawDataLocator.dl4jDataRoot()}hollywoodTesting/threeClasses/"
    val selectStatement = "select * from movies where budget > 9 and " +
            "runtime > 0 and ID > 0"
    val results = MovieSelector().generalSelection(selectStatement)
    RSMapperOutput.rsMappersToCSV(results, TMDBMovies.columnNames(),
            "${dataDir}allData.csv", randomShuffleSeed)
    //RSMapperOutput.printAllColumnsFromRSMappers(results,
    //        TMDBMovies.columnsForPrinting(), TMDBMovies.dataTypesForPrinting())

}

fun createHollywoodDB(){
    if(!Finals.START_FRESH)
        return

    // Skip the info logs when creating the hollywood DBs, there are a lot of
    // select statements

    // Create the DB, and the first table in the DB (links_table)
    buildDBShards(MovielensLinkFacilitator())

    /* ---------- Now start adding tables to the DB shards ---------------------------- */

    // 2nd table should be the genome_tags table
    addTableToShards(MovielensGenomeTagsFacilitator())
    addTableToShards(MovielensMoviesFacilitator())
    addTableToShards(TMDBCreditsFacilitator())
    addTableToShards(MovielensGenomeScoresFacilitator())
    addTableToShards(MovielensIndividualTagsFacilitator())
    addTableToShards(MovielensIndividualRatingsFacilitator())
    addTableToShards(TMDBMoviesFacilitator())
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
    //RSMapperOutput.printAllColumnsFromRSMappers(coms, RedditComs.columnsForPrinting(),
    //        RedditComs.dataTypesForPrinting())

    //return
    //val res = rss.selectAllAfterDate(2018, 2, 28, 23, 59, 58)
    //val startDate = LocalDateTime.of(2017, 11, 30, 23, 59, 58)
    //val endDate = LocalDateTime.of(2017, 12, 1, 0, 0, 0)
    //val results = rss.selectAllBetweenDates(startDate, endDate)
    //val results = rss.selectAllWhereColumnEquals("subreddit_name", "4chan")


    RSMapperOutput.printAllColumnsFromRSMappers(res, RedditSubs.columnsForPrinting(),
            RedditSubs.dataTypesForPrinting())
    //RSMapperOutput.rsMappersToCSV(results, RedditSubs.columnsForPrinting(), "out.csv")
}

fun doComs(){
    if(Finals.START_FRESH){
        buildDBShards(CommentsFacilitator())
        return
    }
    val author = "mariowned"
    val rcs = RedditComSelector()

    //val res = rcs.selectAllFromAuthor("a4k04")
    //val res = rcs.selectAllAfterDate(2018, 2, 28, 23, 59, 50)
    //val startDate = LocalDateTime.of(2017, 11, 30, 23, 59, 58)
    //val endDate = LocalDateTime.of(2017, 12, 1, 0, 0, 0)
    //val results = rcs.selectAllBetweenDates(startDate, endDate)
    //val results = rcs.selectAllWhereColumnEqualsAndColumnAboveValue("author", "a4k04",
    // "score", "10")

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
