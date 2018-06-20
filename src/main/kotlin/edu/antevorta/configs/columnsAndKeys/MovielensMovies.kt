/*
 * Copyright (c) 2018 Sean Grimes. All Rights Reserved.
 * License: MIT
 */

package edu.antevorta.configs.columnsAndKeys

import edu.antevorta.configs.Finals

object MovielensMovies {
    fun CSVKeys() = listOf(
            "movieID",
            "title",
            "genres"
    )

    fun columnNames() = listOf(
            Finals.ID,
            "tmdb_movieid",
            "imdb_movieid",
            "movielens_movieid",
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