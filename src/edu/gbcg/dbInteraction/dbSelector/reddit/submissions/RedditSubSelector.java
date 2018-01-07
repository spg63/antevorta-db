/*
 * Copyright (c) 2018 Sean Grimes. All rights reserved.
 * License: MIT License
 */

package edu.gbcg.dbInteraction.dbSelector.reddit.submissions;

import edu.gbcg.configs.Finals;
import edu.gbcg.dbInteraction.dbSelector.RSMapper;
import edu.gbcg.dbInteraction.dbSelector.SelectionWorker;
import edu.gbcg.dbInteraction.dbSelector.Selector;
import edu.gbcg.configs.DBLocator;

import java.util.ArrayList;
import java.util.List;

public class RedditSubSelector extends Selector {

    public RedditSubSelector(){
        this.tableName = Finals.SUB_TABLE_NAME;
    }

    /**
     * Return a list of RSMapper objects from the DB query
     * @param SQLStatement
     * @return The list of RSMappers, null if query returned 0 results
     */
    public List<RSMapper> generalSelection(String SQLStatement){
        List<String> DBs = DBLocator.redditSubsAbsolutePaths();
        verifyDBsExist(DBs);

        List<SelectionWorker> workers = new ArrayList<>();
        for(int i = 0; i < DBs.size(); ++i)
            workers.add(new SelectionWorker(DBs.get(i), SQLStatement, new SubmissionSetMapper()));
        return genericSelect(workers, SQLStatement);
    }
}
