/*
 * Copyright (c) 2018 Sean Grimes. All Rights Reserved.
 * License: MIT
 */

package edu.gbcg.dbInteraction

import java.sql.Connection

/**
 * Execute a statement against a DB connection in a thread.
 * Currently used for creating DB indices
 */
class DBWorker(private val conn: Connection, private val SQLStatement: String): Runnable{
    override fun run(){ DBCommon.execute(conn, SQLStatement) }
}