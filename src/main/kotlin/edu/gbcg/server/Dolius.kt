/*
 * Copyright (c) 2018 Sean Grimes. All Rights Reserved.
 * License: MIT
 */

package edu.gbcg.server

import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader
import java.net.ServerSocket
import java.util.concurrent.Callable

/**
 * Note: This server is intentionally capped at 5 threads. Access to the DB (and data processing) is already threaded
 * as much as it reasonably should be. Any threading here will introduce additional latency however the convenience to
 * make multiple requests at the same time out-weighs the downsides. This also allows for a minimal number of
 * users access to the resources concurrently.
 */

@JvmField var currentThreads = 0
@JvmField val MAX_THREADS = 5

class Dolius: Callable<Any> {

    init{
        currentThreads++

    }

    fun destroy(){
        currentThreads--
    }

    override fun call(): Int{

        currentThreads--
        return 5
    }

}


fun main(args: Array<String>){

    /*
    var clientSentence: String
    var capSentence: String

    var sock = ServerSocket(3383)
    while(true){
        var connectionSock = sock.accept()
        var inClient = BufferedReader(InputStreamReader(connectionSock.getInputStream()))
        var outClient = DataOutputStream(connectionSock.getOutputStream())
        clientSentence = inClient.readLine()
        println("rec: $clientSentence")
        capSentence = clientSentence.toUpperCase()
        println("cap: $capSentence")
        outClient.writeBytes(capSentence)
        println("wrote back")
        outClient.close() // Yes needed
        connectionSock.close() // Needed?
    }
    */
}

