/*
 * Copyright (c) 2018 Sean Grimes. All Rights Reserved.
 * License: MIT
 */

package edu.gbcg.dbInteraction.dbSelector.reddit.submissions

import edu.gbcg.configs.DBLocator
import edu.gbcg.configs.Finals
import edu.gbcg.dbInteraction.dbSelector.RSMapper
import edu.gbcg.dbInteraction.dbSelector.SelectionWorker
import edu.gbcg.dbInteraction.dbSelector.Selector

class RedditSubSelector: Selector {
    constructor(){
        this.tableName = Finals.SUB_TABLE_NAME
    }

    override fun generalSelection(SQLStatement: String): List<RSMapper> {
        val DBs = DBLocator.redditSubsAbsolutePaths()
        verifyDBsExist(DBs)

        var workers = ArrayList<SelectionWorker>()
        for(i in 0 until DBs.size)
            workers.add(SelectionWorker(DBs[i], SQLStatement, SubmissionSetMapper()))
        return genericSelect(workers, SQLStatement)
    }
}
