/*
 * Copyright (c) 2018 Sean Grimes. All Rights Reserved.
 * License: MIT
 */

package edu.antevortadb.dbInteraction.columnsAndKeys

import edu.antevortadb.configs.Finals

object MovielensMovies {
    fun csvKeys() = listOf(
            "movieID",
            "title",
            "genres"
    )

    fun columnNames() = listOf(
            Finals.ID,
            Finals.TMDB_ID,
            Finals.IMDB_ID,
            Finals.ML_ID,
            "movielens_title",
            "genres"
    )

    fun columnsForPrinting() = columnNames()

    fun dataTypes() = listOf(
            " INTEGER PRIMARY KEY AUTOINCREMENT,",
            " INTEGER,",
            " INTEGER,",
            " INTEGER,",
            " TEXT,",
            " JSON"
    )

    fun dataTypesForPrinting() = listOf(
            "INT",
            "INT",
            "INT",
            "INT",
            "TEXT",
            "JSON"
    )
}
