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
import org.json.JSONObject
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStream
import java.io.InputStreamReader
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.Callable

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
    private val BUSY_STR = "Server was busy, please try again later."
    private val sleepTimeMS: Long = 2000
    private val DB_KEY = "dataBase"
    private val METHOD_KEY = "methodName"
    private val ARG_KEY = "argument_"

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
        val inputReader = BufferedReader(InputStreamReader(socket.getInputStream()))
        val input = inputReader.readLine()

        if(SERVER_BUSY) {
            handleBusy(input)
            destroy()
        }

        logger_.info("Dolius processing: $input")

        // Query the DB
        val mappers = process(input)

        // Turn the mappers into a list of JSON objects
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
    private fun process(inputLine: String): List<RSMapper> {
        // Convert the string from client to JSONObject
        val jsonObject = JSONObject(inputLine)

        // Get all key names
        val keys = JSONObject.getNames(jsonObject)

        // Get the DB name
        val dbName = jsonObject.getString(DB_KEY)

        // Get the methodName
        val methodName = jsonObject.getString(METHOD_KEY)

        // Get the arguments for the function call
        var args = ArrayList<String>()
        // If a key starts with ARG_KEY, select the associated value from the json object
        keys
                .filter { it.startsWith(ARG_KEY) }
                .mapTo(args) { jsonObject.getString(it) }

        // Get a Selector object
        val selector = Selector.getSelectorOnType(dbName)

        //----- NOTE: ONLY GENERAL SELECTION WORKING RIGHT NOW ---------------------------------------------------------

        // Make the selection, get the RSMappers
        return selector.generalSelection(args[0])
    }

    /**
     * Takes the List of JSONObjects and
     */
    private fun returnToClient(jsonArray: JSONArray){
        val clientWriter = DataOutputStream(socket.getOutputStream())
        clientWriter.writeBytes(jsonArray.toString())
        clientWriter.close()
    }

    private fun handleBusy(inputString: String){

    }

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

