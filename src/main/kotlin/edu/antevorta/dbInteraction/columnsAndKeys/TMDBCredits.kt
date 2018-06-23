/*
 * Copyright (c) 2018 Sean Grimes. All Rights Reserved.
 * License: MIT
 */

package edu.antevorta.dbInteraction.columnsAndKeys

import edu.antevorta.configs.Finals

object TMDBCredits {
    fun CSVKeys() = listOf(
            "movie_id",
            "title",
            "cast",
            "crew"
    )

    fun columnNames() = listOf(
            Finals.ID,
            Finals.TMDB_ID,
            Finals.IMDB_ID,
            Finals.ML_ID,
            "TMDB_title",
            "cast",
            "crew"
    )

    fun columnsForPrinting() = columnNames()

    fun dataTypes() = listOf(
            " INTEGER PRIMARY KEY AUTOINCREMENT,",
            " INTEGER,",
            " INTEGER,",
            " INTEGER,",
            " TEXT,",
            " JSON,",
            " JSON"
    )

    fun dataTypesForPrinting() = listOf(
            "INT",
            "INT",
            "INT",
            "INT",
            "TEXT",
            "JSON",
            "JSON"
    )
}