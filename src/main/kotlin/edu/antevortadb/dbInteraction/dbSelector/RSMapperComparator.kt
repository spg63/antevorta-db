/*
 * Copyright (c) 2018 Sean Grimes. All Rights Reserved.
 * License: MIT
 */

package edu.antevortadb.dbInteraction.dbSelector

import javalibs.TSL

object RSMapperComparator: Comparator<RSMapper> {

    var columnsWithOrder = OrderBySelection()

    override fun compare(rs1: RSMapper, rs2: RSMapper): Int {
        // columns with order *must* be set before this function can work, it's the only
        // way to know how to sort the mappers, by date, name, or other. If this is null,
        // it wasn't set, leave things unsorted by telling the sortWith function using
        // this comparator that all values are equal and log an error message. The user
        // should reset this variable to null when they're done the sorting.

        // Reset the column count with this, columnsWithOrder is a 'static' var
        columnsWithOrder.reset()
        if(!columnsWithOrder.hasNextColumn()) {
            TSL.get().err("RSMapperComparator.columnsWithOrder was empty, " +
                    "sorting was not completed!")
            return 0
        }
        // Do the actual comparison, recursively if more than 1 column is used for
        // tie-breaking
        return compareMappers(rs1, rs2)
    }

    private fun compareMappers(rs1: RSMapper, rs2: RSMapper): Int {
        // End the recursion, the columns to be used for comparison have been exhausted,
        // declare the objects equal
        if(!columnsWithOrder.hasNextColumn()) return 0

        // Get the column name, and if it should be ascending or descending for sort order
        val colpair = columnsWithOrder.nextColumn()
        val columnName = colpair.first
        val colOrder = colpair.second

        // Check that the column name is valid, perform no sort if it's invalid
        if(badColumnName(rs1, columnName)) return 0

        // Send it to ascending or descending based on orderby preferene
        return when(colOrder) {
            true -> compareAscending(rs1, rs2, columnName)
            false -> compareDescending(rs1, rs2, columnName)
        }
    }

    private fun compareAscending(rs1: RSMapper, rs2: RSMapper, columnName: String): Int {
        return when {
            rs1.getString(columnName) > rs2.getString(columnName) -> 1
            rs1.getString(columnName) < rs2.getString(columnName) -> -1
            // Values are equal, make the recursive call, compareMappers will
            // end the recursion when there's no more columns
            else -> compareMappers(rs1, rs2)
        }
    }

    private fun compareDescending(rs1: RSMapper, rs2: RSMapper, columnName: String): Int {
        // Just use compareAscending, reverse the return value or make the
        // recursive call if values are equal
        return when (compareAscending(rs1, rs2, columnName)) {
            1 -> -1
            -1 -> 1
            else -> compareMappers(rs1, rs2)
        }
    }

    /*
        If the column doesn't exist, or it's not comparable, getString will return "".
        In this case return true, and the calling function will know to return a
        no-compare value, 0, and move on
     */
    private fun badColumnName(rs: RSMapper, columnName: String): Boolean {
        if(rs.getString(columnName) == "") {
            TSL.get().err("RSMapperComparator.badColumnName was true, sorting " +
                    "was not completed!")
            return true
        }
        return false
    }
}
