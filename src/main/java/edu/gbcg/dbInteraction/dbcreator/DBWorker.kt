/*
 * Copyright (c) 2018 Sean Grimes. All Rights Reserved.
 * License: MIT
 */

package edu.gbcg.dbInteraction.dbcreator

import edu.gbcg.dbInteraction.DBCommon
import java.sql.Connection

class DBWorker(private val conn: Connection, private val SQLStatement: String): Runnable{
    override fun run(){ DBCommon.execute(conn, SQLStatement) }
}