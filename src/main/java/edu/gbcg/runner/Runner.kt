/*
 * Copyright (c) 2018 Sean Grimes. All Rights Reserved.
 * License: MIT
 */

package edu.gbcg.runner

import com.google.common.base.Stopwatch
import edu.gbcg.configs.Finals
import edu.gbcg.configs.columnsAndKeys.RedditComments
import edu.gbcg.configs.columnsAndKeys.RedditSubmissions
import edu.gbcg.dbInteraction.RSMapperOutput
import edu.gbcg.dbInteraction.dbSelector.reddit.comments.RedditComSelector
import edu.gbcg.dbInteraction.dbSelector.reddit.submissions.RedditSubSelector
import edu.gbcg.dbInteraction.dbcreator.reddit.Facilitator
import edu.gbcg.dbInteraction.dbcreator.reddit.comments.CommentsFacilitator
import edu.gbcg.dbInteraction.dbcreator.reddit.submissions.SubmissionsFacilitator
import edu.gbcg.utils.Out
import edu.gbcg.utils.TSL
import java.time.LocalDateTime

fun main(args : Array<String>){
    val logger = TSL.get()
    val out = Out.get()

    Finals.START_FRESH = true
/*
    if(Finals.isWindows() && Finals.START_FRESH){
        logger.err("isWindows() was true while trying to start fresh")
        logger.shutDownAndKill()
    }
*/

    val sw = Stopwatch.createStarted()

    //doSubs()
    doComs()

    sw.stop()

    logger.info("Execution took " + out.timer_millis(sw))
    logger.info("Execution took " + out.timer_secs(sw))
    logger.info("Execution took " + out.timer_mins(sw))

    logger.shutDown()
}

fun doSubs(){
    if(Finals.START_FRESH){
        buildDBShards(SubmissionsFacilitator())
        return
    }
    val rss = RedditSubSelector()

    val results = rss.selectAllFromAuthor("a4k04")
    //val results = rss.selectAllAfterDate(2017, 11, 30, 23, 59, 58)
    val startDate = LocalDateTime.of(2017, 11, 30, 23, 59, 58)
    val endDate = LocalDateTime.of(2017, 12, 1, 0, 0, 0)
    //val results = rss.selectAllBetweenDates(startDate, endDate)

    RSMapperOutput.printAllColumnsFromRSMappers(results, RedditSubmissions.columnsForPrinting())
}

fun doComs(){
    if(Finals.START_FRESH){
        buildDBShards(CommentsFacilitator())
        return
    }
    val rcs = RedditComSelector()

    //val results = rcs.selectAllFromAuthor("a4k04")
    val startDate = LocalDateTime.of(2017, 11, 30, 23, 59, 58)
    val endDate = LocalDateTime.of(2017, 12, 1, 0, 0, 0)
    val results = rcs.selectAllBetweenDates(startDate, endDate)

    RSMapperOutput.printAllColumnsFromRSMappers(results, RedditComments.columnsForPrinting())
}

fun buildDBShards(fac: Facilitator){
    fac.createDBs()
    fac.pushJSONDataIntoDBs()
}
