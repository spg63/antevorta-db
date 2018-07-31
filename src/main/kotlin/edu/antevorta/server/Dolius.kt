/*
 * Copyright (c) 2018 Sean Grimes. All Rights Reserved.
 * License: MIT
 */

package edu.antevorta.server

import edu.antevorta.configs.Finals
import edu.antevorta.dbInteraction.dbSelector.RSMapper
import edu.antevorta.dbInteraction.dbSelector.Selector
import edu.antevorta.utils.TSL
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.lang.reflect.InvocationTargetException
import java.net.ServerSocket
import java.net.Socket

/**
 * Note: This server is intentionally capped at 5 threads. Access to the DB (and data processing) is already
 * threaded as much as it reasonably should be. Any threading here will introduce additional latency
 * however the convenience to make multiple requests at the same time out-weighs the downsides. This also
 * allows for a minimal number of users access to the resources concurrently.
 */

// NOTE: These two vars are the kotlin version of static class vars, available to all instances of the class
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
        ++currentThreads
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

        // Read client JSON request. Should contain user, pass, query
        val inputReader: BufferedReader
        val input: String
        try{
            inputReader = BufferedReader(InputStreamReader(socket.getInputStream()))
            //logger.info("Incoming IP: ${socket.remoteSocketAddress.toString()}")
            //logger.info("Incoming Inet: ${socket.inetAddress.toString()}")
            input = inputReader.readLine()
        }
        catch(e: IOException){
            logger_.exception(e)
            handleRejection("Server failed to read client input")
            destroy()
            return
        }

        // Create the jsonObject from the client data
        val jsonObject = getJsonObject(input)

        if(jsonObject == null){
            handleRejection("Invalid JSON passed to server")
            destroy()
            return
        }

        // Get username and userpass from the json object, try authentication if user and pass exist
        val user: String
        val pass: String
        if(jsonObject.has(USER) && jsonObject.has(PASS)) {
            user = jsonObject.getString(USER)
            pass = jsonObject.getString(PASS)
            authenticateUser(user, pass)
        }
        else{
            logger_.err("jsonObject missing USER or PASS key")
            handleRejection("JSON from client missing authentication information")
            destroy()
            return
        }

        // If auth fails, let client know and quit
        if(AUTH_FAIL){
            handleAuthFailure()
            destroy()
            return
        }

        // Try getting the query
        val query: String
        if(jsonObject.has(QUERY))
            query = jsonObject.getString(QUERY)
        else{
            logger_.err("jsonObject missing QUERY key")
            handleRejection("JSON from client missing query information")
            destroy()
            return
        }

        // Basic string sanitization for query
        sanitize(query)

        // If sanitization fails, let client know and quit
        if(SANITIZE_FAIL){
            handleRejection(input)
            destroy()
            return
        }

        logger_.info("Dolius processing \"$query\" from $user")

        // Query the DB and get the RSMappers in return
        val mappers = process(query)

        // Turn the list of RSMappers into a JSONArray of JSONObjects
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
        val obj: JSONObject
        try {
            obj = JSONObject(jsonString)
        }
        catch(e: JSONException){
            logger_.err("Failed JSON Input: $jsonString")
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
        AUTH_FAIL = !this.configHandler.isUserAuthorized(username, userpass)
        if(AUTH_FAIL)
            logger_.warn("Dolius: AUTH_FAIL for $username | $userpass")
    }


    /**
     * Write non-json objects back to the client
     */
    private fun writeMessageToClient(message: String){
        try {
            val clientWriter = DataOutputStream(socket.getOutputStream())
            clientWriter.writeBytes(message)
            clientWriter.close()
        }
        catch(e: InvocationTargetException){
            logger_.exception(e)
        }
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
        --currentThreads
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
        val banned = this.configHandler.getBannedSQLWords()
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

var restartCount = 0

fun runServerInTryCatch(){
    val sock = ServerSocket(Finals.SERVER_SOCKET)
    while(true){
        try{
            Dolius(sock.accept()).run()
        }
        catch(e: Exception){
            ++restartCount
            TSL.get().err("Dolius was hit in the face by an uncaught exception. Dolius has now been restarted $restartCount times.")
            if(!sock.isClosed) sock.close()
            // Just re-call this function to restart the server if some un-caught exception is throw
            runServerInTryCatch()
        }
    }
}

fun main(args: Array<String>){
    // This function just runs the server in a while(true) continuous loop. If an exception is thrown the function is
    // simply re-called to restart the server. Not a long term solution but it'll do for now.
    runServerInTryCatch()
}
