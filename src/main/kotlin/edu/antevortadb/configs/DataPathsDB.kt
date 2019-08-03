package edu.antevortadb.configs

import edu.antevortadb.dbInteraction.dbSelector.BaseMapper
import edu.antevortadb.dbInteraction.dbSelector.RSMapper

var map: RSMapper = BaseMapper()

class DataPathsDB {
    private val maps: MutableList<RSMapper> = mutableListOf()

    init{
        // Need to read in the DB stuff here
    }

    companion object {
        private val instance: DataPathsDB = DataPathsDB()

        @Synchronized
        fun get(): DataPathsDB {
            return instance
        }
    }

}