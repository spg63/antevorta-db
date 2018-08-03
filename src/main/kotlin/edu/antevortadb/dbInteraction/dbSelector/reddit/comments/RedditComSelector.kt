/*
 * Copyright (c) 2018 Sean Grimes. All Rights Reserved.
 * License: MIT
 */

package edu.antevortadb.dbInteraction.dbSelector.reddit.comments

import edu.antevortadb.configs.DBLocator
import edu.antevortadb.configs.Finals
import edu.antevortadb.dbInteraction.columnsAndKeys.RedditComs
import edu.antevortadb.dbInteraction.dbSelector.RSMapper
import edu.antevortadb.dbInteraction.dbSelector.SelectionWorker
import edu.antevortadb.dbInteraction.dbSelector.Selector

class RedditComSelector : Selector() {
    init {
        this.tableName = Finals.REDDIT_COM_TABLE
        this.listOfColumns = RedditComs.columnNames()
    }

    override fun generalSelection(SQLStatement: String): List<RSMapper> {
        val dbs = DBLocator.redditComsAbsolutePaths()
        verifyDBsExist(dbs)

        val workers = ArrayList<SelectionWorker>()
        for(i in 0 until dbs.size)
            workers.add(SelectionWorker(dbs[i], SQLStatement, CommentSetMapper()))
        return genericSelect(workers, SQLStatement)
    }
}
