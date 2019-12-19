package edu.antevortadb.configs

import javalibs.SysHelper
import javalibs.TSL
import org.json.JSONObject
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader
import java.lang.Exception
import java.net.Socket
import java.net.URL
import javax.swing.text.html.parser.TagElement

class Telemetry {
    /* ---------- Telemetry Keys -------------------------------------------------------*/
    companion object {
        const val OS_NAME   = "os.name"
        const val OS_VER    = "os.version"
        const val USER_NAME = "user.name"
        const val USER_HOME = "user.home"
        const val WORKING   = "user.dir"
        const val IP_ADDR   = "ip.addr"
        const val JAVA_VER  = "java.version"
        const val NUM_CORES = "availableProcessors"
        const val MAX_MEM   = "memory.max"
        const val AVAIL_MEM = "memory.available"
        const val FREE_MEM  = "memory.free"
        const val MAX_HDD   = "storage.total"
        const val FREE_HDD  = "storage.free"

        const val user      = "tele"
        const val port      = 3383
        const val host      = "corticus.us"
    }

    val sysHelper = SysHelper.get()

    fun push(){
        return
        // Gather the IP address. Want to get this first, if there's a timeout this
        // function just skips gathering telemetry since I don't want to try another
        // network connection at this point and waste more time
        val ipAddr = this.sysHelper.ipAddr
        if(ipAddr == this.sysHelper.BAD_NETWORK_ATEMPT)
            return

        val json = JSONObject()
        json.put("HOSTNAME", host)
        json.put("HOSTPORT", port)
        json.put("USER", "tele")
        json.put("PASS", "OPEN")

        // Put the system information into the object
        json.put(Telemetry.OS_NAME, this.sysHelper.osName())
        json.put(Telemetry.OS_VER, this.sysHelper.osVer())
        // TODO: Fix the core counting
        //json.put(Telemetry.NUM_CORES, this.sysHelper.availableProcs())
        json.put(Telemetry.USER_NAME, this.sysHelper.userName())
        json.put(Telemetry.USER_HOME, this.sysHelper.userHome())
        json.put(Telemetry.WORKING, this.sysHelper.userWorking())
        json.put(Telemetry.JAVA_VER, this.sysHelper.javaVer())
        json.put(Telemetry.IP_ADDR, ipAddr)
        json.put(Telemetry.MAX_MEM, this.sysHelper.maxMem())
        json.put(Telemetry.AVAIL_MEM, this.sysHelper.totalMem())
        json.put(Telemetry.FREE_MEM, this.sysHelper.freeMemory())
        json.put(Telemetry.MAX_HDD, this.sysHelper.rootTotalSpace())
        json.put(Telemetry.FREE_HDD, this.sysHelper.rootFreeSpace())

        try {
            val sock = Socket(host, port)
            val serverWriter = DataOutputStream(sock.getOutputStream())
            serverWriter.writeBytes(json.toString() + "\n")
            serverWriter.flush()
            serverWriter.close()
            sock.close()
        }
        catch(e: Exception){
            // Log it and move on, this is far from necessary to run the program
            TSL.get().exception(e)
        }
    }
}

