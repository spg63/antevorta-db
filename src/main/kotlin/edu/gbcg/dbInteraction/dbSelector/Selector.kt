/*
 * Copyright (c) 2018 Sean Grimes. All Rights Reserved.
 * License: MIT
 */

package edu.gbcg.dbInteraction.dbSelector

import edu.gbcg.configs.Finals
import edu.gbcg.dbInteraction.RSMapperComparators
import edu.gbcg.dbInteraction.TimeUtils
import edu.gbcg.dbInteraction.dbSelector.reddit.comments.RedditComSelector
import edu.gbcg.dbInteraction.dbSelector.reddit.submissions.RedditSubSelector
import edu.gbcg.utils.TSL
import java.time.LocalDateTime
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executors
import java.util.concurrent.Future

/**
 * This class implements all shared functionality for DB selections. Based on the type of SelectionWorker passed to
 * the genericSelect function it will query the proper DB files and use the proper RSMapper object
 */
abstract class Selector{
    protected var tableName: String = ""
    protected val logger_: TSL = TSL.get()

//----------------------------------------------------------------------------------------------------------------------
// NOTE: This can be used when one of the functions below doesn't satisfy your querying needs. I suggest just writing
// the function below unless you're absolutely positive the query is a one-off so you don't need to keep writing the
// same SQL string.
//----------------------------------------------------------------------------------------------------------------------
    abstract fun generalSelection(SQLStatement: String): List<RSMapper>

//----------------------------------------------------------------------------------------------------------------------
// The following functions take the tableName variable (set the in the derived class default c'tors) and create an
// sql query string using the DBSelector class. The query string is passed back down to a derived class through
// generalSelection to ascertain which workers to use and which DB shards to search before calling the generalized
// genericSelect function below.
//----------------------------------------------------------------------------------------------------------------------

    fun selectAllFromAuthor(author: String): List<RSMapper> {
        return selectAllWhereColumnEquals(Finals.AUTHOR, author)
    }

    fun selectAllFromAuthorOrderBy(author: String, orderBy: String): List<RSMapper> {
        val sel = DBSelector()
                .from(this.tableName)
                .where("${Finals.AUTHOR} = '$author'")
                .orderBy(orderBy)
        return generalSelection(sel.sql())
    }

    fun selectAllWhereColumnEquals(columnName: String, equalsValue: String): List<RSMapper> {
        val sel = DBSelector()
                .from(this.tableName)
                .where("$columnName = '$equalsValue'")
        return generalSelection(sel.sql())
    }

    fun selectAllWhereColumnLessThan(columnName: String, lessThanValue: String): List<RSMapper> {
        val sel = DBSelector()
                .from(this.tableName)
                .where("$columnName < '$lessThanValue'")
        return generalSelection(sel.sql())
    }

    fun selectAllWhereColumnGreaterThan(columnName: String, greaterThanValue: String): List<RSMapper> {
        val sel = DBSelector()
                .from(this.tableName)
                .where("$columnName > '$greaterThanValue'")
        return generalSelection(sel.sql())
    }

    fun selectAllWhereColumnEqualsAndColumnAboveValue(column: String, equalsVal: String,
                                                      filter: String, value: String): List<RSMapper> {
        val sel = DBSelector()
                .from(this.tableName)
                .where("$column = '$equalsVal'")
                .and("$filter > '$value'")
        return generalSelection(sel.sql())
    }

    fun selectAllAfterDate(year: Int, month: Int, day: Int, hour: Int, minute: Int, second: Int): List<RSMapper> {
        val utc = TimeUtils.utcSecondsFromValues_SEL(year, month, day, hour, minute, second)
        return selectAllWhereColumnGreaterThan(Finals.CREATED_DT, utc.toString())
    }

    fun selectAllAfterDate(dt: LocalDateTime): List<RSMapper> {
        return selectAllWhereColumnGreaterThan(Finals.CREATED_DT, TimeUtils.utcSecondsFromLDT_SEL(dt).toString())
    }

    fun selectAllBeforeDate(year: Int, month: Int, day: Int, hour: Int, minute: Int, second: Int): List<RSMapper> {
        val utc = TimeUtils.utcSecondsFromValues_SEL(year, month, day, hour, minute, second)
        return selectAllWhereColumnLessThan(Finals.CREATED_DT, utc.toString())
    }

    fun selectAllBeforeDate(dt: LocalDateTime): List<RSMapper> {
        return selectAllWhereColumnLessThan(Finals.CREATED_DT, TimeUtils.utcSecondsFromLDT_SEL(dt).toString())
    }

    fun selectAllBetweenDates(startYear: Int, startMonth: Int, startDay: Int,
                              startHour: Int, startMinute: Int, startSecond: Int,
                              endYear: Int, endMonth: Int, endDay: Int,
                              endHour: Int, endMinute: Int, endSecond: Int): List<RSMapper> {
        val start = LocalDateTime.of(startYear, startMonth, startDay, startHour, startMinute, startSecond)
        val end = LocalDateTime.of(endYear, endMonth, endDay, endHour, endMinute, endSecond)
        return selectAllBetweenDates(start, end)
    }

    fun selectAllBetweenDates(start: LocalDateTime, end: LocalDateTime): List<RSMapper> {
        val startDate = TimeUtils.utcSecondsFromLDT_SEL(start)
        val endDate = TimeUtils.utcSecondsFromLDT_SEL(end)
        val sel = DBSelector()
                .from(this.tableName)
                .where("${Finals.CREATED_DT} > '$startDate'")
                .and("${Finals.CREATED_DT} < '$endDate'")
        return generalSelection(sel.sql())
    }


//----------------------------------------------------------------------------------------------------------------------
// The functions below implement the queries on the DB. verifyDBsExist simply checks that the requests DB shards are
// in place on the disk where they're expected to be. If this fails the program dies...something is very wrong if
// this failed. genericSelect is a generalized DB query function which gives a list of selection workers will search
// the proper DB shards.
//----------------------------------------------------------------------------------------------------------------------

    /*
        Perform a multi-threaded selection against the DB shards. Each shard is given a single thread
     */
    @Suppress("UNCHECKED_CAST")
    protected fun genericSelect(workers: List<SelectionWorker>, SQLStatement: String): List<RSMapper> {
        val futureResults: ArrayList<Future<ArrayList<RSMapper>>> = ArrayList()
        val executor = Executors.newFixedThreadPool(Finals.DB_SHARD_NUM)

        // For each worker in workers, submit to executor and put result in futureResults
        workers.mapTo(futureResults) { executor.submit(it) as Future<ArrayList<RSMapper>> }

        // Get all results from the worker threads and place the RSMappers in the results ArrayList
        val results = ArrayList<RSMapper>()
        try{
            for(i in 0 until Finals.DB_SHARD_NUM) {
                results.addAll(futureResults[i].get())
            }
        }
        catch(e: InterruptedException){
            logger_.exception(e)
        }
        catch(e: ExecutionException){
            logger_.exception(e)
        }
        executor.shutdown()

        logger_.info("$SQLStatement --- ${results.size} results.")

        // The research machine has multiple DB shards which makes "orderby" requests almost useless. The code below
        // will search for orderby in the query, if it exists it will determine which column name the ordering is to
        // be done on and if the ordering is ascending or decending (default assumes ascending).
        // NOTE: This only handles a single orderby clause right now!!!
        return handleOrderBy(SQLStatement, results)
    }

    /*
        Takes in the query and the results, checks the query to see if it contains an orderby clause, if it does it will
        process the orderby, determine how to sort the results based on the columns the ordering should be done on
        and if the ordering should be ascending (default) or descending, then it will return the properly sorted
        results. If there is no orderby clause the results will be returned unmodified.
     */
    private fun handleOrderBy(query: String, results: List<RSMapper>): List<RSMapper>{
        // If the query doesn't contain "orderby" and it doesn't contain "order by", or the query returned no results
        // return the results without further processing
        if(results.isEmpty()) return results
        if(!query.toLowerCase().contains("orderby") && !query.toLowerCase().contains("order by")) return results

        // Determine which columns names should be sorted, and if it should be sorted ascending or descending
        val columnsAndOrders = determineOrderByColumns(query)

        return doTheSort(results, columnsAndOrders)
    }

    /*
        Determine which column should be used in the sorting when an orderby clause is added to the SQL query
        It takes in the query string and returns a map of column names and a boolean. The boolean is true when the
        sorting should be done in ascending order (the default) and false when it should be done in decending order.
     */
    //TODO: This is bullshited for created_dt
    private fun determineOrderByColumns(query: String): OrderBySelection {
        return OrderBySelection("created_dt", true)
    }

    /*
        The columnName is the column that the sort needs to be based on
        If isAscending is true the data should be sorted in ascending order, if it's false it should be sorted in
        descending order
     */
    private fun doTheSort(results: List<RSMapper>, columnsWithOrder: OrderBySelection): List<RSMapper> {
        val mutableResults = results.toMutableList()

        // Set the OrderBySelection before doing the sort. Unfortunately this can't be done in the Comparator
        // function call because the comparator implements a specific interface. Oh well.
        RSMapperComparators.columnsWithOrder = columnsWithOrder

        mutableResults.sortWith(RSMapperComparators)

        // Reset the columnsWithOrder to the default value for future use
        RSMapperComparators.columnsWithOrder = OrderBySelection()

        return mutableResults
    }




    /*
        Kill the program if the DBs don't exist or a shard is missing
     */
    protected fun verifyDBsExist(DBs: List<String>) {
        if(DBs.isEmpty())
            logger_.logAndKill("Selector.verifyDBsExist DBs was empty")
        if(DBs.size != Finals.DB_SHARD_NUM)
            logger_.logAndKill("Selector.verifyDBsExist DBs.size != Finals.DB_SHARD_NUM")
    }

    /*
        Get selector type based on string matching...stupid idea, will revisit another time
        NOTE: The companion object is kotlin specific
     */
    companion object {
        fun getSelectorOnType(matchingString: String): Selector{
            return when {
                matchingString.toLowerCase().contains(Finals.REDDIT_COM_TABLE) -> RedditComSelector()
                matchingString.toLowerCase().contains(Finals.REDDIT_SUB_TABLE) -> RedditSubSelector()
                else -> throw IllegalArgumentException("Selector.getSelectorOnType is a stupid idea Sean")
            }
        }
    }

}
