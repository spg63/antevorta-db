/*
 * Copyright (c) 2018 Sean Grimes. All Rights Reserved.
 * License: MIT
 */

package edu.antevorta.dbInteraction.dbcreator

import org.json.JSONObject

/**
 * This class implements the JSON specific code for parsing and pushing the data into a newly created DB
 */
abstract class JsonPusher: DataPusher {
    protected var jsonObjects: ArrayList<JSONObject>
    protected var numObjects = 0

    var JSONStrings: List<String>
    // Custom setter for JSONStrings so that numObjects also gets set when setting JSONStrings outside of c'tor
    set(value){
        field = value
        this.numObjects = this.JSONStrings.size
    }

    constructor(): super() {
        this.jsonObjects = ArrayList()
        this.JSONStrings = ArrayList()
    }

    // Base DataPusher will handle the path to the DB, the names for the table columns, and the table name
    constructor(dbPath: String, jsonLines: List<String>, columnNames: List<String>, tableName: String)
            : super(dbPath, columnNames, tableName)
    {
        this.JSONStrings = jsonLines
        this.numObjects = JSONStrings.size
        this.jsonObjects = ArrayList()
    }

    override fun run(){
        for(i in 0 until this.numObjects)
            this.jsonObjects.add(JSONObject(this.JSONStrings[i]))
        // This is implemented by a derived class, specific to the data being read and data being written to the DB
        parseAndPushDataToDB()
    }
}