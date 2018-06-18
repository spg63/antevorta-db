/*
 * Copyright (c) 2018 Sean Grimes. All Rights Reserved.
 * License: MIT
 */

package edu.gbcg.configs.columnsAndKeys

// Table = links_table
object MovielensLink{
    fun CSVKeys() = listOf(
            "movieId",
            "imdbId",
            "tmdbId"
    )

    fun columnNames() = listOf(
            "ID",
            "tmdb_movieid",
            "imdb_movieid",
            "movielens_movieid"
    )

    // Not eliminating any columns when printing objects from this table
    fun columnsForPrinting() = columnNames()

    fun dataTypes() = listOf(
            "INT",
            "INT",
            "INT",
            "INT"
    )

    // Not eliminating any columns when printing objects from this table
    fun dataTypesForPrinting() = dataTypes()
}