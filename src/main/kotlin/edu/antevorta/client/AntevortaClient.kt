/*
 * Copyright (c) 2018 Sean Grimes. All Rights Reserved.
 * License: MIT
 */

package edu.antevorta.client

import edu.antevorta.utils.TSL
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.*
import java.net.Socket

// Kotlin version of public static vars. Not accessible from Java but no java needs these vars in this system.
// They're used in the companion object to create the config file as well as when the server is queried
const val USER        = "USER"
const val PASS        = "PASS"
const val QUERY       = "QUERY"
const val HOST_NAME   = "HOSTNAME"
const val HOST_PORT   = "HOSTPORT"
const val NOOP_FLAG   = "=*="

@Suppress("unused")
class AntevortaClient(configFilePath: String) {

    private val logger      = TSL.get()
    private val configPath  = configFilePath
    private var hostname    = String()
    private var hostport    = 0
    private var user        = String()
    private var pass        = String()

    // Default c'tor is in the class declaration. The config file needs to be parsed regardless of what c'tor
    // may be called so do it in the init block
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
         * @param pass Your password -- NOTE: THIS IS NOT SECURE, DON'T USE A PASSWORD YOU CURRENTLY OR WILL
         * USE ELSEWHERE!!
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
                TSL.get().exception(e)
            }
        }
    }


    /**
     * Query the server with your sql string. The server will determine which DB to query based on table name
     * in the sql string.
     * @param SQLQuery, the sql compatible query string to use to query the DB
     * @return The JSONArray
     */
    fun queryServer(SQLQuery: String): JSONArray {
        val emptyArray = "[]"
        val results: JSONArray
        try{
            // Open the socket to the server
            val sock = Socket(this.hostname, this.hostport)
            val serverWriter = DataOutputStream(sock.getOutputStream())
            val serverReader = BufferedReader(InputStreamReader(sock.getInputStream()))

            // Build JSONObject, consisting of user, pass, and query string
            val queryObject = buildJSONObject(SQLQuery)

            // Server reads line by line, need to ensure the string ends with a newline char or the server
            // will hang
            serverWriter.writeBytes(queryObject.toString() + "\n")
            serverWriter.flush()

            // Sit here and wait for the server to respond. JSONArray will be returned in one line for easy
            // parsing by the client. This seems to work fine for large results running over TCP, if problems
            // arise this can be revised to transfer raw bytes with better error handling
            var jsonArrayString = serverReader.readLine()

            // String could perhaps be null if a failure occurs, however in practice it should just hang on
            // the readLine() call -- Might be smart to add a timeout for that call that is reasonable to
            // accomodate large results
            if(jsonArrayString == null)
                jsonArrayString = emptyArray

            // Response starts with NOOP_FLAG if the server didn't perform the request, a reason for the
            // failure will come after the flag
            if(jsonArrayString.startsWith(NOOP_FLAG)){
                logger.err("Server failed to return results: ${jsonArrayString.substring(NOOP_FLAG.length)}")
                return JSONArray(emptyArray)
            }

            // NOTE: A query returning no results will return [], a valid (but empty) JSONArray.
            // RSMapperOutput knows how to handle empty results
            results = JSONArray(jsonArrayString)
        }
        catch(e: IOException){
            logger.exception(e)
            return JSONArray()
        }

        return results
    }

//----- Internal methods -------------------------------------------------------------------------------------

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
            logger.exception((e))
        }

        if(fullString.isEmpty()){
            logger.logAndKill("Unable to parse config file")
        }

        val json: JSONObject = try {
            JSONObject(fullString)
        }
        catch(e: JSONException) {
            logger.logAndKill(e)
            JSONObject()
        }

        this.hostname   = json.getString(HOST_NAME)
        this.hostport   = json.getInt(HOST_PORT)
        this.user       = json.getString(USER)
        this.pass       = json.getString(PASS)
    }

    private fun buildJSONObject(SQLQuery: String): JSONObject {
        val json = JSONObject()
        json.put(USER, this.user)
        json.put(PASS, this.pass)
        json.put(QUERY, SQLQuery)

        return json
    }


//----- See the older Java version for usage of this class. At the moment it's still up-to-date --------------


}






















