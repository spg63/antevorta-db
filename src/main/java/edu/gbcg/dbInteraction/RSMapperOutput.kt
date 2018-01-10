/*
 * Copyright (c) 2018 Sean Grimes. All Rights Reserved.
 * License: MIT
 */

package edu.gbcg.dbInteraction

import edu.gbcg.configs.Finals
import edu.gbcg.dbInteraction.dbSelector.RSMapper
import edu.gbcg.utils.FileUtils
import edu.gbcg.utils.Out

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

        var output = ""

        for(i in 0 until columnNames.size - 1){
            output += columnNames[i]
            output += ","
        }
        output += columnNames[columnNames.size - 1]
        output += "\n"

        for(mapper in mappers){
            for(i in 0 until columnNames.size - 1){
                var result = mapper.getString(columnNames[i])
                if(result != null)
                    result = result.replace(',', '\'')
                output += result
                output += ","
            }
            output += mapper.getString(columnNames[columnNames.size - 1]).replace(',','\'')
            output += "\n"
        }
        FileUtils.get().writeNewFile(csvFilePath, output)
    }
}
