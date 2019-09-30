package edu.antevortadb.configs

import org.json.JSONObject
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader
import java.net.Socket
import java.net.URL

class GatherTelemetry {
    val user = "tele"
    val port = 3383
    val host = "corticus.us"

    fun push(){
        val osName = System.getProperty(Finals.OS_NAME)
        val osVersion = System.getProperty(Finals.OS_VER)
        val userName = System.getProperty(Finals.USER_NAME)
        val homeDir = System.getProperty(Finals.USER_HOME)
        val working = System.getProperty(Finals.WORKING)
        val javaVer = System.getProperty(Finals.JAVA_VER)

        val ip = URL("http://checkip.amazonaws.com")
        val ipIn = BufferedReader(InputStreamReader(ip.openStream()))
        val returnedIP = ipIn.readLine()
        ipIn.close()

        val json = JSONObject()
        json.put("HOSTNAME", host)
        json.put("HOSTPORT", port)
        json.put("USER", "tele")
        json.put("PASS", "OPEN")

        // Put the system information into the object
        json.put(Finals.OS_NAME, osName)
        json.put(Finals.OS_VER, osVersion)
        json.put(Finals.USER_NAME, userName)
        json.put(Finals.USER_HOME, homeDir)
        json.put(Finals.WORKING, working)
        json.put(Finals.JAVA_VER, javaVer)
        json.put(Finals.IP_ADDR, returnedIP)

        val sock = Socket(host, port)
        val serverWriter = DataOutputStream(sock.getOutputStream())
        serverWriter.writeBytes(json.toString() + "\n")
        serverWriter.flush()
    }
}