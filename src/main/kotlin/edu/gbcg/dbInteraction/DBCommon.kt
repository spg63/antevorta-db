/*
 * Copyright (c) 2018 Sean Grimes. All Rights Reserved.
 * License: MIT
 */

@file:Suppress("unused")

package edu.gbcg.dbInteraction

import edu.gbcg.configs.Finals
import edu.gbcg.utils.DBUtils
import edu.gbcg.utils.TSL
import java.sql.Connection
import java.sql.ResultSet
import java.sql.SQLException


/**
 * Class to hold functionality shared among DB classes and wrapping the generalized functions from DBUtils to handle
 * things like data paths, db locations, db type, and whether or not to enforce foreign keys. Basically just wraps up
 * DBUtils with default values for this specific project to simplify function calls in higher level code
 */

object DBCommon{
    @JvmField val enforceForeignKeys = Finals.enableForeignKeys
    @JvmField val dbPrefix = Finals.DB_URL_PREFIX
    @JvmField val dbDriver = Finals.DB_DRIVER

    /**
     * Gets a DB Connection object based on db name, the URL to the DB and the type of DB driver in
     * use. The URL prefix and DB Driver class are both found in Finals
     * @param db Name of the database
     * @return The connection to the db
     */
    @JvmStatic fun connect(db: String): Connection {
        val conn = DBUtils.get().connect(db, dbPrefix, dbDriver, enforceForeignKeys)
        if(Finals.SYNC_MODE_OFF){
            try{
                val st = conn.createStatement()
                val sql = "PRAGMA synchronous=OFF"
                st.execute(sql)
                st.close()
            }
            catch(e: SQLException){
                TSL.get().logAndKill(e)
            }
        }
        return conn
    }

    /**
     * Close database connection
     * @param conn The DB connection
     */
    @JvmStatic fun disconnect(conn: Connection) {
        DBUtils.get().disconnect(conn)
    }

    /**
     * Perform a DB insertion
     * NOTE: The connection will not be closed for you
     * @param conn The connection to the DB
     * @param SQLStatement The SQLstatement, as a string
     */
    @JvmStatic fun insert(conn: Connection, SQLStatement: String) {
        DBUtils.get().insert(conn, SQLStatement)
    }

    /**
     * Perform a DB insertion
     * NOTE: There is no connection to close
     * @param db The path to the DB
     * @param SQLStatement The SQLstatement, as a string
     */
    @JvmStatic fun insert(db: String, SQLStatement: String) {
        DBUtils.get().insert(db, dbPrefix, dbDriver, SQLStatement, enforceForeignKeys)
    }

    /**
     * Perform a DB deletion operation
     * NOTE: The connection will not be closed for you
     * @param conn The connection to the DB
     * @param SQLStatement The SQLStatement, as a string
     */
    @JvmStatic fun delete(conn: Connection, SQLStatement: String) {
        DBUtils.get().delete(conn, SQLStatement)
    }

    /**
     * Perform a DB deletion operation
     * NOTE: There is no connection to close
     * @param db The path to the DB
     * @param SQLStatement The SQL Statement, as a string
     */
    @JvmStatic fun delete(db: String, SQLStatement: String) {
        DBUtils.get().delete(db, dbPrefix, dbDriver, SQLStatement, enforceForeignKeys)
    }

    /**
     * Executes a batch insertion. There is no batch size limit. This function assumes the user
     * has properly split the insertion into managable chunks.
     * NOTE: The connection will not be closed for you
     * @param conn The DB Connection
     * @param SQLStatements A list of SQL statements
     */
    @JvmStatic fun insertAll(conn: Connection, SQLStatements: List<String>) {
        DBUtils.get().insertAll(conn, SQLStatements)
    }

    /**
     * Executes a batch insertion. There is no batch size limit. This function assumes the user
     * has properly split the insertion into manageable chunks.
     * NOTE: There is no connection to close
     * @param db The path to the DB
     * @param SQLStatements A list of SQL statements
     */
    @JvmStatic fun insertAll(db: String, SQLStatements: List<String>) {
        DBUtils.get().insertAll(db, dbPrefix, dbDriver, SQLStatements, enforceForeignKeys)
    }

    /**
     * Executs a batch deletion. There is no batch size limit. This function assumes the user has
     * properly split the deletion into manageable chunks.
     * NOTE: The connection will not be closed for you
     * @param conn The DB connection
     * @param SQLStatements A list of SQL statements
     */
    @JvmStatic fun deleteAll(conn: Connection, SQLStatements: List<String>) {
        DBUtils.get().deleteAll(conn, SQLStatements)
    }

    /**
     * Executs a batch deletion. There is no batch size limit. This function assumes the user has
     * properly split the deletion into manageable chunks.
     * @param db The path ot the DB
     * @param SQLStatements A list of SQL statements
     */
    @JvmStatic fun deleteAll(db: String, SQLStatements: List<String>) {
        DBUtils.get().deleteAll(db, dbPrefix, dbDriver, SQLStatements, enforceForeignKeys)
    }

    /**
     * Get a single result set from a single selection statement
     * NOTE: The connection must remain open while you require access to the ResultSet
     * @param conn The connection to the DB
     * @param SQLStatement SQL select statement
     * @return A ResultSet if the selection was successful
     */
    @JvmStatic fun select(conn: Connection, SQLStatement: String): ResultSet {
        return DBUtils.get().select(conn, SQLStatement)
    }

    /**
     * Performs a batch selection
     * NOTE: The connection must remain open while you require access to the ResultSet
     * @param conn The connection to the DB
     * @param SQLStatements SQL select statements
     * @return A list of ResultSet objects if the selections were successful
     */
    @JvmStatic fun selectAll(conn: Connection, SQLStatements: List<String>): List<ResultSet> {
        return DBUtils.get().selectAll(conn, SQLStatements)
    }

    /**
     * Perform a non insert, delete, update operation
     * @param conn
     * @param SQLStatement
     */
    @JvmStatic fun execute(conn: Connection, SQLStatement: String) {
        DBUtils.get().execute(conn, SQLStatement)
    }

    /**
     * Get a SQL string for index creation
     * @param table Table to create the index for
     * @param columnToIndex Column name the index is being created on
     * @param indexName What to call the index
     * @return The SQL string
     */
    @JvmStatic fun getDBIndexSQLStatement(table: String, columnToIndex: String, indexName: String): String {
        return "create index $indexName on $table ($columnToIndex);"
    }

    /**
     * Get a SQL string to drop an existing index
     * @param indexName The index name to drop
     * @return The SQL string
     */
    @JvmStatic fun getDropDBIndexSQLStatement(indexName: String): String {
        return "drop index if exists '$indexName';"
    }

    /**
     * Close an open resultset, prevents try/catch all over the code
     * @param rs
     */
    @JvmStatic fun closeResultSet(rs: ResultSet) {
        DBUtils.get().closeResultSet(rs)
    }
}
