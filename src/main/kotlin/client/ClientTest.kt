/*
 * Copyright (c) 2018 Sean Grimes. All Rights Reserved.
 * License: MIT
 */

package client

import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader
import java.net.Socket

fun main(args: Array<String>){
    var sentence: String
    var modSentence: String
    var br = BufferedReader(InputStreamReader(System.`in`))
    var sock = Socket("localhost", 3383)
    var out = DataOutputStream(sock.getOutputStream())
    var fromServ = BufferedReader(InputStreamReader(sock.getInputStream()))

    sentence = br.readLine()
    println("sentence: $sentence")
    println("writing")
    out.writeBytes(sentence + "\n")
    println("written")
    modSentence = fromServ.readLine()
    println("read back")
    println(modSentence)
    sock.close()
}