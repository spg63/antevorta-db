/*
 * Copyright (c) 2018 Sean Grimes. All Rights Reserved.
 * License: MIT
 */

package edu.antevortadb.server

import edu.antevortadb.configs.DataPaths
import edu.antevortadb.configs.Finals
import edu.antevortadb.configs.Telemetry
import edu.antevortadb.dbInteraction.dbSelector.RSMapper
import edu.antevortadb.dbInteraction.dbSelector.Selector
import javalibs.FileUtils
import javalibs.TSL
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.*
import java.lang.StringBuilder
import java.lang.reflect.InvocationTargetException
import java.net.ServerSocket
import java.net.Socket
import java.nio.charset.StandardCharsets
import java.time.LocalDateTime

/**
 * Note: This server is intentionally capped at 5 threads. Access to the DB (and data
 * processing) is already threaded as much as it reasonably should be. Any threading here
 * will introduce additional latency however the convenience to make multiple requests at
 * the same time out-weighs the downsides. This also allows for a minimal number of
 * users access to the resources concurrently.
 */

// NOTE: These two vars are the kotlin version of static class vars, available to all
// instances of the class
var currentThreads = 0
const val MAX_THREADS = 5

@Suppress("PrivatePropertyName")
class Dolius(private val socket: Socket): Runnable {
    private var sleepCounter = 0
    private val maxSleep = 5
    private val logger = TSL.get()
    private var serverBusy = false
    private var sanitizeFail = false
    private var authFail = false
    private val noopFlag = "=*="
    private val busyStr =
        "${noopFlag}Server was busy, unable to process request. Please try again later."
    private val rejectionStr = "${noopFlag}Server will not perform that kind of work."
    private val authStr =
            "${noopFlag}Server could not authenticate user, please check credentials."
    private val sleepTimeMS: Long = 2000
    private val USER = "USER"
    private val PASS = "PASS"
    private val QUERY = "QUERY"
    private val configHandler = ServerConfigHandler()

    // Log separately connections so I can easily track them
    init{
        while(currentThreads >= MAX_THREADS) {
            logger.dolius("Dolius: Sleeping for $sleepTimeMS")
            Thread.sleep(sleepTimeMS)
            sleepCounter++
            if(sleepCounter >= maxSleep) {
                logger.dolius("Dolius: Reached maxSleep")
                serverBusy = true
                break
            }
            logger.dolius("Dolius: currentThreads >= MAX_THREADS")
        }
        ++currentThreads
    }

    /**
     * Read the data from the socket, process the data to determine function call, call
     * the function, parse the RSMapper into a list of JSON objects, push the JSON
     * objects back to the client and close the connection
     */
    override fun run() {
        // Get the data from the socket
        // If the server has reached max threads and max sleep, tell client the server is
        // busy and quit
        if(serverBusy) {
            handleBusy()
            destroy()
            return
        }

        // Read client JSON request. Should contain user, pass, query
        val inputReader: BufferedReader
        val input: String
        try{
            inputReader = BufferedReader(InputStreamReader(socket.getInputStream()))
            input = inputReader.readLine()
        }
        catch(e: Exception){
            logger.exception(e)
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

        // Get username and userpass from the json object, try authentication if user and
        // pass exist
        val user: String
        val pass: String
        if(jsonObject.has(USER) && jsonObject.has(PASS)) {
            user = jsonObject.getString(USER)
            pass = jsonObject.getString(PASS)
            // Short circuit here to handle the telemetry collection. The
            // processTelemetry function returns without doing anything else
            if(user == "tele") processTelemetry(jsonObject)
            authenticateUser(user, pass)
        }
        else{
            logger.dolius("jsonObject missing USER or PASS key")
            handleRejection("JSON from client missing authentication information")
            destroy()
            return
        }

        // If auth fails, let client know and quit
        if(authFail){
            handleAuthFailure()
            destroy()
            return
        }

        // Try getting the query
        val query: String
        if(jsonObject.has(QUERY))
            query = jsonObject.getString(QUERY)
        else{
            logger.dolius("jsonObject missing QUERY key")
            handleRejection("JSON from client missing query information")
            destroy()
            return
        }

        // Basic string sanitization for query
        sanitize(query)

        // If sanitization fails, let client know and quit
        if(sanitizeFail){
            handleRejection(input)
            destroy()
            return
        }

        logger.dolius("Dolius processing \"$query\" from $user")

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



// ---------------------------------------------------------------------------------------


    /**
     * Process the telemetry data from the remote machine running this code
     */
    private fun processTelemetry(jObj: JSONObject){
        // Where we store the information
        val storeDir = DataPaths.DOLIUS_CONFIG_PATH
        val storePath = storeDir + "telemetry.invasive"

        // Make sure it exists
        FileUtils.get().checkAndCreateDir(storeDir)

        // Get the information
        // OS related information
        val osFull =
                if(jObj.has(Telemetry.OS_FULL)) jObj.get(Telemetry.OS_FULL)
                else "null"
        val osBuild =
                if(jObj.has(Telemetry.OS_BUILD)) jObj.get(Telemetry.OS_BUILD)
                else "null"
        val osCode =
                if(jObj.has(Telemetry.OS_CODE)) jObj.get(Telemetry.OS_CODE)
                else "null"
        val osManufacturer =
                if(jObj.has(Telemetry.OS_MANU)) jObj.get(Telemetry.OS_MANU)
                else "null"
        val osName =
                if(jObj.has(Telemetry.OS_NAME)) jObj.get(Telemetry.OS_NAME)
                else "null"
        val osVer =
                if(jObj.has(Telemetry.OS_VER)) jObj.get(Telemetry.OS_VER)
                else "null"

        // Gather user information
        val userName =
                if(jObj.has(Telemetry.USER_NAME)) jObj.get(Telemetry.USER_NAME)
                else "null"
        val userHome =
                if(jObj.has(Telemetry.USER_HOME)) jObj.get(Telemetry.USER_HOME)
                else "null"
        val workingDir =
                if(jObj.has(Telemetry.WORKING)) jObj.get(Telemetry.WORKING)
                else "null"
        val ipAddr =
                if(jObj.has(Telemetry.IP_ADDR)) jObj.get(Telemetry.IP_ADDR)
                else "null"
        val hostName =
                if(jObj.has(Telemetry.HOST_NAME)) jObj.get(Telemetry.HOST_NAME)
                else "null"
        val jVer =
                if(jObj.has(Telemetry.JAVA_VER)) jObj.get(Telemetry.JAVA_VER)
                else "null"

        // Gather hardware information
        val hdManufacturer =
                if(jObj.has(Telemetry.HD_MANU)) jObj.get(Telemetry.HD_MANU)
                else "null"
        val hdModel =
                if(jObj.has(Telemetry.HD_MODEL)) jObj.get(Telemetry.HD_MODEL)
                else "null"
        val hdSerial =
                if(jObj.has(Telemetry.HD_SERI)) jObj.get(Telemetry.HD_SERI)
                else "null"
        val uptime =
                if(jObj.has(Telemetry.UPTIME)) jObj.get(Telemetry.UPTIME)
                else "null"
        val onAC =
                if(jObj.has(Telemetry.ON_AC)) jObj.get(Telemetry.ON_AC)
                else "null"
        val batteryRemaining =
                if(jObj.has(Telemetry.BAT_REM)) jObj.get(Telemetry.BAT_REM)
                else "null"
        val batteryCycles =
                if(jObj.has(Telemetry.BAT_CYCL)) jObj.get(Telemetry.BAT_CYCL)
                else "null"
        val physicalCores =
                if(jObj.has(Telemetry.PHYS_CORE)) jObj.get(Telemetry.PHYS_CORE)
                else "null"
        val logicalCores =
                if(jObj.has(Telemetry.LOGI_CORE)) jObj.get(Telemetry.LOGI_CORE)
                else "null"
        val hyperThreadingEnabled =
                if(jObj.has(Telemetry.HT_ENABLE)) jObj.get(Telemetry.HT_ENABLE)
                else "null"
        val cpuBaseFreq =
                if(jObj.has(Telemetry.CPU_BASE)) jObj.get(Telemetry.CPU_BASE)
                else "null"
        val cpuTemperature =
                if(jObj.has(Telemetry.CPU_TMP)) jObj.get(Telemetry.CPU_TMP)
                else "null"
        val maxMem =
                if(jObj.has(Telemetry.MAX_MEM)) jObj.get(Telemetry.MAX_MEM)
                else "null"
        val availMem =
                if(jObj.has(Telemetry.AVAIL_MEM)) jObj.get(Telemetry.AVAIL_MEM)
                else "null"
        val freeMem =
                if(jObj.has(Telemetry.FREE_MEM)) jObj.get(Telemetry.FREE_MEM)
                else "null"
        val totalHDD =
                if(jObj.has(Telemetry.MAX_HDD)) jObj.get(Telemetry.MAX_HDD)
                else "null"
        val freeHDD =
                if(jObj.has(Telemetry.FREE_HDD)) jObj.get(Telemetry.FREE_HDD)
                else "null"

        val sb = StringBuilder()
        val ldt = LocalDateTime.now()
        val dtString = ldt.toString().replace("T", "_").replace(":", "_")
        sb.append("DT: $dtString").append("\n")
        sb.append("OS Information:").append("\n")
        sb.append("${Telemetry.OS_FULL}: $osFull").append("\n")
        sb.append("${Telemetry.OS_BUILD}: $osBuild").append("\n")
        sb.append("${Telemetry.OS_CODE}: $osCode").append("\n")
        sb.append("${Telemetry.OS_MANU}: $osManufacturer").append("\n")
        sb.append("${Telemetry.OS_NAME}: $osName").append("\n")
        sb.append("${Telemetry.OS_VER}: $osVer").append("\n")

        sb.append("User Information:").append("\n")
        sb.append("${Telemetry.USER_NAME}: $userName").append("\n")
        sb.append("${Telemetry.USER_HOME}: $userHome").append("\n")
        sb.append("${Telemetry.WORKING}: $workingDir").append("\n")
        sb.append("${Telemetry.IP_ADDR}: $ipAddr").append("\n")
        sb.append("${Telemetry.HOST_NAME}: $hostName").append("\n")
        sb.append("${Telemetry.JAVA_VER}: $jVer").append("\n")

        sb.append("Hardware Information:").append("\n")
        sb.append("${Telemetry.HD_MANU}: $hdManufacturer").append("\n")
        sb.append("${Telemetry.HD_MODEL}: $hdModel").append("\n")
        sb.append("${Telemetry.HD_SERI}: $hdSerial").append("\n")
        sb.append("${Telemetry.UPTIME}: $uptime").append("\n")
        sb.append("${Telemetry.ON_AC}: $onAC").append("\n")
        sb.append("${Telemetry.BAT_REM}: $batteryRemaining").append("\n")
        sb.append("${Telemetry.BAT_CYCL}: $batteryCycles").append("\n")
        sb.append("${Telemetry.PHYS_CORE}: $physicalCores").append("\n")
        sb.append("${Telemetry.LOGI_CORE}: $logicalCores").append("\n")
        sb.append("${Telemetry.HT_ENABLE}: $hyperThreadingEnabled").append("\n")
        sb.append("${Telemetry.CPU_BASE}: $cpuBaseFreq").append("\n")
        sb.append("${Telemetry.CPU_TMP}: $cpuTemperature").append("\n")
        sb.append("${Telemetry.MAX_MEM}: $maxMem").append("\n")
        sb.append("${Telemetry.AVAIL_MEM}: $availMem").append("\n")
        sb.append("${Telemetry.FREE_MEM}: $freeMem").append("\n")
        sb.append("${Telemetry.MAX_HDD}: $totalHDD").append("\n")
        sb.append("${Telemetry.FREE_HDD}: $freeHDD").append("\n")
        sb.append("----------").append("\n")

        val storeString = sb.toString()

        // Append it to the file
        FileUtils.get().appendToFile(storePath, storeString)
    }

    /**
     * Determines which function to call and queries the DB after some basic validation
     */
    private fun process(sqlQuery: String): List<RSMapper> {
        // Get a Selector object. Will auto detect which DB based on the table name.
        // This is not ideal.
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
            logger.dolius("Failed JSON Input: $jsonString")
            logger.exception(e)
            return null
        }
        return obj
    }

    /**
     * Authenticate a user
     * NOTE: This is not designed to be some super secure system, it's just a basic
     * attempt to prevent DOS attacks from people who may have found host / port in
     * github commits
     */
    private fun authenticateUser(username: String, userpass: String){
        // If this is just for telemetry reporting get out of here
        if(username == "tele") {
            authFail = true
            return
        }
        authFail = !this.configHandler.isUserAuthorized(username, userpass)
        if(authFail)
            logger.dolius("Dolius: authFail for $username | $userpass")
    }


    /**
     * Write non-json objects back to the client
     */
    private fun writeMessageToClient(message: String){
        try {
            val clientWriter = OutputStreamWriter(socket.getOutputStream(),
                    StandardCharsets.UTF_8)
            clientWriter.write(message)
            clientWriter.close()
        }
        catch(e: InvocationTargetException){
            logger.exception(e)
        }
    }

    /**
     * Let the user know authentication has failed
     */
    private fun handleAuthFailure(){
        writeMessageToClient(authStr)
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
        logger.dolius("Dolius: handleBusy")
        writeMessageToClient(busyStr)
    }

    /**
     * Tells the client the server didn't want to perform work, for any reason other
     * than being busy
     */
    private fun handleRejection(inpurString: String){
        logger.dolius("Dolius: handleRejection -- $inpurString")
        writeMessageToClient(rejectionStr)
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
                sanitizeFail = true
                return
            }
    }

    /**
     * Close the sock and decrement the number of active threads
     */
    private fun destroy(){
        socket.close()
        --currentThreads
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
            // Simple variable to track how many times we've died
            ++restartCount
            // The server crashed but the currentThreads variable wasn't decremented,
            // decrement it now
            --currentThreads
            TSL.get().err("Dolius was hit in the face by an uncaught exception. " +
                    "Dolius has now been restarted $restartCount times.")
            e.printStackTrace()
            TSL.get().exception(e)
            if(!sock.isClosed) sock.close()
            // Just re-call this function to restart the server if some un-caught
            // exception is throw
            runServerInTryCatch()
        }
    }
}

fun main(args: Array<String>){
    // This function just runs the server in a while(true) continuous loop. If an
    // exception is thrown the function is simply re-called to restart the server. Not
    // a long term solution but it'll do for now.
    runServerInTryCatch()
}
