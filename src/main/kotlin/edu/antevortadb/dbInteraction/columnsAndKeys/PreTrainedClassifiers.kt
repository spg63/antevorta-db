package edu.antevortadb.dbInteraction.columnsAndKeys

import edu.antevortadb.configs.Finals

object PreTrainedClassifiers {
    fun columnNames() = listOf (
            Finals.ID,
            Finals.CREATED_DT,
            "AgentName",
            "BlobData"
    )

    fun dataTypes() = listOf (
            " INTEGER PRIMARY KEY AUTOINCREMENT,",
            " INTEGER,",
            " TEXT,",
            " BLOB"
    )
}