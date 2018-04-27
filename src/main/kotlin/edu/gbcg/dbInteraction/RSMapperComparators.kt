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
        if(!columnsWithOrder.hasNextColumn()) {
            TSL.get().err("RSMapperComparators.columnsWithOrder was empty, sorting was not completed!")
            return 0
        }
        // Do the actual comparison, recursively if more than 1 column is used for tie-breaking
        return compareMappers(rs1, rs2)


        // I guess we somehow missed all the possible options, return 0 for no sort
        return 0
    }

    private fun compareMappers(rs1: RSMapper, rs2: RSMapper): Int {
        // End the recursion, the columns to be used for comparison have been exhausted, declare the objects equal
        if(!columnsWithOrder.hasNextColumn()) return 0

        // Get the column name, and if it should be ascending or descending for sort order
        val colpair = columnsWithOrder.nextColumn()
        val columnName = colpair.first
        val colOrder = colpair.second

        // Send it to ascending or descending based on orderby preferene
        return when(colOrder) {
            true -> compareAscending(rs1, rs2, columnName)
            false -> compareDescending(rs1, rs2, columnName)
        }
    }

    private fun compareAscending(rs1: RSMapper, rs2: RSMapper, columnName: String): Int {
        // Check that columnName is valid, perform no sort if it's invalid
        if(badColumnName(rs1, columnName)) return 0

        return when {
            rs1.getString(columnName) > rs2.getString(columnName) -> 1
            rs1.getString(columnName) < rs2.getString(columnName) -> -1
            // Values are equal, make the recursive call, compareMappers will end the recursion when there's no more
            // columns
            else -> compareMappers(rs1, rs2)
        }
    }

    private fun compareDescending(rs1: RSMapper, rs2: RSMapper, columnName: String): Int {
        // Check that columnName is valid, perform no sort if it's invalid
        if(badColumnName(rs1, columnName)) return 0

        return when {
            rs1.getString(columnName) > rs2.getString(columnName) -> -1
            rs1.getString(columnName) < rs2.getString(columnName) -> 1
            else -> compareMappers(rs1, rs2)
        }
    }

    /*
        If the column doesn't exist, or it's not comparable, getString will return "". In this case return true, and
        the calling function will know to return a no-compare value, 0, and move on
     */
    private fun badColumnName(rs: RSMapper, columnName: String): Boolean {
        if(rs.getString(columnName) == "") {
            TSL.get().err("RSMapperComparators.badColumnName was true, sorting was not completed!")
            return true
        }
        return false
    }
}
