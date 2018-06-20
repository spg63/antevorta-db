/*
 * Copyright (c) 2018 Sean Grimes. All Rights Reserved.
 * License: MIT
 */

package edu.antevorta.configs.columnsAndKeys

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
            "tmdb_movieid",
            "imdb_movieid",
            "movielens_movieid",
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

    )
}