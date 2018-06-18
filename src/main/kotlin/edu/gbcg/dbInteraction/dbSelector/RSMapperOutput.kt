/*
 * Copyright (c) 2018 Sean Grimes. All Rights Reserved.
 * License: MIT
 */

@file:Suppress("unused")

package edu.gbcg.dbInteraction.dbSelector

import edu.gbcg.configs.Finals
import edu.gbcg.dbInteraction.TimeUtils
import edu.gbcg.utils.FileUtils
import edu.gbcg.utils.Out
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
                    else -> mapper.getString(columnNames[i])
                }
                if(Finals.CREATED_DT == columnNames[i] || Finals.SCRAPED_DT == columnNames[i])
                    outmap = TimeUtils.utcSecondsToZDT(outmap)
                out.writef("%-20s: %s\n", columnNames[i], outmap)
            }
            println("\n----------------------------------------------------------------------------------------------------\n")
        }
    }

    @Suppress("SENSELESS_COMPARISON")
    fun RSMappersToCSV(mappers: List<RSMapper>?, columnNames: List<String>, csvFilePath: String) {
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

        for(mapper in mappers){
            for(i in 0 until columnNames.size - 1){
                var result = mapper.getString(columnNames[i])
                if(result != null)
                    result = result.replace(',', '\'')
                sb.append(result)
                sb.append(",")
            }
            sb.append(mapper.getString(columnNames[columnNames.size - 1]).replace(',','\''))
            sb.append("\n")
        }
        FileUtils.get().writeNewFile(csvFilePath, sb.toString())
    }
}
