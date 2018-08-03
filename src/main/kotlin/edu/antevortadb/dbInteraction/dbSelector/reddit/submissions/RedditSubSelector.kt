/*
 * Copyright (c) 2018 Sean Grimes. All Rights Reserved.
 * License: MIT
 */

package edu.antevortadb.dbInteraction.dbSelector.reddit.submissions

import edu.antevortadb.configs.DBLocator
import edu.antevortadb.configs.Finals
import edu.antevortadb.dbInteraction.columnsAndKeys.RedditSubs
import edu.antevortadb.dbInteraction.dbSelector.RSMapper
import edu.antevortadb.dbInteraction.dbSelector.SelectionWorker
import edu.antevortadb.dbInteraction.dbSelector.Selector

class RedditSubSelector : Selector() {
    init {
        this.tableName = Finals.REDDIT_SUB_TABLE
        this.listOfColumns = RedditSubs.columnNames()
    }

    override fun generalSelection(SQLStatement: String): List<RSMapper> {
        val dbs = DBLocator.redditSubsAbsolutePaths()
        verifyDBsExist(dbs)

        val workers = ArrayList<SelectionWorker>()
        for(i in 0 until dbs.size)
            workers.add(SelectionWorker(dbs[i], SQLStatement, SubmissionSetMapper()))
        return genericSelect(workers, SQLStatement)
    }
}
