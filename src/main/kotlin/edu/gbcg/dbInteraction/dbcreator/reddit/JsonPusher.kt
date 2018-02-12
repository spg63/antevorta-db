/*
 * Copyright (c) 2018 Sean Grimes. All Rights Reserved.
 * License: MIT
 */

package edu.gbcg.dbInteraction.dbcreator.reddit

import org.json.JSONObject

abstract class JsonPusher: Runnable {
    protected var json_objects: ArrayList<JSONObject>

    var JSONStrings: List<String>
    // Custom setter for JSONStrings so that numObjects also gets set when setting JSONStrings outside of c'tor
    set(value){
        field = value
        this.numObjects = this.JSONStrings.size
    }

    lateinit var DB: String
    lateinit var columns: List<String>
    lateinit var tableName: String
    protected var numObjects = 0


    constructor(){
        this.json_objects = ArrayList()
        this.JSONStrings = ArrayList()
    }

    constructor(dbPath: String, jsonLines: List<String>, columnNames: List<String>, tableName: String){
        this.DB = dbPath
        this.JSONStrings = jsonLines
        this.columns = columnNames
        this.tableName = tableName
        this.numObjects = JSONStrings.size
        this.json_objects = ArrayList()
    }

    override fun run(){
        for(i in 0 until this.numObjects)
            this.json_objects.add(JSONObject(this.JSONStrings[i]))
        parseAndPushDataToDB()
    }

    // Setters and getters are automatically generated for the class vars

    protected fun buildInsertionString(): String {
        var sb = StringBuilder()
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
        sb.append(this.columns[this.columns.size - 1])

        // Start the values string
        sb.append(") VALUES (")
        for(i in 1 until this.columns.size - 1)
            sb.append("?,")
        sb.append("?);")

        return sb.toString()
    }

/*
// Want to automate the grabbing of json values and putting them into a PreparedStatement, unfortunately I'm not sure
// how to deal with nested json objects in unknown datasets.
    protected fun setElementInPS(ps: PreparedStatement, keyName: String, datatype: String){
        // Text insertion
        if(keyName.startsWith("TEXT")){

        }

        // Boolean insertion
        else if("INTEGER DEFAULT 0".equals(keyName) || "INTEGER DEFAULT 0,".equals(keyName)){

        }


    }
*/
    protected abstract fun parseAndPushDataToDB()
}