/*
 * Copyright (c) 2018 Sean Grimes. All Rights Reserved.
 * License: MIT
 */

package edu.antevortadb.dbInteraction.dbSelector

import edu.antevortadb.configs.Finals
import edu.antevortadb.dbInteraction.TimeUtils
import edu.antevortadb.dbInteraction.dbSelector.reddit.comments.RedditComSelector
import edu.antevortadb.dbInteraction.dbSelector.reddit.submissions.RedditSubSelector
import edu.antevortadb.utils.TSL
import java.time.LocalDateTime
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executors
import java.util.concurrent.Future

/**
 * This class implements all shared functionality for DB selections. Based on the type of SelectionWorker
 * passed to the genericSelect function it will query the proper DB files and use the proper RSMapper object
 */
@Suppress("unused", "MemberVisibilityCanBePrivate", "HasPlatformType")
abstract class Selector{
    // dbTableName and listOfColumns get set in the derived class c'tors. dbTableName is used to select the
    // right table from the DB shards (e.g. submission_attrs) for reddit submissions. listOfColumns is used by
    // the orderby function for determining which words in a query refer to a valid column.
    protected lateinit var tableName: String
    protected lateinit var listOfColumns: List<String>
    protected val logger = TSL.get()
    private var hasBeenSorted = false

//------------------------------------------------------------------------------------------------------------
// NOTE: This can be used when one of the functions below doesn't satisfy your querying needs. I suggest
// just writing the function below unless you're absolutely positive the query is a one-off so you don't need
// to keep writing the same SQL string.
//------------------------------------------------------------------------------------------------------------
    abstract fun generalSelection(SQLStatement: String): List<RSMapper>

//------------------------------------------------------------------------------------------------------------
// The following functions take the dbTableName variable (set the in the derived class default c'tors) and
// create an sql query string using the DBSelector class. The query string is passed back down to a derived
// class through generalSelection to ascertain which workers to use and which DB shards to search before
// calling the generalized genericSelect function below.
//------------------------------------------------------------------------------------------------------------

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

    fun selectAllAfterDate(year: Int, month: Int, day: Int, hour: Int,
                           minute: Int, second: Int): List<RSMapper> {
        val utc = TimeUtils.utcSecondsFromValues_SEL(year, month, day, hour, minute, second)
        return selectAllWhereColumnGreaterThan(Finals.CREATED_DT, utc.toString())
    }

    fun selectAllAfterDate(dt: LocalDateTime): List<RSMapper> {
        return selectAllWhereColumnGreaterThan(Finals.CREATED_DT,
                TimeUtils.utcSecondsFromLDT_SEL(dt).toString())
    }

    fun selectAllBeforeDate(year: Int, month: Int, day: Int, hour: Int,
                            minute: Int, second: Int): List<RSMapper> {
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


//------------------------------------------------------------------------------------------------------------
// The functions below implement the queries on the DB. verifyDBsExist simply checks that the requests DB
// shards are in place on the disk where they're expected to be. If this fails the program dies...something
// is very wrong if this failed. genericSelect is a generalized DB query function which gives a list of
// selection workers will search the proper DB shards.
//------------------------------------------------------------------------------------------------------------

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
            logger.exception(e)
        }
        catch(e: ExecutionException){
            logger.exception(e)
        }
        executor.shutdown()

        logger.info("$SQLStatement --- ${results.size} results.")

        // The research machine has multiple DB shards which makes "orderby" requests almost useless. The code
        // below will search for orderby in the query, if it exists it will determine which column name the
        // ordering is to be done on and if the ordering is ascending or decending (default assumes
        // ascending). After checking for the order by command and sorting (if necessary) we check for a limit
        // command. If there is a limit command return only the requested number of results, in sorted order
        // (by order by, or created_dt if no order by)
        val finalResults = handleLimit(SQLStatement, handleOrderBy(SQLStatement, results))

        // Reset the hasBeenSorted var for future use, if necessary
        if(this.hasBeenSorted) this.hasBeenSorted = false

        return finalResults
    }

    /*
        Takes in the query and the results, checks the query to see if it contains an orderby clause, if it
        does it will process the orderby, determine how to sort the results based on the columns the ordering
        should be done on and if the ordering should be ascending (default) or descending, then it will return
        the properly sorted results. If there is no orderby clause the results will be returned unmodified.
     */
    private fun handleOrderBy(query: String, results: List<RSMapper>): List<RSMapper>{
        // If the query doesn't contain "orderby" and it doesn't contain "order by", or the query returned no
        // results return the results without further processing
        if(results.isEmpty()) return results
        if(!query.toLowerCase().contains("orderby") && !query.toLowerCase().contains("order by"))
            return results

        // Tell the class that we're going through witht he sorting
        this.hasBeenSorted = true

        // Not entirely convinced this works yet, needs significantly more testing
        logger.warn("order by sorting is in beta, it may fail or throw an error, please check results!")

        // Determine which columns names should be sorted, and if it should be sorted ascending or descending
        val columnsAndOrders = determineOrderByColumns(query.toLowerCase())

        return doTheSort(results, columnsAndOrders)
    }

    /*
        Determine which column should be used in the sorting when an orderby clause is added to the SQL query
        It takes in the query string and returns a map of column names and a boolean. The boolean is true when
        the sorting should be done in ascending order (the default) and false when it should be done in
        decending order.
     */
    private fun determineOrderByColumns(query: String): OrderBySelection {
        val theOrdering = OrderBySelection()
        // Split the string on the order by command, the left side doesn't matter, right side has the order
        // by info
        val orderBySplits = query.split("order by")

        // Split the columns names (with asc or desc) on comma
        val columnsWithOrdering = orderBySplits[1].split(",")
        for(columnAndOrder in columnsWithOrdering) {
            // Clean up the spaces from the beginning or end of the string
            val column = columnAndOrder.trim()

            // Split the column name from the asc or desc tag
            val columnNameAndOrder = column.split(" ")

            // See if the column exists in the the table list, if it doesn't get out of here
            val col = columnNameAndOrder[0]
            if(!this.listOfColumns.contains(col)){
                logger.warn("Table does not appear to have column: $col")
                break
            }

            // Get the "asc" or "desc" from the second part of the string
            val order = columnNameAndOrder[1]
            var orderBool = true
            if(order == "desc" || order == "descending")
                orderBool = false

            theOrdering.addColumn(col, orderBool)
        }

        return theOrdering
    }

    /*
        The columnName is the column that the sort needs to be based on
        If isAscending is true the data should be sorted in ascending order, if it's false it should be
        sorted in descending order
     */
    private fun doTheSort(results: List<RSMapper>, columnsWithOrder: OrderBySelection): List<RSMapper> {
        val mutableResults = results.toMutableList()

        // Set the OrderBySelection before doing the sort. Unfortunately this can't be done in the Comparator
        // function call because the comparator implements a specific interface. Oh well.
        RSMapperComparator.columnsWithOrder = columnsWithOrder

        mutableResults.sortWith(RSMapperComparator)

        // Reset the columnsWithOrder to the default value for future use
        RSMapperComparator.columnsWithOrder = OrderBySelection()

        return mutableResults
    }

    /*
        When the limit command is given it is given to all 6 shards, resulting in 6x as many results as one
        actually wants to receive. This function limits the results as if they're all coming from a single DB,
        as that's what the user expects. If there was an order by command the results this function receives
        will already be sorted as the user wants, if there is no order by command this function will default
        to a sort based on created_dt, the creation time of the element in the DB as this is how the data is
        stored in the DB (for the most part). This will, however, perform a full created_dt sort as there are
        some data inconsistencies around this for stored elements in the DBs
     */
    private fun handleLimit(SQLQuery: String, results: List<RSMapper>): List<RSMapper> {
        // If the query doesn't contain the limit command just return the results
        if(results.isEmpty()) return results
        if(!SQLQuery.toLowerCase().contains("limit")) return results

        // If it hasn't been sorted yet, we need to sort it. Build an OrderBySelection and sort with ascending
        // order using creating time if it exists, else sort by ID
        var sortedResults = results
        if(!this.hasBeenSorted){
            val orderBy = OrderBySelection()

            if(results[0].getString(Finals.CREATED_DT) != "")
                orderBy.addColumn(Finals.CREATED_DT, true)
            else
                orderBy.addColumn(Finals.ID, true)

            sortedResults = doTheSort(results, orderBy)
        }

        // Parse the string to determine what the limit value is
        val limitNumber = getLimitNumber(SQLQuery)

        // Check to make sure we'll stay within the bounds of the results array.
        // NOTE: This is >= so that we can just return the results if the number of expected results is equal
        // to the number of results we have. If this was just ">" (as one would expect) we would unnecessarily
        // copy the results without ignoring any of the returned results
        if(limitNumber >= sortedResults.size)
            return sortedResults

        // New list to put the limited results in
        val limitedResults = ArrayList<RSMapper>()

        for(i in 0 until limitNumber)
            limitedResults.add(sortedResults[i])

        return limitedResults
    }

    /*
        Parse an SQLQuery for the limit command and get the number of results the user expects to receive
     */
    private fun getLimitNumber(SQLStatement: String): Int {
        // Splitting on limit guarantees the first word in the second array element will be the number of
        // results the user expects (assuming a proper SQLStatement)
        val splitOnLimit = SQLStatement.split("limit")

        // Splitting it on spaces guarantees that the first elements in the split will be the limit number
        // after cleaning up any extra leading or trailing spaces. This works when the limit command is the
        // last command as well as when it's burried in a string with a bunch of other commands
        val limitNumberStringSplits = splitOnLimit[1].trim().split(" ")

        // Get the limit number and perform a final clean. There shouldn't be any non-printing characters on
        // the string now but clean it up all the same
        val limitNumber = limitNumberStringSplits[0].trim()

        // Return the limit number as an integer
        return limitNumber.toInt()
    }

    /*
        Kill the program if the DBs don't exist or a shard is missing
     */
    protected fun verifyDBsExist(DBs: List<String>) {
        if(DBs.isEmpty())
            logger.logAndKill("Selector.verifyDBsExist DBs was empty")
        if(DBs.size != Finals.DB_SHARD_NUM) {
            logger.err("Selector.verifyDBsExist DBs.size = ${DBs.size} | " +
                    "Finals.DB_SHARD_NUM = ${Finals.DB_SHARD_NUM}")
            logger.logAndKill("Selector.verifyDBsExist DBs.size != Finals.DB_SHARD_NUM")

        }
    }

    /*
        Get selector type based on string matching...stupid idea, will revisit another time
        NOTE: The companion object is kotlin specific
     */
    companion object {
        fun getSelectorOnType(matchingString: String): Selector {
            return when {
                matchingString.toLowerCase().contains(Finals.REDDIT_COM_TABLE) -> RedditComSelector()
                matchingString.toLowerCase().contains(Finals.REDDIT_SUB_TABLE) -> RedditSubSelector()
                else -> throw IllegalArgumentException("Selector.getSelectorOnType is a stupid idea Sean")
            }
        }
    }

}
