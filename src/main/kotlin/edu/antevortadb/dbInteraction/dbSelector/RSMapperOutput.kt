/*
 * Copyright (c) 2018 Sean Grimes. All Rights Reserved.
 * License: MIT
 */

@file:Suppress("unused")

package edu.antevortadb.dbInteraction.dbSelector

import edu.antevortadb.configs.Finals
import edu.antevortadb.dbInteraction.TimeUtils
import edu.antevortadb.utils.FileUtils
import edu.antevortadb.utils.Out
import org.json.JSONObject
import java.util.*
import kotlin.text.StringBuilder

object RSMapperOutput{
    private val out = Out.get()

    fun printAllColumnsFromRSMappers(mappers: List<RSMapper>, columnNames: List<String>,
                                                dataTypes: List<String>) {
        if(mappers.isEmpty()){
            println("**----- NO RESULTS -----**")
            return
        }

        for(mapper in mappers){
            for(i in 0 until columnNames.size){
                var outmap = when {
                    dataTypes[i] == "BOOL" -> mapper.getBoolean(columnNames[i]).toString()
                    dataTypes[i] == "INT" -> mapper.getLong(columnNames[i]).toString()
                    dataTypes[i] == "JSON" -> JSONObject(mapper.getString(columnNames[i])).toString()
                    dataTypes[i] == "REAL" -> mapper.getDouble(columnNames[i]).toString()
                    else -> mapper.getString(columnNames[i])
                }
                if(Finals.CREATED_DT == columnNames[i] || Finals.SCRAPED_DT == columnNames[i])
                    outmap = TimeUtils.utcSecondsToZDT(outmap)
                out.writef("%-20s: %s\n", columnNames[i], outmap)
            }
            println("\n-----------------------------------------------------------------------------------\n")
        }
    }

    @Suppress("SENSELESS_COMPARISON")
    fun rsMappersToCSV(mappers: List<RSMapper>?, columnNames: List<String>, csvFilePath: String,
                       shuffleSeedOrNegOne: Long) {
        if(mappers == null || mappers.isEmpty()){
            println("**----- NO RESULTS -----**")
            return
        }

        val sb = StringBuilder()

        for(i in 0 until columnNames.size - 1){
            sb.append(columnNames[i])
            sb.append(",")
        }
        sb.append(columnNames[columnNames.size - 1])
        sb.append("\n")

        val printableMappers = mappers.toMutableList()

        if(shuffleSeedOrNegOne != -1L)
            printableMappers.shuffle(Random(shuffleSeedOrNegOne))

        for(mapper in printableMappers){
            for(i in 0 until columnNames.size - 1){
                var result = mapper.getString(columnNames[i])
                if(result != null) {
                    result = result.replace(",", "")
                    result = result.replace("\n", "")
                }
                sb.append(result)
                sb.append(",")
            }
            sb.append(mapper.getString(columnNames[columnNames.size - 1]).replace(',','\''))
            sb.append("\n")
        }
        FileUtils.get().writeNewFile(csvFilePath, sb.toString())
    }
}
