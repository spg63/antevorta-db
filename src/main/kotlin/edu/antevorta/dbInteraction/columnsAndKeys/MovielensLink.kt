/*
 * Copyright (c) 2018 Sean Grimes. All Rights Reserved.
 * License: MIT
 */

package edu.antevorta.dbInteraction.columnsAndKeys

import edu.antevorta.configs.Finals

// Table = links_table
object MovielensLink{
    fun CSVKeys() = listOf(
            "movieId",
            "imdbId",
            "tmdbId"
    )

    fun columnNames() = listOf(
            Finals.ID,
            "tmdb_movieid",
            "imdb_movieid",
            "movielens_movieid"
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
