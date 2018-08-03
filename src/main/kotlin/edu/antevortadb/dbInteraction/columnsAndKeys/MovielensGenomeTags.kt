/*
 * Copyright (c) 2018 Sean Grimes. All Rights Reserved.
 * License: MIT
 */

package edu.antevortadb.dbInteraction.columnsAndKeys

import edu.antevortadb.configs.Finals

object MovielensGenomeTags {
    fun csvKeys() = listOf(
            "tagId",
            "tag"
    )

    fun columnNames() = listOf(
            Finals.ID,
            "tagid",
            "tag"
    )

    // Not eliminating any columns when printing object from this table
    fun columnsForPrinting() = columnNames()

    fun dataTypes() = listOf(
            " INTEGER PRIMARY KEY AUTOINCREMENT,",
            " INTEGER,",
            " TEXT"
    )

    fun dataTypesForPrinting() = listOf(
            "INT",
            "INT",
            "TEXT"
    )
}
