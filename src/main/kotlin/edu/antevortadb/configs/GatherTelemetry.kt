package edu.antevortadb.configs

import javalibs.SysHelper
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
    val sysHelper = SysHelper.get()

    fun push(){
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
        json.put(Finals.OS_NAME, System.getProperty(Finals.OS_NAME))
        json.put(Finals.OS_VER, System.getProperty(Finals.OS_VER))
        json.put(Finals.NUM_CORES, Runtime.getRuntime().availableProcessors().toString())
        json.put(Finals.USER_NAME, System.getProperty(Finals.USER_NAME))
        json.put(Finals.USER_HOME, System.getProperty(Finals.USER_HOME))
        json.put(Finals.WORKING, System.getProperty(Finals.WORKING))
        json.put(Finals.JAVA_VER, System.getProperty(Finals.JAVA_VER))
        json.put(Finals.IP_ADDR, returnedIP)

        val sock = Socket(host, port)
        val serverWriter = DataOutputStream(sock.getOutputStream())
        serverWriter.writeBytes(json.toString() + "\n")
        serverWriter.flush()
    }
}

