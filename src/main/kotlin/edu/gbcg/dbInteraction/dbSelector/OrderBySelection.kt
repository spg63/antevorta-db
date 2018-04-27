/*
 * Copyright (c) 2018 Sean Grimes. All Rights Reserved.
 * License: MIT
 */

package edu.gbcg.dbInteraction.dbSelector

import edu.gbcg.utils.TSL

class OrderBySelection() {
    private val columnList = ArrayList<Pair<String, Boolean>>()
    private var currentColumn = -1

    /**
     * Add a column name and isAscending pair.
     * @param columnName The name of the column
     * @param isAscending True if sorting is done in ascending order, false for descending
     */
    fun addColumn(columnName: String, isAscending: Boolean) {
        this.columnList.add(Pair(columnName, isAscending))
    }

    fun reset() {
        currentColumn = -1
    }

    /**
     * Determine if there is another column to use for sorting
     * @return true if another column exists, false otherwise
     */
    fun hasNextColumn(): Boolean {
        return currentColumn < columnList.size - 1
    }

    /**
     * Get the column name and a boolean for ascending
     * @return A Pair<column name, isAscending>
     */
    fun nextColumn(): Pair<String, Boolean> {
        ++currentColumn
        if(currentColumn >= columnList.size)
            TSL.get().logAndKill("OrderBySelection.nextColumn exceeded bounds of columnList")
        return this.columnList[currentColumn]
    }
}
