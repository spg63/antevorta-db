package edu.antevortadb.configs

import javalibs.NetworkUtils
import javalibs.SysHelper
import javalibs.TSL
import org.json.JSONObject

class Telemetry {
    val sysHelper: SysHelper
    val networkUtils: NetworkUtils
    val ipAddr: String


    init {
        this.sysHelper = SysHelper.get()
        this.networkUtils = NetworkUtils.get()
        this.ipAddr = this.networkUtils.externalIPAddr()
    }

    companion object {
        /* ---------- Telemetry Keys ---------------------------------------------------*/
        // OS Keys
        const val OS_FULL   = "os.fullInformation"
        const val OS_BUILD  = "os.buildNumber"
        const val OS_CODE   = "os.codeName"
        const val OS_MANU   = "os.manfacturer"
        const val OS_NAME   = "os.name"
        const val OS_VER    = "os.version"

        // User Keys
        const val USER_NAME = "user.name"
        const val USER_HOME = "user.home"
        const val WORKING   = "user.currentWorkingDir"
        const val IP_ADDR   = "ip.addr"
        const val HOST_NAME = "user.hostName"
        const val JAVA_VER  = "java.version"
        const val JAVA_VEN  = "java.vendor"

        // Hardware Keys
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
        const val MAX_MEM   = "hd.JVM_memoryMax"
        const val AVAIL_MEM = "hd.JVM_memoryAvailable"
        const val FREE_MEM  = "hd.JVM_memoryFree"
        const val MAX_HDD   = "hd.storageTotal"
        const val FREE_HDD  = "hd.storageFree"

        /* ---------- Socket Keys ------------------------------------------------------*/
        const val user      = "tele"
        const val port      = Finals.SERVER_SOCKET_PORT
        const val host      = Finals.SERVER_SOCKET_HOST
        const val timeout   = 2000
    }

    /**
     * Gather the telemetry data and push it to ripper for collection
     */
    fun push() {
        // Gather the IP address. Want to get this first, if there's a timeout this
        // function just skips gathering telemetry since I don't want to try another
        // network connection at this point and waste more time. Also check for
        // ripper's status and skip if ripper isn't reachable
        if(this.networkUtils.BAD_NETWORK_ATEMPT == this.ipAddr || !isRipperAvailable()) {
            TSL.get().warn("Telemetry push aborted due to network problems")
            return
        }

        val json = buildTelemetryObject()
        TSL.get().trace("Telemetry object built successfully")

        // Push the json object to ripper, no response necessary
        networkUtils.writeWithoutResponse(host, port, json.toString())
        TSL.get().trace("Pushed telemetry object to $host")
    }

    /**
     * Get all of the telemetry data for the system this code runs on
     * @return The telemetry JSON object as a String
     */
    fun getTelemetryString(): String {
        val jsonPrintSpacing = 4
        return buildTelemetryObject().toString(jsonPrintSpacing)
    }

    // Build the full JSON object that contains the telemetry data
    private fun buildTelemetryObject(): JSONObject {

        val json = JSONObject()
        json.put("HOSTNAME", host)
        json.put("HOSTPORT", port)
        json.put("USER", "tele")
        json.put("PASS", "OPEN")

        // Gather the OS related information
        json.put(Telemetry.OS_FULL,     this.sysHelper.osVersionFullInfo())
        json.put(Telemetry.OS_BUILD,    this.sysHelper.osBuildNumber())
        json.put(Telemetry.OS_CODE,     this.sysHelper.osCodeName())
        json.put(Telemetry.OS_MANU,     this.sysHelper.osManufacturer())
        json.put(Telemetry.OS_NAME,     this.sysHelper.osName())
        json.put(Telemetry.OS_VER,      this.sysHelper.osVer())

        // Gather the User information
        json.put(Telemetry.USER_NAME,   this.sysHelper.userName())
        json.put(Telemetry.USER_HOME,   this.sysHelper.userHome())
        json.put(Telemetry.WORKING,     this.sysHelper.userWorking())
        json.put(Telemetry.IP_ADDR,     this.ipAddr)
        json.put(Telemetry.HOST_NAME,   this.sysHelper.hostName())
        json.put(Telemetry.JAVA_VER,    this.sysHelper.javaVer())
        json.put(Telemetry.JAVA_VEN,    this.sysHelper.JVMVendor())

        // Gather and aggregate the hardware information
        json.put(Telemetry.HD_MANU,     this.sysHelper.hardwareManufacturer())
        json.put(Telemetry.HD_MODEL,    this.sysHelper.hardwareModel())
        json.put(Telemetry.HD_SERI,     this.sysHelper.hardwareSerial())
        json.put(Telemetry.UPTIME,      this.sysHelper.prettyUptime())
        json.put(Telemetry.ON_AC,       this.sysHelper.runningOnAC())
        // Double.MAX_VALUE can be ambigious on the receiving end
        val batTimeRemaining = this.sysHelper.batteryTimeRemainingSeconds()
        val batTime =   if(batTimeRemaining.compareTo(Double.MAX_VALUE) == 0)
            "Unlimited -- Running on AC (probably)"
        else
            batTimeRemaining.toString()
        json.put(Telemetry.BAT_REM,     batTime)
        json.put(Telemetry.BAT_CYCL,    this.sysHelper.batteryCycleCount())
        json.put(Telemetry.PHYS_CORE,   this.sysHelper.physicalCPUCoreCount())
        json.put(Telemetry.LOGI_CORE,   this.sysHelper.reportedCPUCoreCount())
        json.put(Telemetry.HT_ENABLE,   this.sysHelper.hasHyperThreading())
        json.put(Telemetry.CPU_BASE,    this.sysHelper.cpuMaxBaseFreq())
        json.put(Telemetry.CPU_TMP,     this.sysHelper.cpuTemp())
        json.put(Telemetry.MAX_MEM,     this.sysHelper.maxMem())
        json.put(Telemetry.AVAIL_MEM,   this.sysHelper.totalMem())
        json.put(Telemetry.FREE_MEM,    this.sysHelper.freeMemory())
        json.put(Telemetry.MAX_HDD,     this.sysHelper.rootTotalSpace())
        json.put(Telemetry.FREE_HDD,    this.sysHelper.rootFreeSpace())

        return json
    }

    // Checks if a host is available on a specific port. If not return false
    private fun isRipperAvailable(): Boolean = networkUtils.pingHost(host, port, timeout)

}

