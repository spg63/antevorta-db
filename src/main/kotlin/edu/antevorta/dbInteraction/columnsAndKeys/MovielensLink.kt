/*
 * Copyright (c) 2018 Sean Grimes. All Rights Reserved.
 * License: MIT
 */

package edu.antevorta.dbInteraction.columnsAndKeys

import edu.antevorta.configs.Finals

// Table = links_table
object MovielensLink{
    fun csvKeys() = listOf(
            "movieId",
            "imdbId",
            "tmdbId"
    )

    fun columnNames() = listOf(
            Finals.ID,
            Finals.TMDB_ID,
            Finals.IMDB_ID,
            Finals.ML_ID
    )

    // Not eliminating any columns when printing objects from this table
    fun columnsForPrinting() = columnNames()

    fun dataTypes() = listOf(
            " INTEGER PRIMARY KEY AUTOINCREMENT,",
            " INTEGER,",
            " INTEGER,",
            " INTEGER"
    )

    // Not eliminating any columns when printing objects from this table
    fun dataTypesForPrinting() = listOf(
            "INT",
            "INT",
            "INT",
            "INT"
    )
}
