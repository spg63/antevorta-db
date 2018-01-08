/*
 * Copyright (c) 2018 Sean Grimes. All Rights Reserved.
 * License: MIT
 */

package edu.gbcg.dbInteraction.dbcreator

import edu.gbcg.dbInteraction.DBCommon
import java.sql.Connection

class IndexWorker(conn: Connection, SQLStatement: String): Runnable{
    private val conn = conn
    private val SQLStatement = SQLStatement

    override fun run(){ DBCommon.execute(conn, SQLStatement) }
}