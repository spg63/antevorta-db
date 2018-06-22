/*
 * Copyright (c) 2018 Sean Grimes. All Rights Reserved.
 * License: MIT
 */

package edu.antevorta.dbInteraction.columnsAndKeys

import edu.antevorta.configs.Finals

object MovielensGenomeScores {
    fun CSVKeys() = listOf(
            "movieId",
            "tagId",
            "relevance"
    )

    fun columnNames() = listOf(
            Finals.ID,
            "tmdb_movieid",
            "imdb_movieid",
            "movielens_movieid",
            "tagid",
            "relevance"
    )

    fun columnsForPrinting() = columnNames()

    fun dataTypes() = listOf(
            " INTEGER PRIMARY KEY AUTOINCREMENT,",
            " INTEGER,",
            " INTEGER,",
            " INTEGER,",
            " INTEGER,",
            " REAL"
    )

    fun dataTypesForPrinting() = listOf(
            "INT",
            "INT",
            "INT",
            "INT",
            "INT",
            "REAL"
    )

}
