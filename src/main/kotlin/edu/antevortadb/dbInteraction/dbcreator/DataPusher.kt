/*
 * Copyright (c) 2018 Sean Grimes. All Rights Reserved.
 * License: MIT
 */

package edu.antevortadb.dbInteraction.dbcreator

import edu.antevortadb.dbInteraction.DBCommon
import edu.antevortadb.utils.TSL
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.SQLException

@Suppress("MemberVisibilityCanBePrivate", "PropertyName")
abstract class DataPusher: Runnable {
    lateinit var DB: String
    lateinit var columns: List<String>
    lateinit var tableName: String
    val logger = TSL.get()

    constructor()

    // The stuff the base class should know about
    constructor(dbPath: String, columnNames: List<String>, tableName: String) {
        this.DB = dbPath
        this.columns = columnNames
        this.tableName = tableName
    }

    protected fun buildInsertionString(): String {
        val sb = StringBuilder()
        sb.append("INSERT INTO ")
        sb.append(this.tableName)
        sb.append(" (")

        // Loop through all column names and add them to the insert string
        // NOTE: Starting the loop at 1 to skip the initial ID autoincremenet field
        // Skip the last one to avoid an extra comma
        for(i in 1 until this.columns.size - 1){
            sb.append(this.columns[i])
            sb.append(",")
        }
        // Add the one that was skipped
        sb.append(this.columns[this.columns.size - 1])

        // Start the values string
        sb.append(") VALUES (")
        for(i in 1 until this.columns.size - 1)
            sb.append("?,")
        sb.append("?);")

        return sb.toString()
    }

    // DB specific parsing and pushing of data. This will be implemented in the most
    // derived classes and will be specific to json or csv or other type of data. The
    // function is specific to the data being read and the data being written to the DB
    protected abstract fun parseAndPushDataToDB()

    /*
     *  The below two functions centralize error handling when pushing data into the DB.
     *  These error blocks can be long and theres no reason to repeat them over all of the
     *  different derived classes. Do it here.
     */
    protected fun pusherCatchBlock(e: SQLException?, conn: Connection?){
        logger.exception(e)
        try{
            conn!!.rollback()
        }
        catch(exp: SQLException){
            logger.logAndKill(exp)
        }
    }

    protected fun pusherFinallyBlock(conn: Connection?, ps: PreparedStatement?){
        try{
            if(!conn!!.autoCommit)
                conn.autoCommit = true
        }
        catch(exp: SQLException){
            logger.logAndKill(exp)
        }
        if(ps != null){
            try{
                ps.close()
            }
            catch(ex: SQLException){
                logger.exception(ex)
            }
        }

        DBCommon.disconnect(conn!!)     // Handles the try/catch nonsense
    }
}
