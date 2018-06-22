/*
 * Copyright (c) 2018 Sean Grimes. All Rights Reserved.
 * License: MIT
 */

package edu.antevorta.configs.columnsAndKeys

import edu.antevorta.configs.Finals

object MovielensIndividualTags {
    fun CSVKeys() = listOf(
            "userId",
            "movieId",
            "tag",
            "timestamp"
    )

    fun columnNames() = listOf(
            Finals.ID,
            "tmdb_movieid",
            "imdb_movieid",
            "movielens_movieid",
            "userid",
            "tagid",
            "tag",
            "timestamp"
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