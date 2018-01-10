/*
 * Copyright (c) 2018 Sean Grimes. All Rights Reserved.
 * License: MIT
 */

package edu.gbcg.dbInteraction

import edu.gbcg.configs.Finals
import edu.gbcg.dbInteraction.dbSelector.RSMapper
import edu.gbcg.utils.FileUtils
import edu.gbcg.utils.Out
import kotlin.text.StringBuilder

object RSMapperOutput{
    private val out = Out.get()

    @JvmStatic fun printAllColumnsFromRSMappers(mappers: List<RSMapper>?, columnNames: List<String>) {
        if(mappers == null){
            println("**----- NO RESULTS -----**")
            return
        }

        for(mapper in mappers){
            for(col in columnNames){
                var outmap = mapper.getString(col)
                if(Finals.CREATED_DT.equals(col))
                    outmap = TimeUtils.utcSecondsToZDT(outmap)
                out.writef("%-20s: %s\n", col, outmap)
            }
            println("\n----------------------------------------------------------------------------------------------------\n")
        }
    }

    @JvmStatic fun RSMappersToCSV(mappers: List<RSMapper>?, columnNames: List<String>, csvFilePath: String) {
        if(mappers == null){
            println("**----- NO RESULTS -----**")
            return
        }

        var sb = StringBuilder()

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
