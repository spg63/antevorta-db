/*
 * Copyright (c) 2018 Sean Grimes. All Rights Reserved.
 * License: MIT
 */

package edu.antevorta.dbInteraction.dbSelector.reddit.submissions

import edu.antevorta.configs.DBLocator
import edu.antevorta.configs.Finals
import edu.antevorta.configs.columnsAndKeys.RedditSubs
import edu.antevorta.dbInteraction.dbSelector.RSMapper
import edu.antevorta.dbInteraction.dbSelector.SelectionWorker
import edu.antevorta.dbInteraction.dbSelector.Selector

class RedditSubSelector : Selector() {
    init {
        this.tableName = Finals.REDDIT_SUB_TABLE
        this.listOfColumns = RedditSubs.columnNames()
    }

    override fun generalSelection(SQLStatement: String): List<RSMapper> {
        val DBs = DBLocator.redditSubsAbsolutePaths()
        verifyDBsExist(DBs)

        val workers = ArrayList<SelectionWorker>()
        for(i in 0 until DBs.size)
            workers.add(SelectionWorker(DBs[i], SQLStatement, SubmissionSetMapper()))
        return genericSelect(workers, SQLStatement)
    }
}
