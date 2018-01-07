/*
 * Copyright (c) 2018 Sean Grimes. All rights reserved.
 * License: MIT License
 */

package edu.gbcg.dbInteraction.dbSelector;

import edu.gbcg.configs.Finals;
import edu.gbcg.dbInteraction.TimeUtils;
import edu.gbcg.utils.TSL;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * This class implements all shared functionality for DB selections. Based on the type of SelectionWorker passed to
 * the genericSelect function it will query the proper DB files and use the proper RSMapper object
 */
public abstract class Selector {
    protected String tableName;
    protected TSL logger = TSL.get();

    public Selector(){}

//----------------------------------------------------------------------------------------------------------------------
// NOTE: This can be used when one of the functions below doesn't satisfy your querying needs. I suggest just writing
// the function below unless you're absolutely positive the query is a one-off so you don't need to keep writing the
// same SQL string.
//----------------------------------------------------------------------------------------------------------------------
    public abstract List<RSMapper> generalSelection(String SQLStatement);


//----------------------------------------------------------------------------------------------------------------------
// The following functions take the tableName variable (set the in the derived class default c'tors) and create an
// sql query string using the DBSelector class. The query string is passed back down to a derived class through
// generalSelection to ascertain which workers to use and which DB shards to search before calling the generalized
// genericSelect function below.
//----------------------------------------------------------------------------------------------------------------------

    public List<RSMapper> selectAllFromAuthor(String author){
        return selectAllWhereColumnEquals(Finals.AUTHOR, author);
    }

    public List<RSMapper> selectAllFromAuthorOrderBy(String author, String orderBy){
        DBSelector selector = new DBSelector()
                .from(this.tableName)
                .where(Finals.AUTHOR + " = '"+author+"'")
                .orderBy(orderBy);
        return generalSelection(selector.sql());
    }

    public List<RSMapper> selectAllWhereColumnEquals(String columnName, String equalsValue){
        DBSelector selector = new DBSelector()
                .from(this.tableName)
                .where(columnName + " = '" + equalsValue + "'");
        return generalSelection(selector.sql());
    }

    public List<RSMapper> selectAllWhereColumnLessThan(String columnName, String lessThanValue){
        DBSelector selector = new DBSelector()
                .from(this.tableName)
                .where(columnName + " < '" + lessThanValue + "'");
        return generalSelection(selector.sql());
    }

    public List<RSMapper> selectAllWhereColumnGreaterThan(String columnName, String greaterThanValue){
        DBSelector selector = new DBSelector()
                .from(this.tableName)
                .where(columnName + " > '" + greaterThanValue + "'");
        return generalSelection(selector.sql());
    }

    public List<RSMapper> selectAllWhereColumnEqualsAndColumnAboveValue(String column, String equalsVal,
                                                                        String filter, String val){
        DBSelector selector = new DBSelector()
                .from(this.tableName)
                .where(column + " = '" + equalsVal + "'")
                .and(filter + " > '" + val + "'");
        return generalSelection(selector.sql());
    }

    //------ NEED MUCH MORE DATE FUNCTIONS

    public List<RSMapper> selectAllAfterDate(int year, int month, int day, int hour, int minute, int second) {
        long utc = TimeUtils.utcSecondsFromValues_SEL(year, month, day, hour, minute, second);
        return selectAllWhereColumnGreaterThan(Finals.CREATED_DT, Long.toString(utc));
    }

    public List<RSMapper> selectAllAfterDate(LocalDateTime dt){
        return selectAllWhereColumnGreaterThan(Finals.CREATED_DT, Long.toString(TimeUtils.utcSecondsFromLDT_SEL(dt)));
    }

    public List<RSMapper> selectAllBeforeDate(int year, int month, int day, int hour, int minute, int second){
        long utc = TimeUtils.utcSecondsFromValues_SEL(year, month, day, hour, minute, second);
        return selectAllWhereColumnLessThan(Finals.CREATED_DT, Long.toString(utc));
    }

    public List<RSMapper> selectAllBeforeDate(LocalDateTime dt){
        return selectAllWhereColumnLessThan(Finals.CREATED_DT, Long.toString(TimeUtils.utcSecondsFromLDT_SEL(dt)));
    }

    public List<RSMapper> selectAllBetweenDates(int start_year, int start_month, int start_day,
                                                int start_hour, int start_minute, int start_second,
                                                int end_year, int end_month, int end_day,
                                                int end_hour, int end_minute, int end_second){
        LocalDateTime start = LocalDateTime.of(start_year,start_month,start_day,start_hour,start_minute,start_second);
        LocalDateTime end = LocalDateTime.of(end_year, end_month, end_day, end_hour, end_minute, end_second);
        return selectAllBetweenDates(start, end);
    }

    public List<RSMapper> selectAllBetweenDates(LocalDateTime start, LocalDateTime end){
        long startDate = TimeUtils.utcSecondsFromLDT_SEL(start);
        long endDate = TimeUtils.utcSecondsFromLDT_SEL(end);
        DBSelector selector = new DBSelector()
                .from(this.tableName)
                .where(Finals.CREATED_DT + " > '" + startDate + "'")
                .and(Finals.CREATED_DT + " < '" + endDate + "'");
        return generalSelection(selector.sql());
    }



//---------- Generalized version for named functions
    public List<RSMapper> selectAllFromColumn(String column){
        return null;
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
    protected List<RSMapper> genericSelect(List<SelectionWorker> workers, String SQLStatement){
        List<Future<List<RSMapper>>> future_results = new ArrayList<>();
        ExecutorService executor = Executors.newFixedThreadPool(Finals.DB_SHARD_NUM);
        for(SelectionWorker worker : workers)
            future_results.add(executor.submit(worker));

        List<RSMapper> results = new ArrayList<>();
        try{
            for(int i = 0; i < Finals.DB_SHARD_NUM; ++i)
                results.addAll(future_results.get(i).get());
        }
        catch(InterruptedException e){
            logger.err("Selector.genericSelect InterruptedException");
        }
        catch(ExecutionException ee){
            logger.err("Selector.genericSelect ExecutionException");
        }

        executor.shutdown();

        if(results.isEmpty()) {
            logger.info(SQLStatement + " --- 0 results.");
            return null;
        }
        logger.info(SQLStatement + " --- " + results.size() + " results.");
        return results;
    }
    /*
        Kill the program if the DBs don't exist or a shard is missing
     */
    protected void verifyDBsExist(List<String> DBs) {
        if (DBs == null) {
            logger.err("Selector.verifyDBsExist DBs was null");
            throw new RuntimeException("Selector.verifyDBsExist DBs was null");
        }
        if (DBs.size() != Finals.DB_SHARD_NUM) {
            logger.err("Selector.verifyDBsExist DBs.size() != Finals.DB_SHARD_NUM");
            throw new RuntimeException("Selector.verifyDBsExist DBs.size() != Finals.DB_SHARD_NUM");
        }
    }
}
