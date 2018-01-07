/*
 * Copyright (c) 2018 Sean Grimes. All rights reserved.
 * License: MIT License
 */

package edu.gbcg.dbInteraction.dbSelector.reddit.comments;

import edu.gbcg.configs.Finals;
import edu.gbcg.dbInteraction.dbSelector.RSMapper;
import edu.gbcg.dbInteraction.dbSelector.SelectionWorker;
import edu.gbcg.dbInteraction.dbSelector.Selector;
import edu.gbcg.configs.DBLocator;

import java.util.ArrayList;
import java.util.List;

public class RedditComSelector extends Selector {

    public RedditComSelector(){
        this.tableName = Finals.COM_TABLE_NAME;
    }

    /**
     * Execute a SQL query across all DB shards in parallel
     * @param SQLStatement
     * @return A list of RSMapper objects containing any results
     */
    public List<RSMapper> generalSelection(String SQLStatement){
        List<String> DBs = DBLocator.redditComsAbsolutePaths();
        verifyDBsExist(DBs);

        List<SelectionWorker> workers = new ArrayList<>();
        for(int i = 0; i < DBs.size(); ++i)
            workers.add(new SelectionWorker(DBs.get(i), SQLStatement, new CommentSetMapper()));
        return genericSelect(workers, SQLStatement);
    }
}
