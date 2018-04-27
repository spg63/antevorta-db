/*
 * Copyright (c) 2018 Sean Grimes. All Rights Reserved.
 * License: MIT
 */

package edu.gbcg.dbInteraction.dbSelector

import edu.gbcg.configs.Finals

class OrderBySelection{
    var primaryColumn = String()
    var secondaryColumn = String()
    var tertiaryColumn = String()
    var numColumnsToSortBy = 0
    var primaryIsAscending = true
    var secondaryIsAscending = true
    var tertiaryIsAscending = true
    var currentColumn = 0

    // Default to date/time and ascending order, though I can't see why this would actually be used
    constructor(){
        this.primaryColumn = Finals.CREATED_DT
        this.primaryIsAscending = true
    }

    constructor(primaryColumn: String, primaryIsAscending: Boolean){
        this.primaryColumn = primaryColumn
        this.primaryIsAscending = primaryIsAscending
        this.numColumnsToSortBy = 1
    }

    constructor(primaryColumn: String, primaryIsAscending: Boolean,
                secondaryColumn: String, secondaryIsAscending: Boolean){
        this.primaryColumn = primaryColumn
        this.primaryIsAscending = primaryIsAscending
        this.secondaryColumn = secondaryColumn
        this.secondaryIsAscending = secondaryIsAscending
        this.numColumnsToSortBy = 2
    }

    constructor(primaryColumn: String, primaryIsAscending: Boolean,
                secondaryColumn: String, secondaryIsAscending: Boolean,
                tertiaryColumn: String, tertiaryIsAscending: Boolean){
        this.primaryColumn = primaryColumn
        this.primaryIsAscending = primaryIsAscending
        this.secondaryColumn = secondaryColumn
        this.secondaryIsAscending = secondaryIsAscending
        this.tertiaryColumn = tertiaryColumn
        this.tertiaryIsAscending = tertiaryIsAscending
        this.numColumnsToSortBy = 3
    }

    fun getColumnToCompareWith(): String {
        ++this.currentColumn
        return when (currentColumn) {
            1 -> this.primaryColumn
            2 -> this.secondaryColumn
            else -> this.tertiaryColumn
        }
    }

    fun getColumnOrderToCompareWith(): Boolean {
        return when (currentColumn) {
            1 -> this.primaryIsAscending
            2 -> this.secondaryIsAscending
            else -> this.tertiaryIsAscending
        }
    }

}

/*
Need this to be an 'iterator' type interface, returning the next column. Then the comparator can be done recursively
in a nice manner, handle however many orderby columns it needs to, and will stop based on a sentinel, return
something like "NOMORECOLUMNS" as a string that the comparator can check for and then stop the recursion
 */