/*
 * Copyright (c) 2018 Sean Grimes. All rights reserved.
 * License: MIT License
 */

package edu.antevorta.dbInteraction.dbSelector

import edu.antevorta.dbInteraction.DBCommon

import java.util.concurrent.Callable

/**
 * SelectionWorker is designed to work for any DB objects. In order to do this is needs to know the path to the DB,
 * the query to run on the DB, and which time os RSMapper object it will be populating. Each mapper object handles
 * the specifics of pulling data out of a ResultSet for a given DB connection
 */
class SelectionWorker
/**
 * Build a SelectionWorker
 * @param dbPath The path to the DB file
 * @param SQLQuery The query to run on the DB
 * @param rsMapper The mapper object to be populated with data from the ResultSet
 */
(private val dbPath: String, private val SQLQuery: String, private val rsMapper: RSMapper) : Callable<Any> {

    /**
     * A thread running on a single DB shard
     * @return A list of populated RSMapper objects
     */
    override fun call(): MutableList<RSMapper>? {
        // Connect to the DB
        val conn = DBCommon.connect(this.dbPath)

        // Create the ResultSet
        val rs = DBCommon.select(conn, this.SQLQuery)

        // Pull all results from the ResultSet
        val mappers = this.rsMapper.buildMappers(rs)

        // Close the ResultSet, we've got all the data
        DBCommon.closeResultSet(rs)

        // Close the connection, we're done
        DBCommon.disconnect(conn)
        return mappers
    }
}
