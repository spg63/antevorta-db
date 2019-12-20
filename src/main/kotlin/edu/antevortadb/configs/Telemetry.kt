package edu.antevortadb.configs

import javalibs.SysHelper
import javalibs.TSL
import org.json.JSONObject
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.lang.Exception
import java.net.InetSocketAddress
import java.net.Socket
import java.net.URL
import javax.swing.text.html.parser.TagElement

class Telemetry {
    companion object {
        /* ---------- Telemetry Keys ---------------------------------------------------*/
        const val OS_FULL   = "os.fullInformation"
        const val OS_BUILD  = "os.buildNumber"
        const val OS_CODE   = "os.codeName"
        const val OS_MANU   = "os.manfacturer"
        const val OS_NAME   = "os.name"
        const val OS_VER    = "os.version"
        const val USER_NAME = "user.name"
        const val USER_HOME = "user.home"
        const val WORKING   = "user.dir"
        const val IP_ADDR   = "ip.addr"
        const val HOST_NAME = "user.hostName"
        const val JAVA_VER  = "java.version"

        const val HD_MANU   = "hd.manufacturer"
        const val HD_MODEL  = "hd.hardwareModel"
        const val HD_SERI   = "hd.hardwareSerial"
        const val UPTIME    = "hd.uptime"
        const val ON_AC     = "hd.AC"
        const val BAT_REM   = "hd.batteryTimeRemaining"
        const val BAT_CYCL  = "hd.batteryCycles"
        const val PHYS_CORE = "hd.physicalCores"
        const val LOGI_CORE = "hd.logicalCores"
        const val HT_ENABLE = "hd.hasHyperThreading"
        const val CPU_BASE  = "hd.cpuBaseFreq"
        const val CPU_TMP   = "hd.cpuTemp"
        const val MAX_MEM   = "hd.memoryMax"
        const val AVAIL_MEM = "hd.memoryAvailable"
        const val FREE_MEM  = "hd.memoryFree"
        const val MAX_HDD   = "hd.storageTotal"
        const val FREE_HDD  = "hd.storageFree"

        /* ---------- Socket Keys ------------------------------------------------------*/
        const val user      = "tele"
        const val port      = 3383
        const val host      = "corticus.us"
        const val timeout   = 2000
    }

    val sysHelper = SysHelper.get()

    fun push(){
        // Gather the IP address. Want to get this first, if there's a timeout this
        // function just skips gathering telemetry since I don't want to try another
        // network connection at this point and waste more time. Also check for
        // ripper's status and skip if ripper isn't reachable
        val ipAddr = this.sysHelper.externalIPAddr()
        if(this.sysHelper.BAD_NETWORK_ATEMPT == ipAddr || !isRipperAvailable())
            return

        val json = JSONObject()
        json.put("HOSTNAME", host)
        json.put("HOSTPORT", port)
        json.put("USER", "tele")
        json.put("PASS", "OPEN")

        // Gather the OS related information
        json.put(Telemetry.OS_FULL, this.sysHelper.osVersionFullInfo())
        json.put(Telemetry.OS_BUILD, this.sysHelper.osBuildNumber())
        json.put(Telemetry.OS_CODE, this.sysHelper.osCodeName())
        json.put(Telemetry.OS_MANU, this.sysHelper.osManufacturer())
        json.put(Telemetry.OS_NAME, this.sysHelper.osName())
        json.put(Telemetry.OS_VER, this.sysHelper.osVer())

        // Gather the User information
        json.put(Telemetry.USER_NAME, this.sysHelper.userName())
        json.put(Telemetry.USER_HOME, this.sysHelper.userHome())
        json.put(Telemetry.WORKING, this.sysHelper.userWorking())
        json.put(Telemetry.IP_ADDR, ipAddr)
        json.put(Telemetry.HOST_NAME, this.sysHelper.hostName())
        json.put(Telemetry.JAVA_VER, this.sysHelper.javaVer())

        // Gather and aggregate the hardware information
        json.put(Telemetry.HD_MANU, this.sysHelper.hardwareManufacturer())
        json.put(Telemetry.HD_MODEL, this.sysHelper.hardwareModel())
        json.put(Telemetry.HD_SERI, this.sysHelper.hardwareSerial())
        json.put(Telemetry.UPTIME, this.sysHelper.prettyUptime())
        json.put(Telemetry.ON_AC, this.sysHelper.runningOnAC())
        json.put(Telemetry.BAT_REM, this.sysHelper.batteryTimeRemainingSeconds())
        json.put(Telemetry.BAT_CYCL, this.sysHelper.batteryCycleCount())
        json.put(Telemetry.PHYS_CORE, this.sysHelper.physicalCPUCoreCount())
        json.put(Telemetry.LOGI_CORE, this.sysHelper.reportedCPUCoreCount())
        json.put(Telemetry.HT_ENABLE, this.sysHelper.hasHyperThreading())
        json.put(Telemetry.CPU_BASE, this.sysHelper.cpuMaxBaseFreq())
        json.put(Telemetry.CPU_TMP, this.sysHelper.cpuTemp())
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

    // Checks if a host is available on a specific port. If not return false
    fun isRipperAvailable(): Boolean {
        return try{
            val sock = Socket()
            sock.connect(InetSocketAddress(host, port), timeout)
            true
        }
        catch(isDown: IOException){
            false
        }
    }
}

