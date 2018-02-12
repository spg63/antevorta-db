/*
 * Copyright (c) 2018 Sean Grimes. All Rights Reserved.
 * License: MIT
 */

package edu.gbcg.server

import edu.gbcg.configs.Finals
import edu.gbcg.dbInteraction.dbSelector.RSMapper
import edu.gbcg.dbInteraction.dbSelector.Selector
import edu.gbcg.utils.TSL
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader
import java.net.ServerSocket
import java.net.Socket

/**
 * Note: This server is intentionally capped at 5 threads. Access to the DB (and data processing) is already threaded
 * as much as it reasonably should be. Any threading here will introduce additional latency however the convenience to
 * make multiple requests at the same time out-weighs the downsides. This also allows for a minimal number of
 * users access to the resources concurrently.
 */

var currentThreads = 0
const val MAX_THREADS = 5

class Dolius(private val socket: Socket): Runnable {
    private var sleepCounter = 0
    private val MAX_SLEEP = 5
    private val logger_ = TSL.get()
    private var SERVER_BUSY = false
    private var SANITIZE_FAIL = false
    private var AUTH_FAIL = false
    private val NOOP_FLAG = "=*="
    private val BUSY_STR = "${NOOP_FLAG}Server was busy, unable to process request. Please try again later."
    private val REJT_STR = "${NOOP_FLAG}Server will not perform that kind of work."
    private val AUTH_STR = "${NOOP_FLAG}Server could not authenticate user, please check credentials."
    private val sleepTimeMS: Long = 2000
    private val USER = "USER"
    private val PASS = "PASS"
    private val QUERY = "QUERY"
    private val configHandler = ServerConfigHandler()

    // Log separately connections so I can easily track them
    init{
        while(currentThreads >= MAX_THREADS) {
            logger_.info("Dolius: Sleeping for $sleepTimeMS")
            Thread.sleep(sleepTimeMS)
            sleepCounter++
            if(sleepCounter >= MAX_SLEEP) {
                logger_.warn("Dolius: Reached MAX_SLEEP")
                SERVER_BUSY = true
                break
            }
            logger_.warn("Dolius: currentThreads >= MAX_THREADS")
        }
        currentThreads++
    }

    /**
     * Read the data from the socket, process the data to determine function call, call the function, parse the
     * RSMapper into a list of JSON objects, push the JSON objects back to the client and close the connection
     */
    override fun run() {
        // Get the data from the socket
        // If the server has reached max threads and max sleep, tell client the server is busy and quit
        if(SERVER_BUSY) {
            handleBusy()
            destroy()
            return
        }

        val inputReader = BufferedReader(InputStreamReader(socket.getInputStream()))
        val input = inputReader.readLine()

        // Create the jsonObject from the client data
        val jsonObject = getJsonObject(input)

        if(jsonObject == null){
            handleRejection("Invalid JSON passed to server")
            destroy()
            return
        }

        // Get username and userpass from the json object
        val user = jsonObject.getString(USER)
        val pass = jsonObject.getString(PASS)

        // Authenticate the user
        authenticateUser(user, pass)

        // If auth fails, let client know and quit
        if(AUTH_FAIL){
            handleAuthFailure()
            destroy()
            return
        }

        // The actual query
        val query = jsonObject.getString(QUERY)

        // Basic string sanitization for query
        sanitize(query)

        // If sanitization fails, let client know and quit
        if(SANITIZE_FAIL){
            handleRejection(input)
            destroy()
            return
        }

        logger_.info("Dolius processing: $input")

        // Query the DB and get the RSMappers in return
        val mappers = process(query)

        // Turn the list of RSMappers into a list of JSONObjects
        val jsonArray = JSONArray()
        for(mapper in mappers)
            jsonArray.put(mapper.getAsJSONObject())

        // Write the json objects back to the client
        returnToClient(jsonArray)

        // Decrement thread counter and close the socket
        destroy()
    }



// ---------------------------------------------------------------------------------------------------------------------


    /**
     * Determines which function to call and queries the DB after some basic validation
     */
    private fun process(sqlQuery: String): List<RSMapper> {
        // Get a Selector object. Will auto detect which DB based on the table name. This is not ideal.
        val selector = Selector.getSelectorOnType(sqlQuery)

        // Make the selection, get the RSMappers
        return selector.generalSelection(sqlQuery)
    }

    /**
     * Get a json object from the input string
     */
    private fun getJsonObject(jsonString: String): JSONObject?{
        var obj: JSONObject
        try {
            obj = JSONObject(jsonString)
        }
        catch(e: JSONException){
            logger_.exception(e)
            return null
        }
        return obj
    }

    /**
     * Authenticate a user
     * NOTE: This is not designed to be some super secure system, it's just a basic attempt to prevent DOS attacks
     * from people who may have found host / port in github commits
     */
    private fun authenticateUser(username: String, userpass: String){
        AUTH_FAIL = this.configHandler.isUserAuthorized(username, userpass)
        if(AUTH_FAIL)
            logger_.warn("Dolius: AUTH_FAIL for $username | $userpass")
    }


    /**
     * Write non-json objects back to the client
     */
    private fun writeMessageToClient(message: String){
        val clientWriter = DataOutputStream(socket.getOutputStream())
        clientWriter.writeBytes(message)
        clientWriter.close()
    }

    /**
     * Let the user know authentication has failed
     */
    private fun handleAuthFailure(){
        writeMessageToClient(AUTH_STR)
    }

    /**
     * Takes the List of JSONObjects and send it back to the client
     */
    private fun returnToClient(jsonArray: JSONArray){
        writeMessageToClient(jsonArray.toString())
    }

    /**
     * Tells the client the server is busy and try again later
     */
    private fun handleBusy(){
        logger_.warn("Dolius: handleBusy")
        writeMessageToClient(BUSY_STR)
    }

    /**
     * Tells the client the server didn't want to perform work, for any reason other than being busy
     */
    private fun handleRejection(inpurString: String){
        logger_.warn("Dolius: handleRejection -- $inpurString")
        writeMessageToClient(REJT_STR)
    }

    /**
     * Sanitize the input string
     */
    private fun sanitize(inputLine: String){
        // Get the banned words from a config file in future
        val banned = listOf("drop", "create", "insert", "index", "rename", "pragma", "schema", "update", "dump")
        val lowerInput = inputLine.toLowerCase()
        for(word in banned)
            if(word in lowerInput) {
                SANITIZE_FAIL = true
                return
            }
    }

    /**
     * Close the sock and decrement the number of active threads
     */
    private fun destroy(){
        socket.close()
        currentThreads--
    }

}


fun main(args: Array<String>){
    var sock = ServerSocket(Finals.SERVER_SOCKET)
    while(true)
        Dolius(sock.accept()).run()
}

