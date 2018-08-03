/*
 * Copyright (c) 2018 Sean Grimes. All Rights Reserved.
 * License: MIT
 */

@file:Suppress("unused")

package edu.antevortadb.dbInteraction.columnsAndKeys

import edu.antevortadb.configs.Finals

object MovielensIndividualRatings {
    fun csvKeys() = listOf(
            "userId",
            "movieId",
            "rating",
            "timestamp"
    )

    fun columnNames() = listOf(
            Finals.ID,
            Finals.TMDB_ID,
            Finals.IMDB_ID,
            Finals.ML_ID,
            Finals.USER_ID,
            "rating",
            Finals.CREATED_DT
    )

    fun columnNamesForPrinting() = columnNames()

    fun dataTypes() = listOf(
            " INTEGER PRIMARY KEY AUTOINCREMENT,",
            " INTEGER,",
            " INTEGER,",
            " INTEGER,",
            " INTEGER,",
            " REAL,",
            " INTEGER"
    )

    fun dataTypesForPrinting() = listOf(
            "INT",
            "INT",
            "INT",
            "INT",
            "INT",
            "REAL",
            "INT"
    )
}
