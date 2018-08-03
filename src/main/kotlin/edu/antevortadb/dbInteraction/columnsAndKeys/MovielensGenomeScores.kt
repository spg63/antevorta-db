/*
 * Copyright (c) 2018 Sean Grimes. All Rights Reserved.
 * License: MIT
 */

package edu.antevortadb.dbInteraction.columnsAndKeys

import edu.antevortadb.configs.Finals

object MovielensGenomeScores {
    fun csvKeys() = listOf(
            "movieId",
            "tagId",
            "relevance"
    )

    fun columnNames() = listOf(
            Finals.ID,
            Finals.TMDB_ID,
            Finals.IMDB_ID,
            Finals.ML_ID,
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
