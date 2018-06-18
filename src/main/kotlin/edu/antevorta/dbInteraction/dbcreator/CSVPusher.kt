/*
 * Copyright (c) 2018 Sean Grimes. All Rights Reserved.
 * License: MIT
 */

package edu.antevorta.dbInteraction.dbcreator

import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVRecord

/**
 * This class implements CSV specific code for parsing and pushing the data into a newly created DB
 */
abstract class CSVPusher: DataPusher {
    protected var numRecords = 0
    protected val parseFormat = CSVFormat.DEFAULT
    protected val parser = null

    // Custom setter to update numRecords when necessary
    var csvRecords: List<CSVRecord>
    set(value){
        field = value
        this.numRecords = this.csvRecords.size
    }

    constructor(): super() {
        this.csvRecords = ArrayList()
    }

    constructor(dbPath: String, columnNames: List<String>, tableName: String, records: List<CSVRecord>)
            :super(dbPath, columnNames, tableName)
    {
        this.csvRecords = records
        this.numRecords = csvRecords.size
    }

    // Run is simple here, just parse the lines of CSV file
    override fun run() {
        parseAndPushDataToDB()
    }
}