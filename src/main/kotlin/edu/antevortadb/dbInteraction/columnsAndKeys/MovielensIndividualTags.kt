/*
 * Copyright (c) 2018 Sean Grimes. All Rights Reserved.
 * License: MIT
 */

package edu.antevortadb.dbInteraction.columnsAndKeys

import edu.antevortadb.configs.Finals

object MovielensIndividualTags {
    fun csvKeys() = listOf(
            "userId",
            "movieId",
            "tag",
            "timestamp"
    )

    fun columnNames() = listOf(
            Finals.ID,
            Finals.TMDB_ID,
            Finals.IMDB_ID,
            Finals.ML_ID,
            Finals.USER_ID,
            "tagid",
            "tag",
            Finals.CREATED_DT
    )

    fun columnsForPrinting() = columnNames()

    fun dataTypes() = listOf(
            " INTEGER PRIMARY KEY AUTOINCREMENT,",
            " INTEGER,",
            " INTEGER,",
            " INTEGER,",
            " INTEGER,",
            " INTEGER,",
            " TEXT,",
            " INTEGER"
    )

    fun dataTypesForPrinting() = listOf(
            "INT",
            "INT",
            "INT",
            "INT",
            "INT",
            "INT",
            "TEXT",
            "INT"
    )
}
