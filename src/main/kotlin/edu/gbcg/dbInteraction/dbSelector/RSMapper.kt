/*
 * Copyright (c) 2018 Sean Grimes. All Rights Reserved.
 * License: MIT
 */

package edu.gbcg.dbInteraction.dbSelector

import edu.gbcg.dbInteraction.TimeUtils
import edu.gbcg.utils.TSL
import java.sql.ResultSet
import java.sql.SQLException
import java.time.LocalDateTime

/**
 * Builds the ResultSet mapper object. It will run through a list of column names and pull data from a ResultSet for
 * storage in memory. If a column name does not appear in the ResultSet it will skip that column while reading the data
 * NOTE: A 3rd utility mapper class is needed to prevent a switch over class type, BaseMapper. This class doesn't
 * implement any functionality regarding pulling data from a ResultSet but is used to give data back to the user from
 * buildMappersImpl.
 *
 * NOTE: Storing all elements as a String avoids the type casting when pulling data from the ResultSet which delays
 * the type cast until the value is pull from the internal map stored here, if the value is pulled as something other
 * than a string.
 */
abstract class RSMapper {
    protected var map: Map<String, String> = HashMap()
    protected val logger_ = TSL.get()

    constructor()
    constructor(map: Map<String, String>) {
        this.map = map
    }

    /**
     * Get the value as a string
     * @param key Column name
     * @return The value or ""
     */
    fun getString(key: String): String {
        return getItem(key)
    }

    /**
     * Get the value as an int
     * @param key Column name
     * @return The value or 0 if the value can't be returned as an int
     */
    fun getInt(key: String): Int {
        val stringVal = getItem(key)
        if(stringVal == "") return 0
        val value: Int
        try{
            value = stringVal.toInt()
        }
        catch(e: NumberFormatException){
            logger_.exception(e)
            logger_.err("RSMapper.getInt conversion failed")
            throw e
        }
        return value
    }

    /**
     * Get the value as a long
     * @param key Column name
     * @return The value or 0 if the value can't be returned as a long
     */
    fun getLong(key: String): Long {
        val stringVal = getItem(key)
        if(stringVal == "") return 0
        val value: Long
        try{
            value = stringVal.toLong()
        }
        catch(e: NumberFormatException){
            logger_.exception(e)
            logger_.err("RSMapper.getLong conversion failed")
            throw e
        }
        return value
    }

    /**
     * Get the value as an LocalDateTime object. One assumes this is only called for columns that contain time stored
     * in UTC seconds
     * @param key
     * @return The LocalDateTime object if possible, else null
     */
    fun getLTDFromColumnHoldingUTCSeconds(key: String): LocalDateTime? {
        val time = getLong(key)
        if(time == 0L){
            logger_.err("RSMapper.getLDTFromColumnHoldingUTCSeconds unable to create LDT object")
            throw IllegalArgumentException("RSMapper.getLDTFromColumnHoldingUTCSeconds unable to create LDT object")
        }
        return TimeUtils.utcSecondsToLDT(time)
    }

    /**
     * Get the value as a double
     * @param key Column name
     * @return The value or 0.0 if the value can't be returned as a double
     */
    fun getDouble(key: String): Double {
        val stringVal = getItem(key)
        if(stringVal == "") return 0.0
        val value: Double
        try{
            value = stringVal.toDouble()
        }
        catch(e: NumberFormatException){
            logger_.exception(e)
            logger_.err("RSMapper.getDouble conversion failed")
            throw e
        }
        return value
    }

    /**
     * Get the value as a boolean
     * @param key Column name
     * @return The value or false if the value can't be returned as a double
     */
    fun getBoolean(key: String): Boolean {
        return getInt(key) == 1
    }

    /**
     * Return all items from a given row as a list
     * @return All items from row as a List of strings
     */
    fun getAllItemsAsString(): List<String> {
        var vals = ArrayList<String>()
        vals.addAll(this.map.values)
        return vals
    }

    /**
     * Get the underlying map of string, string (column names, column values)
     * @return The underlying map
     */
    fun getMapAsStrings(): Map<String, String> {
        return this.map
    }

    /**
     * Build a list of RSmappers from a ResultSet object
     * @param rs
     * @return The list of RSMappers
     */
    abstract fun buildMappers(rs: ResultSet): MutableList<RSMapper>?

    /*
        Return item or ""
     */
    protected fun getItem(key: String): String {
        val res = this.map[key]
        if(res == null) {
            logger_.err("RSMapper.getItem() No value for $key")
            throw IllegalArgumentException("RSMapper.getItem() No value for $key")
        }
        return res
    }

    /*
        The implementation of buildMappers
     */
    protected fun buildMappersImpl(rs: ResultSet, colNames: List<String>): MutableList<RSMapper> {
        var maps = ArrayList<RSMapper>()

        if(rs == null)
            return maps

        var colIDs = HashMap<String, Int>()
        try{
            // If there are no results for a search the resultset will appear closed
            if(rs.isClosed)
                return maps

            // Find the index for each column to prevent a lot of string comparisons
            for(col in colNames){
                var colIDX: Int
                // Need to do this in a try-catch. When a non-* query is performed the ResultSet will NOT contain all
                // columns from the DB and trying to find non-existant columns will throw an SQLException. In the
                // future this should be optimized to skip trying all columns and only try those from the query
                try{
                    colIDX = rs.findColumn(col)
                }
                catch(ex: SQLException){
                    continue
                }
                colIDs[col] = colIDX
            }

            // Loop through all results that were found
            while(rs.next()){
                var map = HashMap<String, String>()

                // For each column, check to see if we have a value and if so, add it to the map
                // NOTE: using the keyset instead of colNames because columns are missing from non-* queries
                for(col in colIDs.keys)
                    map.put(col, rs.getString(colIDs[col]!!))   // Null safety...sick

                // Add it to the list
                maps.add(BaseMapper(map))
            }
        }
        catch(e: SQLException){
            logger_.exception(e)
        }
        return maps
    }
}
