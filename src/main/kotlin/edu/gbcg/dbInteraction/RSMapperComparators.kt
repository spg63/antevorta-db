/*
 * Copyright (c) 2018 Sean Grimes. All Rights Reserved.
 * License: MIT
 */

package edu.gbcg.dbInteraction

import edu.gbcg.dbInteraction.dbSelector.OrderBySelection
import edu.gbcg.dbInteraction.dbSelector.RSMapper
import edu.gbcg.utils.TSL

object RSMapperComparators: Comparator<RSMapper> {

    var columnsWithOrder = OrderBySelection()

    override fun compare(rs1: RSMapper, rs2: RSMapper): Int {
        // columns with order *must* be set before this function can work, it's the only way to know how to sort
        // the mappers, by date, name, or other. If this is null, it wasn't set, leave things unsorted by
        // telling the sortWith function using this comparator that all values are equal and log an error
        // message. The user should reset this variable to null when they're done the sorting.
        if(columnsWithOrder == null) {
            TSL.get().err("RSMapperComparators.columnsWithOrder was null, sorting was not completed!")
            return 0
        }

        // Return quickly if the column name supplied does not match with an existing column name in the RSMapper.
        // This returns 0, indicating equal values, and no sorting will happen
        if(badColumnName(rs1, columnsWithOrder!!.primaryColumn)) return 0

        // Now determine how to do the sort based on the number of columns and the ordering
        if(columnsWithOrder!!.numColumnsToSortBy == 1 && columnsWithOrder!!.primaryIsAscending){
            return compareAscending(rs1, rs2, columnsWithOrder!!.primaryColumn)
        }
        else if(columnsWithOrder!!.numColumnsToSortBy == 1 && !columnsWithOrder!!.primaryIsAscending){
            return compareDescending(rs1, rs2, columnsWithOrder!!.primaryColumn)
        }

        // I guess we somehow missed all the possible options, return 0 for no sort
        return 0
    }

    private fun compareAscending(rs1: RSMapper, rs2: RSMapper, columnName: String,
                                 isAscending: Boolean, numCols: Int = 1): Int {
        return when {
            rs1.getString(columnName) > rs2.getString(columnName) -> 1
            rs1.getString(columnName) < rs2.getString(columnName) -> -1
            // Values for the column are equal, recure through, using the secondary or tertiary column
            else -> {
                // Exit the recursion after we're done the columns, if this is hit the number of columns to use for
                // comparison have been exhausted and it's time to just declare them equal
                if(numCols == 1) return 0
                compareAscending(rs1, rs2, "balls", true, numCols - 1)
            }
        }
    }

    private fun compareDescending(rs1: RSMapper, rs2: RSMapper, columnName: String, numCols: Int = 1): Int {
        return when {
            rs1.getString(columnName) > rs2.getString(columnName) -> -1
            rs1.getString(columnName) == rs2.getString(columnName) -> 0
            else -> 1
        }
    }

    private fun compareTwoPrimaryAscending


    /*
        If the column doesn't exist, or it's not comparable, getString will return "". In this case return true, and
        the calling function will know to return a no-compare value, 0, and move on
     */
    private fun badColumnName(rs: RSMapper, columnName: String): Boolean {
        TSL.get().err("RSMapperComparators.badColumnName was true, sorting was not completed!")
        return rs.getString(columnName) == ""
    }
}
