/*
 * Copyright (c) 2018 Sean Grimes. All Rights Reserved.
 * License: MIT
 */

package edu.gbcg.client

import edu.gbcg.utils.TSL
import org.json.JSONArray
import org.json.JSONObject
import java.io.*

// Kotlin version of public static vars. Not accessible from Java but no java needs these vars in this system.
// They're used in the companion object to create the config file as well as when the server is queried
const val USER        = "USER"
const val PASS        = "PASS"
const val QUERY       = "QUERY"
const val HOST_NAME   = "HOSTNAME"
const val HOST_PORT   = "HOSTPORT"
const val NOOP_FLAG   = "=*="

class AntevortaClient(configFilePath: String) {

    private val logger_     = TSL.get()
    private val configPath  = configFilePath
    private var hostname    = String()
    private var hostport    = 0
    private var user        = String()
    private var pass        = String()

    // Default c'tor is in the class declaration. The config file needs to be parsed regardless of what c'tor may be
    // called so do it in the init block
    init{
        parseConfigFile()
    }

    companion object {
        /**
         * Write a config file with the required information
         * @param configFile Path where you want to write the config file, and the config file name
         * @param hostname The host
         * @param hostport The port
         * @param user Your username
         * @param pass Your password -- NOTE: THIS IS NOT SECURE, DON'T USE A PASSWORD YOU CURRENTLY OR WILL USE ELSEWHERE!!
         */
        fun writeConfigFile(configFile: String, hostname: String, hostport: Int, user: String, pass: String) {
            val json = JSONObject()
            json.put(HOST_NAME, hostname)
            json.put(HOST_PORT, hostport)
            json.put(USER, user)
            json.put(PASS, pass)

            // Make the directory if it doesn't exist yet
            val f = File(configFile)
            f.parentFile.mkdirs()

            // Write JSON string to the file
            try {
                val fw = FileWriter(configFile)
                fw.write(json.toString())
            } catch (e: IOException) {
                logger_.exception(e)
            }
        }
    }


    /**
     * Query the server with your sql string. The server will determine which DB to query based on table name in the
     * sql string.
     * @param SQLQuery, the sql compatible query string to use to query the DB
     * @return
     */
    fun queryServer(SQLQuery: String): JSONArray {
        return JSONArray()
    }

//----- Internal methods -----------------------------------------------------------------------------------------------

    private fun parseConfigFile(){
        var fullString = String()
        try{
            val br = BufferedReader(FileReader(this.configPath))
            val sb = StringBuilder()
            var line = br.readLine()
            while(line != null){
                sb.append(line)
                sb.append(System.lineSeparator())
                line = br.readLine()
            }
            fullString = sb.toString()
        }
        catch(e: IOException){
            logger_.exception((e))
        }

        if(fullString.isEmpty()){
            logger_.logAndKill("Unable to parse config file")
        }

        val json        = JSONObject(fullString)
        this.hostname   = json.getString(HOST_NAME)
        this.hostpost   = json.getInt(HOST_PORT)
        this.user       = json.getString(USER)
        this.pass       = json.getString(PASS)
    }

    private fun buildJSONObject(SQLQuery: String): JSONObject {

    }


//----- See the older Java version for usage of this class. At the moment it's still up-to-date ------------------------
    init {
        parseConfigFile()
    }


}






















