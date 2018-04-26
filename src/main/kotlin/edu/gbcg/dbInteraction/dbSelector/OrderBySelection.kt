/*
 * Copyright (c) 2018 Sean Grimes. All Rights Reserved.
 * License: MIT
 */

package edu.gbcg.dbInteraction.dbSelector

class OrderBySelection{
    var primaryColumn = String()
    var secondaryColumn = String()
    var tertiaryColumn = String()
    var numColumnsToSortBy = 0
    var primaryIsAscending = true
    var secondaryIsAscending = true
    var tertiaryIsAscending = true

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

}
