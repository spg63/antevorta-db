/*
 * Copyright (c) 2018 Sean Grimes. All Rights Reserved.
 * License: MIT
 */

package edu.gbcg.server

import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader
import java.net.ServerSocket

fun main(args: Array<String>){
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
        outClient.close()
    }
}