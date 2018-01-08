/*
 * Copyright (c) 2018 Sean Grimes. All rights reserved.
 * License: MIT License
 */

package edu.gbcg.dbInteraction.dbSelector;

import edu.gbcg.dbInteraction.TimeUtils;
import edu.gbcg.utils.TSL;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Builds the ResultSet mapper object. It will run through a list of column names and pull data from a ResultSet for
 * storage in memory. If a column name does not appear in the ResultSet it will skip that column while reading the data
 * NOTE: A 3rd utility mapper class is needed to prevent a switch over class type, BaseMapper. This class doesn't
 * implement any functionality regarding pulling data from a ResultSet but is used to give data back to the user from
 * buildMappers_impl.
 *
 * NOTE: Storing all elements as a String avoids the type casting when pulling data from the ResultSet which delays
 * the type cast until the value is pull from the internal map stored here, if the value is pulled as something other
 * than a string.
 */
public abstract class RSMapper {
    protected Map<String, String> map = new HashMap<>();
    protected TSL logger = TSL.get();

    /**
     * Given a map of column names to column data, create an RSMapper
     * @param map
     */
    public RSMapper(Map<String, String> map){ this.map = map; }
    public RSMapper(){}

    /**
     * Get the value as a string
     * @param key Column name
     * @return The value or ""
     */
    public String getString(String key){
        return getItem(key);
    }

    /**
     * Get the value as an int
     * @param key Column name
     * @return The value or 0 if the value can't be returned as an int
     */
    public int getInt(String key){
        String stringVal = getItem(key);
        if(stringVal == null || stringVal == "") return 0;
        int val;
        try{
            val = Integer.parseInt(key);
        }
        catch(NumberFormatException e){
            logger.warn("NFE RSMapper.getInt");
            return 0;
        }
        return val;
    }

    /**
     * Get the value as a long
     * @param key Column name
     * @return The value or 0 if the value can't be returned as a long
     */
    public long getLong(String key){
        String stringVal = getItem(key);
        if(stringVal == null || stringVal == "") return 0;
        long val;
        try{
            val = Long.parseLong(key);
        }
        catch(NumberFormatException e){
            logger.warn("NFE RSMapper.getLong");
            return 0;
        }
        return val;
    }

    /**
     * Get the value as an LocalDateTime object. One assumes this is only called for columns that contain time stored
     * in UTC seconds
     * @param key
     * @return The LocalDateTime object if possible, else null
     */
    public LocalDateTime getLTDFromColumnHoldingUTCSeconds(String key){
        long time = getLong(key);
        if(time == 0) {
            logger.warn("RSMapper.getLDTFromColumnHoldingUTCSeconds unable to create LDT object");
            return null;
        }
        return TimeUtils.utcSecondsToLDT(time);
    }

    /**
     * Get the value as a double
     * @param key Column name
     * @return The value or 0.0 if the value can't be returned as a double
     */
    public double getDouble(String key){
        String stringVal = getItem(key);
        if(stringVal == null || stringVal == "") return 0.0d;
        double val;
        try{
            val = Double.parseDouble(stringVal);
        }
        catch(NumberFormatException e){
            logger.warn("NFE RSMapper.getDouble");
            return 0.0d;
        }
        return val;
    }

    /**
     * Get the value as a boolean
     * @param key Column name
     * @return The value or false if the value can't be returned as a double
     */
    public boolean getBoolean(String key){
        return getInt(key) == 1 ? true : false;
    }

    /**
     * Return all items from a given row as a list
     * @return All items from row as a List of strings
     */
    public List<String> getAllItemsAsStrings(){
        List<String> vals = new ArrayList<>();
        vals.addAll(this.map.values());
        return vals;
    }

    /**
     * Get the underlying map of string, string (column names, column values)
     * @return The underlying map
     */
    public Map<String, String> getMapAsStrings(){
        return this.map;
    }

    /**
     * Build a list of RSmappers from a ResultSet object
     * @param rs
     * @return The list of RSMappers
     */
    public abstract List<RSMapper> buildMappers(ResultSet rs);

    /*
        Return item or ""
     */
    protected String getItem(String key){
        return this.map.getOrDefault(key, "");
    }

    /*
        The implementation of buildMappers
     */
    protected List<RSMapper> buildMappers_impl(ResultSet rs, List<String> colNames){
        List<RSMapper> maps = new ArrayList<>();

        if(rs == null)
            return maps;

        Map<String, Integer> colIDs = new HashMap<>();
        try{
            // If there are no results for a search the resultset will appear closed
            if(rs.isClosed())
                return maps;

            // Find the index for each column to prevent a lot of string comparisons
            for (String col : colNames) {
                int colIDX;
                // Need to do this in a try-catch. When a non-* query is performed the ResultSet will NOT contain all
                // columns from the DB and trying to find non-existant columns will throw an SQLException. In the
                // future this should be optimized to skip trying all columns and only try those from the query
                try{
                    colIDX = rs.findColumn(col);
                }
                catch(SQLException e){
                    continue;
                }
                colIDs.put(col, colIDX);
            }

            // Loop through all results that were found
            while(rs.next()){
                Map<String, String> map = new HashMap<>();

                // For each column, check to see if we have a value and if so, add it to the map
                // Note: using the keyset instead of colNames because columns are missing from non-* queries
                for(String col : colIDs.keySet())
                    map.put(col, rs.getString(colIDs.get(col)));

                // Add it to the list
                maps.add(new BaseMapper(map));
            }

        }catch(SQLException e){
            logger.err("RSMapper.buildMappers SQLException");
            e.printStackTrace();
        }
        return maps;
    }
}
