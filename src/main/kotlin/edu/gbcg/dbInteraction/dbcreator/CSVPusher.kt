/*
 * Copyright (c) 2018 Sean Grimes. All Rights Reserved.
 * License: MIT
 */

package edu.gbcg.dbInteraction.dbcreator

abstract class CSVPusher: DataPusher {
    constructor(): super()
    constructor(dbPath: String, columnNames: List<String>, tableName: String) :super(dbPath, columnNames, tableName)


    // Need to override run() here and actually implement something

}